package com.example.scraper.service.impl;

import com.example.scraper.model.Item;
import com.example.scraper.service.ItemService;
import com.example.scraper.service.ScraperService;
import com.example.scraper.util.CategoryEncoder;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class TechstarsScraperService implements ScraperService {
    private static final String FILTER_PREFIX = "?filter=";
    private static final String MAX_PAGE_PARAM = "&page=" + 20;

    private final ItemService itemService;

    @Value("${scrape.source.url.techstars}")
    private String baseUrl;

    @Override
    public void collectData(List<String> jobFunctions) {
        log.info("Starting collecting data by job functions: " + jobFunctions);
        var categoryFilterParam = buildFilter(jobFunctions);
        var jobElements = scrapeJobElements(categoryFilterParam);
        List<Future<Item>> itemsFuture = new ArrayList<>();

        try (var executorService = Executors
                .newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            for (Element element: jobElements) {
                var itemFuture = executorService.submit(() -> parseElementToItem(element));

                itemsFuture.add(itemFuture);
            }
        }
        List<Item> items = getItemsListFromFuture(itemsFuture);
        saveItemsToDb(items);
    }

    private String buildFilter(List<String> jobFunctions) {
        var filter = "";
        if (!jobFunctions.isEmpty()) {
            filter = FILTER_PREFIX + CategoryEncoder.encodeToBase64(jobFunctions);
        }
        return filter;
    }

    private Elements scrapeJobElements(String jobFunctionFilterParam) {
        var url = baseUrl + "/jobs" + jobFunctionFilterParam + MAX_PAGE_PARAM;

        Document document;
        try {
            log.info("Starting scraping document from url: " + url);
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("Can't get document from: " + url);
            return new Elements();
        }
        log.info("Connected successfully and got document");

        return document.select(".sc-beqWaB.gupdsY.job-card[data-testid=job-list-item]");
    }

    private Item parseElementToItem(Element jobElement) {
        String jobPageUrl;
        String organizationUrl;
        String logoUrl;
        String organizationTitle;
        String positionName;
        String laborFunction;
        String locations;
        long postedDate;

        try {
            jobPageUrl = baseUrl + Objects.requireNonNull(
                    jobElement.selectFirst("a[data-testid=job-title-link]"))
                    .attr("href");

            if (jobPageUrl.length() > 750) {
                return null;
            }

            organizationUrl = baseUrl
                    + Objects.requireNonNull(jobElement.selectFirst("a[data-testid=link]"))
                    .attr("href");

            logoUrl = Objects.requireNonNull(jobElement.selectFirst("meta[itemprop=logo]"))
                    .attr("content");

            organizationTitle = Objects.requireNonNull(jobElement.selectFirst("meta[itemprop=name]"))
                    .attr("content");

            positionName = Objects.requireNonNull(
                            jobElement.selectFirst("div[itemprop=title]"))
                    .text();

            laborFunction = Objects.requireNonNullElse(
                            jobElement.selectFirst("div[data-testid=tag]"),
                            new Element("no function"))
                    .text();

            locations = Objects.requireNonNullElse(
                            jobElement.selectFirst("div[itemprop=jobLocation]"),
                            new Element("no location"))
                    .text();

            postedDate = parseDateToTimestamp(jobElement);
        } catch (NullPointerException ignored) {
            log.warn("Can't retrieve data from element: " + jobElement);
            return null;
        }

        String description;
        try {
            description = Objects.requireNonNull(
                    jobElement.selectFirst("meta[itemprop=description]"))
                    .attr("content");
        } catch (NullPointerException e) {
            log.warn("No description in the element: " + jobElement);
            description = "no description";
        }

        var tags = parseTagsToSet(jobElement);

        return new Item(
                jobPageUrl,
                positionName,
                organizationUrl,
                logoUrl,
                organizationTitle,
                laborFunction,
                locations,
                postedDate,
                description,
                tags
        );
    }

    private long parseDateToTimestamp(Element element) throws NullPointerException {
        String postedDateStr = Objects.requireNonNull(element.selectFirst("meta[itemprop=datePosted]"))
                .attr("content");
        LocalDate localDate = LocalDate.parse(postedDateStr);

        return localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private String parseTagsToSet(Element element) {
        Elements tagElements = element.select("div[data-testid=tag]");

        StringBuilder tagSb = new StringBuilder();
        for (Element tagElement : tagElements) {
            tagSb.append(tagElement.text());
            tagSb.append("; ");
        }

        return tagSb.toString();
    }

    private List<Item> getItemsListFromFuture(List<Future<Item>> itemsFuture) {
        List<Item> items = new ArrayList<>();
        itemsFuture.forEach(itemFuture -> {
            try {
                items.add(itemFuture.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        return items;
    }

    private void saveItemsToDb(List<Item> items) {
        if (!items.isEmpty()) {
            for (Item item: items) {
                try {
                    if (item != null) {
                        itemService.save(item);
                    }
                } catch (DataIntegrityViolationException ignored) {}
            }
        }
    }
}
package com.example.scraper.service;

import com.example.scraper.model.Item;
import com.example.scraper.util.CategoryEncoder;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class TechstarsScraperService implements ScraperService {
    private static final String FILTER_PREFIX = "?filter=";
    private static final String MAX_PAGE_PARAM = "&page=" + Integer.MAX_VALUE;

    private final ItemService itemService;

    @Value("${scrape.source.url.techstars}")
    private String baseUrl;

    @Override
    public void collectData(List<String> jobFunctions) {
        log.info("Starting collecting data by job functions: " + jobFunctions);
        var categoryFilterParam = buildFilter(jobFunctions);
        var jobElements = scrapeJobElements(categoryFilterParam);

        for (Element element: jobElements) {
            var item = parseElementToItem(element);

            if (item != null ) {
                itemService.save(item);
            }
        }
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
        String locationStr;

        try {
            jobPageUrl = baseUrl + Objects.requireNonNull(
                    jobElement.selectFirst("a[data-testid=job-title-link]"))
                    .attr("href");
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
            locationStr = Objects.requireNonNullElse(
                            jobElement.selectFirst("div[itemprop=jobLocation]"),
                            new Element("no location"))
                    .text();
        } catch (NullPointerException e) {
            log.warn("Can't retrieve data from element: " + jobElement);
            return null;
        }

        var postedDate = parseDateToTimestamp(jobElement);
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
        var locationsSet = Arrays.stream(locationStr.split(";"))
                .collect(Collectors.toSet());

        return new Item(
                jobPageUrl,
                positionName,
                organizationUrl,
                logoUrl,
                organizationTitle,
                laborFunction,
                locationsSet,
                postedDate,
                description,
                tags
        );
    }

    private long parseDateToTimestamp(Element element) {
        String postedDateStr;
        try {
            postedDateStr = Objects.requireNonNull(element.selectFirst("meta[itemprop=datePosted]"))
                    .attr("content");
        } catch (NullPointerException e) {
            log.warn("Can't retrieve a time from element: " + element);
            return 0L;
        }
        LocalDate localDate = LocalDate.parse(postedDateStr);
        return localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private Set<String> parseTagsToSet(Element element) {
        Elements tagElements = element.select("div[data-testid=tag]");

        Set<String> tags = new HashSet<>();
        for (Element tagElement : tagElements) {
            tags.add(tagElement.text());
        }

        return tags;
    }
}

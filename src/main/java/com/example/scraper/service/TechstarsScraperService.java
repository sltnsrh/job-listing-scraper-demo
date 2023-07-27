package com.example.scraper.service;

import com.example.scraper.model.Item;
import com.example.scraper.util.CategoryEncoder;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TechstarsScraperService {
    private static final String FILTER_PREFIX = "?filter=";

    private final ItemService itemService;

    @Value("${scrape.source.url.techstars}")
    private String baseUrl;

    public void collectData(List<String> categories) {
        var categoryFilterParam = buildFilter(categories);
        var jobElements = scrapeJobElements(categoryFilterParam);

        for (Element element: jobElements) {
            var item = parseElementToItem(element);
            itemService.save(item);
        }
    }

    private String buildFilter(List<String> categories) {
        var filter = "";
        if (!categories.isEmpty()) {
            filter = FILTER_PREFIX + CategoryEncoder.encodeToBase64(categories);
        }
        return filter;
    }

    private Elements scrapeJobElements(String categoryFilterParam) {
        Document document;
        try {
            document = Jsoup.connect(baseUrl + "/jobs" + categoryFilterParam).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return document.select(".sc-beqWaB.gupdsY.job-card[data-testid=job-list-item]");
    }

    private Item parseElementToItem(Element jobElement) {
        var jobPageUrl = baseUrl
                + jobElement.selectFirst("a[data-testid=job-title-link]").attr("href");
        var positionName = jobElement.selectFirst("div[itemprop=title]").text();
        var organizationUrl = baseUrl
                + jobElement.selectFirst("a[data-testid=link]").attr("href");
        var logoUrl = jobElement.selectFirst("meta[itemprop=logo]").attr("content");
        var organizationTitle = jobElement.selectFirst("meta[itemprop=name]").attr("content");
        var laborFunction = jobElement.selectFirst("div[data-testid=tag]").text();
        var location = jobElement.selectFirst("div[itemprop=jobLocation]").text();
        var postedDate = parseDateToTimestamp(jobElement);
        var description = jobElement.selectFirst("meta[itemprop=description]").attr("content");
        var tags = parseTagsToSet(jobElement);

        return new Item(
                jobPageUrl,
                positionName,
                organizationUrl,
                logoUrl,
                organizationTitle,
                laborFunction,
                location,
                postedDate,
                description,
                tags
        );
    }

    private long parseDateToTimestamp(Element element) {
        var postedDateStr = element.selectFirst("meta[itemprop=datePosted]").attr("content");
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

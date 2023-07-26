package com.example.scraper;

import com.example.scraper.model.Item;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ScraperService {
    @Value("${search.job.website.url}")
    private String jobSiteUrl;

    public void collectData(List<String> categories) {
        Document document;
        try {
            document = Jsoup.connect(jobSiteUrl).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Elements jobElements = document.select(".sc-beqWaB.gupdsY.job-card[data-testid=job-list-item]");

        for (Element element: jobElements) {
            var item = parseElementToItem(element);
            //todo: save item to DB using ItemService
        }
    }

    private Item parseElementToItem(Element jobElement) {
        var jobPageUrl = "https://jobs.techstars.com"
                + jobElement.selectFirst("a[data-testid=job-title-link]").attr("href");
        var positionName = jobElement.selectFirst("div[itemprop=title]").text();
        var organizationUrl = "https://jobs.techstars.com"
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

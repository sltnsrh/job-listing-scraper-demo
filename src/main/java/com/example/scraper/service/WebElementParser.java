package com.example.scraper.service;

import com.example.scraper.model.Item;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Log4j2
public class WebElementParser {

    public List<Item> parseElementsToItems(List<WebElement> elements) {
        List<Future<Item>> jobItemsFuture = new ArrayList<>();
        try (var executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors())) {

            for (WebElement jobElement: elements) {
                Future<Item> itemFuture =
                        executorService.submit(() -> parseElementToItem(jobElement));
                jobItemsFuture.add(itemFuture);
            }
        }
        return getItemsListFromFuture(jobItemsFuture);
    }

    private Item parseElementToItem(WebElement jobElement) {
        String jobPageUrl = jobElement.findElement(
                By.cssSelector("a[data-testid=job-title-link]")).getAttribute("href");
        if (jobPageUrl.length() > 760) {
            log.error("Job page url is to long to save to DB. Item will not be saved. Job url: "
                    + jobPageUrl);
            return null;
        }
        String positionName = jobElement.findElement(By.cssSelector("div[itemprop=title]")).getText();
        String laborFunction;
        try {
            laborFunction = jobElement.findElement(By.cssSelector("div[data-testid=tag]")).getText();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            log.warn("Error while finding labour function element: " + e.getMessage());
            laborFunction = "";
        }
        String organizationUrl = jobElement.findElement(
                By.cssSelector("a[data-testid=link]")).getAttribute("href");
        String logoUrl = jobElement.findElement(
                By.cssSelector("meta[itemprop=logo]")).getAttribute("content");
        String organizationTitle = jobElement.findElement(
                By.cssSelector("meta[itemprop=name]")).getAttribute("content");
        String locationStr;
        try {
            locationStr = jobElement.findElement(By.cssSelector("div[itemprop=jobLocation]")).getText();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            log.warn("Error while finding location element: " + e.getMessage());
            locationStr = "";
        }
        String description = jobElement.findElement(
                By.cssSelector("meta[itemprop=description]")).getAttribute("content");
        String postedDateStr = jobElement.findElement(
                By.cssSelector("meta[itemprop=datePosted]")).getAttribute("content");
        LocalDate localDate = LocalDate.parse(postedDateStr);
        long postedDate = localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        String tags;
        try {
            List<WebElement> tagElements = jobElement.findElements(By.cssSelector("div[data-testid=tag]"));
            StringBuilder tagsBuilder = new StringBuilder();
            for (WebElement tagElement : tagElements) {
                tagsBuilder.append(tagElement.getText()).append("; ");
            }
            tags = tagsBuilder.toString().trim();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            log.error("Error while finding tags elements: " + e.getMessage());
            tags = "";
        }

        return new Item(
                jobPageUrl,
                positionName,
                organizationUrl,
                logoUrl,
                organizationTitle,
                laborFunction,
                locationStr,
                postedDate,
                description,
                tags
        );
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
}

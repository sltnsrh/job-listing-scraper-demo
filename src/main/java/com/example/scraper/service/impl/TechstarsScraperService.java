package com.example.scraper.service.impl;

import com.example.scraper.model.Item;
import com.example.scraper.service.ItemService;
import com.example.scraper.service.ScraperService;
import com.example.scraper.util.CategoryEncoder;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class TechstarsScraperService implements ScraperService {
    private static final String FILTER_PREFIX = "?filter=";

    private final ItemService itemService;
    private final WebDriver webDriver;

    @Value("${scrape.source.url.techstars}")
    private String baseUrl;

    @Override
    public void collectData(List<String> jobFunctions) {
        log.info("Starting collecting data by job functions: " + jobFunctions);
        var categoryFilterParam = buildFilter(jobFunctions);
        var countOfPages = calculateCountOfPages(categoryFilterParam);

        processAllRequestedElements(categoryFilterParam, countOfPages);
    }

    private String buildFilter(List<String> jobFunctions) {
        var filter = "";
        if (!jobFunctions.isEmpty()) {
            filter = FILTER_PREFIX + CategoryEncoder.encodeToBase64(jobFunctions);
        }
        return filter;
    }

    private int calculateCountOfPages(String categoryFilterParam) {
        webDriver.get(baseUrl + "/jobs" + categoryFilterParam);
        var element = webDriver.findElement(By.cssSelector("div.sc-beqWaB.eJrfpP"));
        String jobsCountText = element.findElement(By.tagName("b")).getText();
        int jobsCount = Integer.parseInt(jobsCountText.replaceAll(",", ""));
        return jobsCount / 19;
    }

    private void processAllRequestedElements(String jobFunctionFilterParam, final int pagesCount) {
        int totalElementsScraped = 0;
        int lastLoadedPage = 0;

        totalElementsScraped += saveDataFromFistPage(jobFunctionFilterParam);
        lastLoadedPage++;

        totalElementsScraped += saveDataFromSecondPage(totalElementsScraped);
        lastLoadedPage++;

        while(lastLoadedPage < pagesCount) {
            ((JavascriptExecutor) webDriver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            log.info("Scrolling the next page...");
            this.sleep(2L);

            lastLoadedPage++;
            log.info("Last loaded page is " + lastLoadedPage);

            var loadedElements = webDriver.findElements(By.cssSelector(".sc-beqWaB.gupdsY.job-card[data-testid=job-list-item]"));
            log.info("Last loaded elements size = " + loadedElements.size());
            var elementsToProcess = loadedElements.stream()
                    .skip(totalElementsScraped)
                    .toList();

            try (var executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
                executorService.submit(() -> parseAndSaveItems(elementsToProcess));
            }
            totalElementsScraped += elementsToProcess.size();
        }
        log.info("Complete scrolling. Total elements scraped: " + totalElementsScraped);
        log.info("Total element save in DB: " + itemService.count());
    }

    private void parseAndSaveItems(List<WebElement> elements) {
        var items = parseElementsToItems(elements);
        saveItemsToDb(items);
    }

    private int saveDataFromFistPage(String jobFunctionFilterParam) {
        var url = baseUrl + "/jobs" + jobFunctionFilterParam;
        webDriver.get(url);

        var firstPageElements = webDriver.findElements(By.cssSelector(".sc-beqWaB.gupdsY.job-card[data-testid=job-list-item]"));
        try (var executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            executorService.submit(() -> parseAndSaveItems(firstPageElements));
        }
        return firstPageElements.size();
    }

    private int saveDataFromSecondPage(int totalElementsScraped) {
        WebElement loadMoreButton = webDriver.findElement(By.cssSelector("button[data-testid=load-more]"));
        if (loadMoreButton.isDisplayed()) {
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", loadMoreButton);
            log.info("Pressing load more button... Waiting for loading second page...");
            try {
                while (loadMoreButton.isDisplayed()) {
                    this.sleep(2L);
                }
            } catch (StaleElementReferenceException ignored){}
            log.info("Loaded second page. Getting data...");
        }

        var currentLoadedList = webDriver.findElements(By.cssSelector(".sc-beqWaB.gupdsY.job-card[data-testid=job-list-item]"));
        var elementsToSave = currentLoadedList.stream()
                .skip(totalElementsScraped)
                .toList();
        try (var executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            executorService.submit(() -> parseAndSaveItems(elementsToSave));
        }

        return elementsToSave.size();
    }

    private List<Item> parseElementsToItems(List<WebElement> elements) {
        List<Future<Item>> jobItemsFuture = new ArrayList<>();
        try (var executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {

            for (WebElement jobElement: elements) {
                Future<Item> itemFuture =
                        executorService.submit(() -> parseElementToItem(jobElement));
                jobItemsFuture.add(itemFuture);
            }
        }
        return getItemsListFromFuture(jobItemsFuture);
    }

    private Item parseElementToItem(WebElement jobElement) {
        String jobPageUrl = jobElement.findElement(By.cssSelector("a[data-testid=job-title-link]")).getAttribute("href");
        if (jobPageUrl.length() > 760) {
            return null;
        }
        String positionName = jobElement.findElement(By.cssSelector("div[itemprop=title]")).getText();
        String laborFunction;
        try {
            laborFunction = jobElement.findElement(By.cssSelector("div[data-testid=tag]")).getText();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            log.warn("Error while finding labour function element: " + e.getMessage());
            laborFunction = "no labor function";
        }
        String organizationUrl = jobElement.findElement(By.cssSelector("a[data-testid=link]")).getAttribute("href");
        String logoUrl = jobElement.findElement(By.cssSelector("meta[itemprop=logo]")).getAttribute("content");
        String organizationTitle = jobElement.findElement(By.cssSelector("meta[itemprop=name]")).getAttribute("content");
        String locationStr;
        try {
            locationStr = jobElement.findElement(By.cssSelector("div[itemprop=jobLocation]")).getText();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            log.warn("Error while finding location element: " + e.getMessage());
            locationStr = "no location info";
        }
        String description = jobElement.findElement(By.cssSelector("meta[itemprop=description]")).getAttribute("content");
        String postedDateStr = jobElement.findElement(By.cssSelector("meta[itemprop=datePosted]")).getAttribute("content");
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
            tags = "no tags info";
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

    private void saveItemsToDb(List<Item> items) {
        if (!items.isEmpty()) {
            for (Item item: items) {
                try {
                    if (item != null) {
                        itemService.save(item);
                    }
                } catch (DataIntegrityViolationException ignored) {}
            }
            log.info("{} items successfully saved to DB",  items.size());
        }
    }

    private void sleep(Long secondsToSleep) {
        try {
            Thread.sleep(secondsToSleep * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

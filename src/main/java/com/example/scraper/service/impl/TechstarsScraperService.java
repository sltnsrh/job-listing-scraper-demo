package com.example.scraper.service.impl;

import com.example.scraper.model.Item;
import com.example.scraper.service.ItemService;
import com.example.scraper.service.ScraperService;
import com.example.scraper.service.WebElementParser;
import com.example.scraper.util.CategoryEncoder;
import java.util.List;
import java.util.concurrent.Executors;
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
    private final WebElementParser webElementParser;

    @Value("${scrape.source.url.techstars}")
    private String baseUrl;

    @Override
    public void collectData(List<String> jobFunctions) {
        log.info("Starting collecting data by job functions: " + jobFunctions);
        var categoryFilterParam = buildFilter(jobFunctions);
        var countOfElements = calculateCountOfElements(categoryFilterParam);

        processAllRequestedElements(categoryFilterParam, countOfElements);
    }

    private String buildFilter(List<String> jobFunctions) {
        var filter = "";
        if (!jobFunctions.isEmpty()) {
            filter = FILTER_PREFIX + CategoryEncoder.encodeToBase64(jobFunctions);
        }
        return filter;
    }

    private int calculateCountOfElements(String categoryFilterParam) {
        webDriver.get(baseUrl + "/jobs" + categoryFilterParam);
        var element = webDriver.findElement(By.cssSelector("div.sc-beqWaB.eJrfpP"));
        String jobsCountText = element.findElement(By.tagName("b")).getText();
        return Integer.parseInt(jobsCountText.replaceAll(",", ""));
    }

    private void processAllRequestedElements(String jobFunctionFilterParam, final int totalElements) {
        int totalElementsScraped = 0;
        int allPagesCount = (totalElements - 19) / 20 + 1;
        int currentPage = 0;

        totalElementsScraped += processDataFromFistPage(jobFunctionFilterParam);
        currentPage++;
        totalElementsScraped += processDataFromSecondPage(totalElementsScraped);
        currentPage++;

        while(currentPage < allPagesCount) {

            var loadedElements = webDriver.findElements(By.cssSelector(
                    ".sc-beqWaB.gupdsY.job-card[data-testid=job-list-item]"));
            while (loadedElements.size() <= totalElementsScraped) {
                int scrollAmount = 2 * ((Long) ((JavascriptExecutor) webDriver)
                        .executeScript("return window.innerHeight;")).intValue();
                ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(0, " + scrollAmount + ");");
                log.info("Scrolling the page...");
                this.sleep(2000L);

                loadedElements = webDriver.findElements(By.cssSelector(
                        ".sc-beqWaB.gupdsY.job-card[data-testid=job-list-item]"));
            }
            log.info("Loaded new elements page, total count = " + loadedElements.size());

            var elementsToProcess = loadedElements.stream()
                    .skip(totalElementsScraped)
                    .toList();

            sendElementToExecutor(elementsToProcess);
            totalElementsScraped += elementsToProcess.size();
            currentPage++;
            log.info("Last processed page: " + currentPage);
        }
        log.info("Complete scrolling. Total elements scraped: " + totalElementsScraped);
        log.info("Total element save in DB: " + itemService.count());
    }

    private int processDataFromFistPage(String jobFunctionFilterParam) {
        var url = baseUrl + "/jobs" + jobFunctionFilterParam;
        webDriver.get(url);

        var firstPageElements = webDriver.findElements(By.cssSelector(
                ".sc-beqWaB.gupdsY.job-card[data-testid=job-list-item]"));
        sendElementToExecutor(firstPageElements);

        return firstPageElements.size();
    }

    private void sendElementToExecutor(List<WebElement> elements) {
        try (var executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() - 1)) {
            executorService.submit(() -> parseAndSaveItems(elements));
        }
    }

    private int processDataFromSecondPage(int totalElementsScraped) {
        WebElement loadMoreButton = webDriver.findElement(By.cssSelector("button[data-testid=load-more]"));
        if (loadMoreButton.isDisplayed()) {
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", loadMoreButton);
            log.info("Pressing load more button... Waiting for loading second page...");
            try {
                while (loadMoreButton.isDisplayed()) {
                    this.sleep(2000L);
                }
            } catch (StaleElementReferenceException ignored){}
            log.info("Loaded second page. Getting data...");
        }

        var currentLoadedList = webDriver.findElements(By.cssSelector(
                ".sc-beqWaB.gupdsY.job-card[data-testid=job-list-item]"));
        var elementsToSave = currentLoadedList.stream()
                .skip(totalElementsScraped)
                .toList();
        sendElementToExecutor(elementsToSave);

        return elementsToSave.size();
    }

    private void parseAndSaveItems(List<WebElement> elements) {
        log.info("Got {} elements to parse into items", elements.size());
        var items = webElementParser.parseElementsToItems(elements);
        saveItemsToDb(items);
    }

    private void saveItemsToDb(List<Item> items) {
        if (!items.isEmpty()) {
            int savedItems = 0;
            for (Item item: items) {
                try {
                    if (item != null) {
                        itemService.save(item);
                        savedItems++;
                    }
                } catch (DataIntegrityViolationException ignored) {}
            }
            log.info("{} items successfully saved to DB",  savedItems);
        }
    }

    private void sleep(Long msToSleep) {
        try {
            Thread.sleep(msToSleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

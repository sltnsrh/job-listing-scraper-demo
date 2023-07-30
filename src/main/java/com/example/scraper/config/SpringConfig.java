package com.example.scraper.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean(destroyMethod = "quit")
    public WebDriver webDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("-remote-allow-origins=*");
        System.setProperty("webdriver.chrome.driver", "/home/sltn/IdeaProjects/scraper/chromedriver");
        System.setProperty("webdriver.http.factory", "jdk-http-client");

        return new ChromeDriver(options);
    }
}

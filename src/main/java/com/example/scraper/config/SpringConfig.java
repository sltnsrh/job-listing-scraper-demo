package com.example.scraper.config;

import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
    @Value("${webdriver.run.in.docker}")
    private boolean runInDocker;
    @Value("${webdriver.chrome.host}")
    private String host;

    @Bean(destroyMethod = "quit")
    public WebDriver webDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("-remote-allow-origins=*");
        System.setProperty("webdriver.http.factory", "jdk-http-client");

        if (runInDocker) {
            try {
                return new RemoteWebDriver(new URL("http://" + host + ":" + "4444" + "/wd/hub"), options);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        System.setProperty("webdriver.chrome.driver", "./chromedriver");

        return new ChromeDriver(options);
    }
}

package com.example.scraper;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ScraperService {
    @Value("${search.job.website.url}")
    private String jobSiteUrl;

    public void collectData(List<String> categories) {

    }
}

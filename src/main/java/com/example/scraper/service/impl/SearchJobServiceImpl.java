package com.example.scraper.service.impl;

import com.example.scraper.model.Item;
import com.example.scraper.service.SearchJobService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SearchJobServiceImpl implements SearchJobService {
    private final static String WEBSITE_URL = "https://jobs.techstars.com/jobs";

    @Override
    public Page<Item> search(List<String> categories, PageRequest pageRequest) {
        return null;
    }
}

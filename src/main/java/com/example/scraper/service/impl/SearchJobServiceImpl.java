package com.example.scraper.service.impl;

import com.example.scraper.model.Item;
import com.example.scraper.service.ItemService;
import com.example.scraper.service.ScraperService;
import com.example.scraper.service.SearchJobService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchJobServiceImpl implements SearchJobService {
    private final ScraperService scraperService;
    private final ItemService itemService;

    @Override
    public Page<Item> search(List<String> jobFunctions, PageRequest pageRequest) {
        scraperService.collectData(jobFunctions);

        return itemService.findAll(pageRequest);
    }
}

package com.example.scraper.service;

import com.example.scraper.model.Item;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface SearchJobService {

    Page<Item> search(List<String> jobFunctions, PageRequest pageRequest);
}

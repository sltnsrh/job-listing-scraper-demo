package com.example.scraper.service;

import com.example.scraper.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ItemService {

    void save(Item item);

    Page<Item> findAllByFieldKeyword(PageRequest pageRequest, String findByField, String keyword);
}

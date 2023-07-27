package com.example.scraper.service;

import com.example.scraper.model.Item;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ItemService {

    void save(Item item);

    void saveAll(List<Item> items);

    Page<Item> findAll(PageRequest pageRequest);
}

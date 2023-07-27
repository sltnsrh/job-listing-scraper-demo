package com.example.scraper.service.impl;

import com.example.scraper.model.Item;
import com.example.scraper.repository.ItemRepository;
import com.example.scraper.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public void save(Item item) {
        itemRepository.save(item);
    }
}

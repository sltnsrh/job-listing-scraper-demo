package com.example.scraper.service.impl;

import com.example.scraper.model.Item;
import com.example.scraper.repository.ItemRepository;
import com.example.scraper.service.ItemService;
import com.example.scraper.util.ItemSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public void save(Item item) {
        itemRepository.save(item);
    }

    @Override
    public Page<Item> findAllByFieldKeyword(PageRequest pageRequest, String findByField, String keyword) {
        Specification<Item> specification = Specification.where(null);

        if (keyword != null && !keyword.isBlank()) {
            specification = specification.and(ItemSpecification
                    .fieldContainsIgnoreCase(findByField, keyword));
        }

        return itemRepository.findAll(specification, pageRequest);
    }
}

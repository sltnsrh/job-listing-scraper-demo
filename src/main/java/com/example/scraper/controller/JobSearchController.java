package com.example.scraper.controller;

import com.example.scraper.model.dto.ItemSearchResponseDto;
import com.example.scraper.repository.ItemRepository;
import com.example.scraper.service.SearchJobService;
import com.example.scraper.service.mapper.ItemMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobSearchController {
    private final SearchJobService searchJobService;
    private final ItemMapper itemMapper;
    private final ItemRepository repository;

    @GetMapping("/search")
    public ResponseEntity<Page<ItemSearchResponseDto>> search(
            @RequestParam(name = "jobFunction") List<String> jobFunction,
            @RequestParam(name = "sortBy", defaultValue = "positionName") String sortBy,
            @RequestParam(name = "direction", defaultValue = "ASC") String sortDirection,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "findByField", required = false) String findByField,
            @RequestParam(name = "keyword", required = false) String keyword

    ) {
        var itemDtosPage = searchJobService.search(
                jobFunction,
                        findByField,
                        keyword,
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.valueOf(sortDirection.toUpperCase()), sortBy)))
                .map(itemMapper::toDto);

        return ResponseEntity.ok(itemDtosPage);
    }
}

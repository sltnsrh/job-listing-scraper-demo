package com.example.scraper.service.mapper;

import com.example.scraper.model.Item;
import com.example.scraper.model.dto.response.ItemSearchResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemSearchResponseDto toDto(Item item);
}

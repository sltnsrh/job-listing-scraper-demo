package com.example.scraper.service.mapper;

import com.example.scraper.model.Item;
import com.example.scraper.model.dto.ItemSearchResponseDto;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "tags", qualifiedByName = "fromStrToSet")
    @Mapping(target = "locations", qualifiedByName = "fromStrToSet")
    ItemSearchResponseDto toDto(Item item);

    @Named(value = "fromStrToSet")
    default Set<String> fromStrToSet(String str) {
        return Arrays.stream(str.split("; ")).collect(Collectors.toSet());
    }
}

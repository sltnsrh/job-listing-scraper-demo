package com.example.scraper.model.dto;

import java.util.Set;
import lombok.Data;

@Data
public class ItemSearchResponseDto {
    private String jobPageUrl;
    private String positionName;
    private String organizationUrl;
    private String logoUrl;
    private String organizationTitle;
    private String laborFunction;
    private Set<String> locations;
    private Long timestamp;
    private String description;
    private Set<String> tags;
}

package com.example.scraper.model.dto.response;

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
    private String location;
    private Long timestamp;
    private String description;
    private Set<String> tags;
}

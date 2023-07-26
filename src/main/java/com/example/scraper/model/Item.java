package com.example.scraper.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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

    public Item(String jobPageUrl,
                String positionName,
                String organizationUrl,
                String logoUrl,
                String organizationTitle,
                String laborFunction,
                String location,
                Long timestamp,
                String description,
                Set<String> tags) {

        this.jobPageUrl = jobPageUrl;
        this.positionName = positionName;
        this.organizationUrl = organizationUrl;
        this.logoUrl = logoUrl;
        this.organizationTitle = organizationTitle;
        this.laborFunction = laborFunction;
        this.location = location;
        this.timestamp = timestamp;
        this.description = description;
        this.tags = tags;
    }
}

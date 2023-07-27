package com.example.scraper.model;

import jakarta.persistence.Column;
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
    @Column(name = "job_page_url", length = 1000)
    private String jobPageUrl;
    @Column(name = "position_name")
    private String positionName;
    @Column(name = "organization_url", length = 500)
    private String organizationUrl;
    @Column(name = "loge_url")
    private String logoUrl;
    @Column(name = "organization_title")
    private String organizationTitle;
    @Column(name = "labor_function")
    private String laborFunction;
    private Set<String> locations;
    private Long timestamp;
    private String description;
    private Set<String> tags;

    public Item(String jobPageUrl,
                String positionName,
                String organizationUrl,
                String logoUrl,
                String organizationTitle,
                String laborFunction,
                Set<String> locations,
                Long timestamp,
                String description,
                Set<String> tags) {

        this.jobPageUrl = jobPageUrl;
        this.positionName = positionName;
        this.organizationUrl = organizationUrl;
        this.logoUrl = logoUrl;
        this.organizationTitle = organizationTitle;
        this.laborFunction = laborFunction;
        this.locations = locations;
        this.timestamp = timestamp;
        this.description = description;
        this.tags = tags;
    }
}

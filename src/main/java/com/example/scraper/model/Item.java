package com.example.scraper.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Data;

@Entity
@Table(name = "items")
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String jobPageUrl;
    private String positionName;
    private String urlToOrganization;
    private String logoUrl;
    private String organizationTitle;
    private String laborFunction;
    private String location;
    private Long timestamp;
    private String description;
    private Set<String> tags;
}

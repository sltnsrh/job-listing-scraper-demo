package com.example.scraper.util;

import com.example.scraper.model.Item;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public final class ItemSpecification {

    private ItemSpecification() {}

    public static Specification<Item> fieldContainsIgnoreCase(String fieldName, String keyword) {
        return (Root<Item> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            Expression<String> fieldExpression = criteriaBuilder.lower(root.get(fieldName));
            String keywordLowerCase = keyword.toLowerCase();
            return criteriaBuilder.like(fieldExpression, "%" + keywordLowerCase + "%");
        };
    }
}

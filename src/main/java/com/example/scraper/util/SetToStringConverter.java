package com.example.scraper.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Converter
public class SetToStringConverter implements AttributeConverter<Set<String>, String> {

    @Override
    public String convertToDatabaseColumn(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }
        return String.join(",", set);
    }

    @Override
    public Set<String> convertToEntityAttribute(String s) {
        if (s == null || s.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(s.split(",")));
    }
}

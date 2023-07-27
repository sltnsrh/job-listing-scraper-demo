package com.example.scraper.config;

import java.util.Base64;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean
    public Base64.Encoder baseEncoder() {
        return Base64.getEncoder();
    }
}

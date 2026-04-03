package com.example.docprocessing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "dms")
public class DmsProperties {

    private String baseUrl;
    private String apiKey;
    private int connectTimeoutMs;
    private int readTimeoutMs;
}
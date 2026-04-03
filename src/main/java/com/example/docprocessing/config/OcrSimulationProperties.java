package com.example.docprocessing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "simulation.ocr")
public class OcrSimulationProperties {

    private int minDelayMs;
    private int maxDelayMs;
    private double failureRate;
}
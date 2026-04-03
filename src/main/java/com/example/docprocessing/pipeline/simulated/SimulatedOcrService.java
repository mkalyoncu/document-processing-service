package com.example.docprocessing.pipeline.simulated;

import com.example.docprocessing.config.OcrSimulationProperties;
import com.example.docprocessing.pipeline.OcrService;
import com.example.docprocessing.pipeline.model.OcrResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SimulatedOcrService implements OcrService {

    private final OcrSimulationProperties properties;

    @Override
    public OcrResult process(String base64Content, UUID requestUid) {
        SimulationSupport.randomDelay(properties.getMinDelayMs(), properties.getMaxDelayMs());

        if (SimulationSupport.shouldFail(properties.getFailureRate())) {
            throw new RuntimeException("Simulated OCR failure");
        }

        String rawText = new String(Base64.getDecoder().decode(base64Content), StandardCharsets.UTF_8);
        int wordCount = rawText.trim().isEmpty() ? 0 : rawText.trim().split("\\s+").length;

        return new OcrResult(
                rawText,
                2,
                wordCount,
                0.96);
    }
}
package com.example.docprocessing.pipeline.simulated;

import com.example.docprocessing.config.ClassifierSimulationProperties;
import com.example.docprocessing.pipeline.ClassifierService;
import com.example.docprocessing.pipeline.model.AlternativeType;
import com.example.docprocessing.pipeline.model.ClassificationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SimulatedClassifierService implements ClassifierService {

    private final ClassifierSimulationProperties properties;

    @Override
    public ClassificationResult classify(String base64Content, UUID requestUid) {
        SimulationSupport.randomDelay(properties.getMinDelayMs(), properties.getMaxDelayMs());

        if (SimulationSupport.shouldFail(properties.getFailureRate())) {
            throw new RuntimeException("Simulated classification failure");
        }

        return new ClassificationResult(
                "INVOICE",
                0.94,
                List.of(
                        new AlternativeType("PURCHASE_ORDER", 0.04),
                        new AlternativeType("RECEIPT", 0.02)
                )
        );
    }
}
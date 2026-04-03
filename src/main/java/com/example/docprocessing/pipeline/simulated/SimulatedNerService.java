package com.example.docprocessing.pipeline.simulated;

import com.example.docprocessing.config.NerSimulationProperties;
import com.example.docprocessing.pipeline.NerService;
import com.example.docprocessing.pipeline.model.NamedEntity;
import com.example.docprocessing.pipeline.model.NerResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SimulatedNerService implements NerService {

    private final NerSimulationProperties properties;

    @Override
    public NerResult extractEntities(String base64Content, UUID requestUid) {
        SimulationSupport.randomDelay(properties.getMinDelayMs(), properties.getMaxDelayMs());

        if (SimulationSupport.shouldFail(properties.getFailureRate())) {
            throw new RuntimeException("Simulated NER failure");
        }

        return new NerResult(
                List.of(
                        new NamedEntity("ORGANIZATION", "Acme Corp", 0.97),
                        new NamedEntity("DATE", "2026-01-15", 0.99),
                        new NamedEntity("AMOUNT", "$12,450.00", 0.95),
                        new NamedEntity("INVOICE_NUMBER", "INV-2026-0342", 0.98),
                        new NamedEntity("PERSON", "John Smith", 0.88)
                )
        );
    }
}
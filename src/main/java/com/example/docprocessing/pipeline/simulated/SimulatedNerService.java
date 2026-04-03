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
                        new NamedEntity("cccc", "aaa", 1)
                )
        );
    }
}
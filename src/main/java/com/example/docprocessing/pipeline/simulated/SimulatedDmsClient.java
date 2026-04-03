package com.example.docprocessing.pipeline.simulated;

import com.example.docprocessing.config.DmsSimulationProperties;
import com.example.docprocessing.pipeline.DmsClient;
import com.example.docprocessing.pipeline.model.DmsDocumentMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SimulatedDmsClient implements DmsClient {

    private final DmsSimulationProperties properties;
    private final RestClient dmsRestClient;

    /**
     * Retrieves document metadata from DMS Rest Service.
     * <p>
     * Currently not used in the simulation flow. In a real implementation,
     * this would call the external DMS service.
     */
    @Override
    @SuppressWarnings("unused")
    public DmsDocumentMetadata getMetadataFromRestClient(UUID docRef) {
        DmsDocumentMetadata response = dmsRestClient.get()
                .uri("/documents/{docRef}/metadata", docRef)
                .retrieve()
                .body(DmsDocumentMetadata.class);

        if (response == null) {
            throw new IllegalStateException("DMS metadata response is null");
        }

        return response;
    }

    @Override
    public DmsDocumentMetadata getMetadata(UUID docRef) {
        SimulationSupport.randomDelay(properties.getMinDelayMs(), properties.getMaxDelayMs());

        if (SimulationSupport.shouldFail(properties.getFailureRate())) {
            throw new RuntimeException("Simulated DMS metadata fetch failure");
        }

        int bucket = Math.abs(docRef.hashCode()) % 5;

        if (bucket == 0) {
            return new DmsDocumentMetadata(
                    "large-file.pdf",
                    "application/pdf",
                    25L * 1024 * 1024 // 25 MB
            );
        }

        return new DmsDocumentMetadata(
                "document-" + docRef + ".pdf",
                "application/pdf",
                5L * 1024 * 1024 // 2 MB
        );
    }

    @Override
    public String getContentAsBase64(UUID docRef) {
        SimulationSupport.randomDelay(properties.getMinDelayMs(), properties.getMaxDelayMs());

        if (SimulationSupport.shouldFail(properties.getFailureRate())) {
            throw new RuntimeException("Simulated DMS content fetch failure");
        }

        return Base64.getEncoder()
                .encodeToString(SimulationSupport.getDocumentContent(docRef).getBytes(StandardCharsets.UTF_8));
    }
}
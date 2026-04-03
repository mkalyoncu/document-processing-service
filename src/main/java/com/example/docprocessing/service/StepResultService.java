package com.example.docprocessing.service;

import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.domain.StepResult;
import com.example.docprocessing.domain.StepStatus;
import com.example.docprocessing.exception.StepResultNotFoundException;
import com.example.docprocessing.pipeline.model.DmsFetchResult;
import com.example.docprocessing.repository.StepResultRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StepResultService {

    private final StepResultRepository stepResultRepository;
    private final ObjectMapper objectMapper;

    public StepResult startStep(UUID documentId, ProcessingStep step) {
        StepResult stepResult = stepResultRepository.findByDocumentIdAndStep(documentId, step)
                .orElse(
                        StepResult.builder()
                                .documentId(documentId)
                                .step(step)
                                .build()
                );

        stepResult.setStatus(StepStatus.PROCESSING);
        stepResult.setStartedAt(LocalDateTime.now());
        stepResult.setCompletedAt(null);
        stepResult.setDurationMs(null);
        stepResult.setResultJson(null);
        stepResult.setErrorMessage(null);

        return stepResultRepository.save(stepResult);
    }

    public StepResult completeStep(UUID documentId, ProcessingStep step, Object result) {
        StepResult stepResult = stepResultRepository.findByDocumentIdAndStep(documentId, step)
                .orElseThrow(() -> new IllegalStateException("Step result not found for completion: " + step));

        LocalDateTime completedAt = LocalDateTime.now();

        stepResult.setStatus(StepStatus.COMPLETED);
        stepResult.setCompletedAt(completedAt);
        stepResult.setDurationMs(calculateDuration(stepResult.getStartedAt(), completedAt));
        stepResult.setResultJson(toJson(result));
        stepResult.setErrorMessage(null);

        return stepResultRepository.save(stepResult);
    }

    public StepResult failStep(UUID documentId, ProcessingStep step, String errorMessage) {
        StepResult stepResult = stepResultRepository.findByDocumentIdAndStep(documentId, step)
                .orElse(
                        StepResult.builder()
                                .documentId(documentId)
                                .step(step)
                                .startedAt(LocalDateTime.now())
                                .build()
                );

        LocalDateTime completedAt = LocalDateTime.now();

        stepResult.setStatus(StepStatus.FAILED);
        stepResult.setCompletedAt(completedAt);
        stepResult.setDurationMs(calculateDuration(stepResult.getStartedAt(), completedAt));
        stepResult.setErrorMessage(errorMessage);
        stepResult.setResultJson(null);

        return stepResultRepository.save(stepResult);
    }

    @Transactional(readOnly = true)
    public List<StepResult> getStepResults(UUID documentId) {
        return stepResultRepository.findByDocumentId(documentId);
    }

    @Transactional(readOnly = true)
    public StepResult getStepResult(UUID documentId, ProcessingStep step) {
        return stepResultRepository.findByDocumentIdAndStep(documentId, step)
                .orElseThrow(() -> new StepResultNotFoundException("Step result not found for step: " + step));
    }

    @Transactional(readOnly = true)
    public String getPersistedBase64Content(UUID documentId) {
        StepResult stepResult = getStepResult(documentId, ProcessingStep.DMS_FETCHING);

        if (stepResult.getResultJson() == null) {
            throw new IllegalStateException("DMS fetch result is empty");
        }

        try {
            DmsFetchResult result = objectMapper.readValue(stepResult.getResultJson(), DmsFetchResult.class);
            return result.getBase64Content();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse persisted DMS fetch result", e);
        }
    }

    private long calculateDuration(LocalDateTime startedAt, LocalDateTime completedAt) {
        if (startedAt == null || completedAt == null) {
            return 0L;
        }

        return Duration.between(startedAt, completedAt).toMillis();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize step result", e);
        }
    }
}
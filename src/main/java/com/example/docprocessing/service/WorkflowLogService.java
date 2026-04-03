package com.example.docprocessing.service;

import com.example.docprocessing.domain.DocumentWorkflow;
import com.example.docprocessing.domain.ProcessingStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowLogService {

    private final ObjectMapper objectMapper;

    public void logTransition(DocumentWorkflow workflow,
                              ProcessingStep previousStep,
                              ProcessingStep currentStep,
                              Long durationMs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", LocalDateTime.now());
        payload.put("level", "INFO");
        payload.put("logger", "workflow.transition");
        payload.put("documentId", workflow.getDocumentId());
        payload.put("requestUid", workflow.getRequestUid());
        payload.put("previousStep", previousStep);
        payload.put("currentStep", currentStep);
        payload.put("durationMs", durationMs);
        payload.put("message", "Step transition completed");

        log.info(toJson(payload));
    }

    public void logFailure(DocumentWorkflow workflow,
                           ProcessingStep failedAtStep,
                           String reason) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", LocalDateTime.now());
        payload.put("level", "WARN");
        payload.put("logger", "workflow.transition");
        payload.put("documentId", workflow.getDocumentId());
        payload.put("requestUid", workflow.getRequestUid());
        payload.put("failedAtStep", failedAtStep);
        payload.put("reason", reason);
        payload.put("message", "Step transition failed");

        log.warn(toJson(payload));
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return payload.toString();
        }
    }
}
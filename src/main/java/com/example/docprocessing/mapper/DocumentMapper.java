package com.example.docprocessing.mapper;

import com.example.docprocessing.domain.DocumentWorkflow;
import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.domain.StepResult;
import com.example.docprocessing.dto.DocumentDetailResponse;
import com.example.docprocessing.dto.DocumentListResponse;
import com.example.docprocessing.dto.StepResultResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DocumentMapper {

    private final ObjectMapper objectMapper;

    public DocumentDetailResponse toDetailResponse(DocumentWorkflow workflow, List<StepResult> stepResults) {
        Map<String, StepResultResponse> stepResultMap = stepResults.stream()
                .collect(Collectors.toMap(
                        stepResult -> mapStepName(stepResult.getStep()),
                        stepResult -> StepResultResponse.builder()
                                .status(stepResult.getStatus())
                                .startedAt(stepResult.getStartedAt())
                                .completedAt(stepResult.getCompletedAt())
                                .durationMs(stepResult.getDurationMs())
                                .result(parseResultJson(stepResult.getResultJson()))
                                .errorMessage(stepResult.getErrorMessage())
                                .build(),
                        (existing, replacement) -> replacement, LinkedHashMap::new));

        return DocumentDetailResponse.builder()
                .documentId(workflow.getDocumentId())
                .docRef(workflow.getDocRef())
                .requestUid(workflow.getRequestUid())
                .documentName(workflow.getDocumentName())
                .contentSizeBytes(workflow.getContentSizeBytes())
                .currentStep(workflow.getCurrentStep())
                .stepResults(stepResultMap)
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .build();
    }

    public DocumentListResponse toListResponse(DocumentWorkflow workflow) {
        return DocumentListResponse.builder()
                .documentId(workflow.getDocumentId())
                .docRef(workflow.getDocRef())
                .documentName(workflow.getDocumentName())
                .currentStep(workflow.getCurrentStep())
                .failedAtStep(workflow.getFailedAtStep())
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .build();
    }

    private String mapStepName(ProcessingStep step) {
        return switch (step) {
            case DMS_FETCHING -> "dmsFetch";
            case OCR_PROCESSING -> "ocr";
            case CLASSIFYING -> "classification";
            case NER_PROCESSING -> "ner";
            default -> step.name().toLowerCase();
        };
    }

    private Object parseResultJson(String resultJson) {
        if (resultJson == null) {
            return null;
        }

        try {
            return objectMapper.readValue(resultJson, Object.class);
        } catch (Exception e) {
            return null;
        }
    }
}
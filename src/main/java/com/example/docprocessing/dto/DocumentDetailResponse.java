package com.example.docprocessing.dto;

import com.example.docprocessing.domain.ProcessingStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailResponse {

    private UUID documentId;
    private UUID docRef;
    private UUID requestUid;
    private String documentName;
    private Long contentSizeBytes;
    private ProcessingStep currentStep;
    private Map<String, StepResultResponse> stepResults;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
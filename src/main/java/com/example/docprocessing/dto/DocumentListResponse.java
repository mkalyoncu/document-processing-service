package com.example.docprocessing.dto;

import com.example.docprocessing.domain.ProcessingStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentListResponse {

    private UUID documentId;
    private UUID docRef;
    private String documentName;
    private ProcessingStep currentStep;
    private String failedAtStep;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
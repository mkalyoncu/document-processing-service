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
public class SubmitDocumentResponse {

    private UUID documentId;
    private UUID docRef;
    private UUID requestUid;
    private ProcessingStep currentStep;
    private LocalDateTime createdAt;
}
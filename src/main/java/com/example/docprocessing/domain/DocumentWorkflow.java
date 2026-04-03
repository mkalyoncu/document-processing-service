package com.example.docprocessing.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_workflow")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentWorkflow {

    @Id
    @Column(name = "document_id", nullable = false, updatable = false)
    private UUID documentId;

    @Column(name = "doc_ref", nullable = false)
    private UUID docRef;

    @Column(name = "request_uid", nullable = false, unique = true)
    private UUID requestUid;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false)
    private ProcessingStep currentStep;

    @Column(name = "document_name")
    private String documentName;

    @Column(name = "content_size_bytes")
    private Long contentSizeBytes;

    @Column(name = "failed_at_step")
    private String failedAtStep;

    @Column(name = "failure_reason", length = 2000)
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (documentId == null) {
            documentId = UUID.randomUUID();
        }

        if (retryCount == null) {
            retryCount = 0;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
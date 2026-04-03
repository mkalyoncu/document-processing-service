package com.example.docprocessing.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "step_result",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "step_result_document_step",
                        columnNames = {"document_id", "step"}
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepResult {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "step", nullable = false)
    private ProcessingStep step;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StepStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
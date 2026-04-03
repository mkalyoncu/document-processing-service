package com.example.docprocessing.repository;

import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.domain.StepResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StepResultRepository extends JpaRepository<StepResult, UUID> {

    List<StepResult> findByDocumentId(UUID documentId);

    Optional<StepResult> findByDocumentIdAndStep(UUID documentId, ProcessingStep step);
}
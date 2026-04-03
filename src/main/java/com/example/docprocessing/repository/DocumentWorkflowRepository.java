package com.example.docprocessing.repository;

import com.example.docprocessing.domain.DocumentWorkflow;
import com.example.docprocessing.domain.ProcessingStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DocumentWorkflowRepository extends JpaRepository<DocumentWorkflow, UUID> {

    @Query("""
            SELECT d
            FROM DocumentWorkflow d
            WHERE d.currentStep = :currentStep
               OR d.failedAtStep = :failedAtStep
            """)
    List<DocumentWorkflow> findByCurrentStepOrFailedAtStep(ProcessingStep currentStep, String failedAtStep);
}
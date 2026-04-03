package com.example.docprocessing.service;

import com.example.docprocessing.domain.DocumentWorkflow;
import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.exception.DocumentNotFoundException;
import com.example.docprocessing.exception.InvalidTransitionException;
import com.example.docprocessing.repository.DocumentWorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowService {
    private static final String CANCELLED_BY_USER_REASON = "CANCELLED_BY_USER";

    private final DocumentWorkflowRepository documentWorkflowRepository;

    public DocumentWorkflow createWorkflow(UUID docRef) {
        DocumentWorkflow workflow = DocumentWorkflow.builder()
                .docRef(docRef)
                .requestUid(UUID.randomUUID())
                .currentStep(ProcessingStep.RECEIVED)
                .retryCount(0)
                .build();

        return documentWorkflowRepository.save(workflow);
    }

    @Transactional(readOnly = true)
    public DocumentWorkflow getById(UUID documentId) {
        return documentWorkflowRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));
    }

    @Transactional(readOnly = true)
    public List<DocumentWorkflow> listAll() {
        return documentWorkflowRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DocumentWorkflow> listByStatus(ProcessingStep step) {
        return documentWorkflowRepository.findByCurrentStepOrFailedAtStep(step, step.name());
    }

    public void updateStep(UUID documentId, ProcessingStep newStep) {
        DocumentWorkflow workflow = getById(documentId);

        validateTransitionStrict(workflow.getCurrentStep(), newStep);

        workflow.setCurrentStep(newStep);

        documentWorkflowRepository.save(workflow);
    }

    public void markFailed(UUID documentId, ProcessingStep failedAtStep, String reason) {
        DocumentWorkflow workflow = getById(documentId);

        workflow.setFailedAtStep(failedAtStep.name());
        workflow.setFailureReason(reason);
        workflow.setCurrentStep(ProcessingStep.FAILED);

        documentWorkflowRepository.save(workflow);
    }

    public DocumentWorkflow updateDocumentMetadata(UUID documentId, String documentName, Long contentSizeBytes) {
        DocumentWorkflow workflow = getById(documentId);

        workflow.setDocumentName(documentName);
        workflow.setContentSizeBytes(contentSizeBytes);

        return documentWorkflowRepository.save(workflow);
    }

    public DocumentWorkflow retry(UUID documentId) {
        DocumentWorkflow workflow = getById(documentId);

        if (workflow.getCurrentStep() != ProcessingStep.FAILED) {
            throw new InvalidTransitionException("Retry allowed only for FAILED documents");
        }

        if (CANCELLED_BY_USER_REASON.equals(workflow.getFailureReason())) {
            throw new InvalidTransitionException("Retry not allowed for cancelled documents");
        }

        ProcessingStep restartStep = mapFailedStepToProcessingStep(workflow.getFailedAtStep());

        workflow.setCurrentStep(restartStep);
        workflow.setFailureReason(null);
        workflow.setFailedAtStep(null);
        workflow.setRetryCount(workflow.getRetryCount() + 1);

        return documentWorkflowRepository.save(workflow);
    }

    public DocumentWorkflow cancel(UUID documentId) {
        DocumentWorkflow workflow = getById(documentId);

        ProcessingStep currentStep = workflow.getCurrentStep();

        if (currentStep == ProcessingStep.COMPLETED || currentStep == ProcessingStep.FAILED) {
            throw new InvalidTransitionException("Cannot cancel a terminal state");
        }

        workflow.setFailedAtStep(currentStep.name());
        workflow.setFailureReason(CANCELLED_BY_USER_REASON);
        workflow.setCurrentStep(ProcessingStep.FAILED);

        return documentWorkflowRepository.save(workflow);
    }

    private void validateTransitionStrict(ProcessingStep current, ProcessingStep next) {
        if (current == ProcessingStep.COMPLETED || current == ProcessingStep.FAILED) {
            throw new InvalidTransitionException(
                    "Cannot transition from terminal state: " + current
            );
        }

        if (isBackwardTransition(current, next)) {
            throw new InvalidTransitionException(
                    "Backward transition not allowed: " + current + " -> " + next
            );
        }
    }

    private boolean isBackwardTransition(ProcessingStep current, ProcessingStep next) {
        return next.ordinal() < current.ordinal();
    }

    private ProcessingStep mapFailedStepToProcessingStep(String failedAtStep) {
        if (failedAtStep == null) {
            throw new IllegalStateException("Failed step is null");
        }

        return switch (failedAtStep) {
            case "DMS_FETCH_FAILED" -> ProcessingStep.DMS_FETCHING;
            case "OCR_FAILED" -> ProcessingStep.OCR_PROCESSING;
            case "CLASSIFICATION_FAILED" -> ProcessingStep.CLASSIFYING;
            case "NER_FAILED" -> ProcessingStep.NER_PROCESSING;
            default -> throw new IllegalStateException("Unknown failed step: " + failedAtStep);
        };
    }
}
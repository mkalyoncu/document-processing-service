package com.example.docprocessing.service;

import com.example.docprocessing.domain.DocumentWorkflow;
import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.exception.InvalidTransitionException;
import com.example.docprocessing.repository.DocumentWorkflowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {
    @Mock
    private DocumentWorkflowRepository documentWorkflowRepository;

    @InjectMocks
    private WorkflowService workflowService;

    @Test
    void retry_shouldThrow_whenDocumentIsCancelled() {
        UUID documentId = UUID.randomUUID();

        DocumentWorkflow workflow = DocumentWorkflow.builder()
                .documentId(documentId)
                .currentStep(ProcessingStep.FAILED)
                .failedAtStep("OCR_PROCESSING")
                .failureReason("CANCELLED_BY_USER")
                .retryCount(0)
                .build();

        when(documentWorkflowRepository.findById(documentId)).thenReturn(Optional.of(workflow));

        InvalidTransitionException ex = assertThrows(
                InvalidTransitionException.class,
                () -> workflowService.retry(documentId)
        );

        assertEquals("Retry not allowed for cancelled documents", ex.getMessage());
    }

    @Test
    void retry_shouldThrow_whenDocumentIsNotFailed() {
        UUID documentId = UUID.randomUUID();

        DocumentWorkflow workflow = DocumentWorkflow.builder()
                .documentId(documentId)
                .currentStep(ProcessingStep.OCR_PROCESSING)
                .retryCount(0)
                .build();

        when(documentWorkflowRepository.findById(documentId)).thenReturn(Optional.of(workflow));

        InvalidTransitionException ex = assertThrows(
                InvalidTransitionException.class,
                () -> workflowService.retry(documentId)
        );

        assertEquals("Retry allowed only for FAILED documents", ex.getMessage());
    }

    @Test
    void cancel_shouldThrow_whenDocumentIsAlreadyCompleted() {
        UUID documentId = UUID.randomUUID();

        DocumentWorkflow workflow = DocumentWorkflow.builder()
                .documentId(documentId)
                .currentStep(ProcessingStep.COMPLETED)
                .build();

        when(documentWorkflowRepository.findById(documentId)).thenReturn(Optional.of(workflow));

        InvalidTransitionException ex = assertThrows(
                InvalidTransitionException.class,
                () -> workflowService.cancel(documentId)
        );

        assertEquals("Cannot cancel a terminal state", ex.getMessage());
    }

    @Test
    void cancel_shouldThrow_whenDocumentIsAlreadyFailed() {
        UUID documentId = UUID.randomUUID();

        DocumentWorkflow workflow = DocumentWorkflow.builder()
                .documentId(documentId)
                .currentStep(ProcessingStep.FAILED)
                .failureReason("DOCUMENT_TOO_LARGE")
                .build();

        when(documentWorkflowRepository.findById(documentId)).thenReturn(Optional.of(workflow));

        InvalidTransitionException ex = assertThrows(
                InvalidTransitionException.class,
                () -> workflowService.cancel(documentId)
        );

        assertEquals("Cannot cancel a terminal state", ex.getMessage());
    }

    @Test
    void updateStep_shouldThrow_whenBackwardTransitionIsRequested() {
        UUID documentId = UUID.randomUUID();

        DocumentWorkflow workflow = DocumentWorkflow.builder()
                .documentId(documentId)
                .currentStep(ProcessingStep.CLASSIFYING)
                .build();

        when(documentWorkflowRepository.findById(documentId)).thenReturn(Optional.of(workflow));

        assertThrows(
                InvalidTransitionException.class,
                () -> workflowService.updateStep(documentId, ProcessingStep.OCR_PROCESSING)
        );
    }
}
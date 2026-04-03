package com.example.docprocessing.service;

import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.domain.StepResult;
import com.example.docprocessing.domain.StepStatus;
import com.example.docprocessing.exception.StepResultNotFoundException;
import com.example.docprocessing.repository.StepResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StepResultServiceTest {

    @Mock
    private StepResultRepository stepResultRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private StepResultService stepResultService;

    @Test
    void getPersistedBase64Content_shouldThrow_whenResultJsonIsNull() {
        UUID documentId = UUID.randomUUID();

        StepResult stepResult = StepResult.builder()
                .documentId(documentId)
                .step(ProcessingStep.DMS_FETCHING)
                .status(StepStatus.COMPLETED)
                .resultJson(null)
                .build();

        when(stepResultRepository.findByDocumentIdAndStep(documentId, ProcessingStep.DMS_FETCHING))
                .thenReturn(Optional.of(stepResult));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> stepResultService.getPersistedBase64Content(documentId)
        );

        assertEquals("DMS fetch result is empty", ex.getMessage());
    }

    @Test
    void getPersistedBase64Content_shouldThrow_whenStepResultDoesNotExist() {
        UUID documentId = UUID.randomUUID();

        when(stepResultRepository.findByDocumentIdAndStep(documentId, ProcessingStep.DMS_FETCHING))
                .thenReturn(Optional.empty());

        assertThrows(
                StepResultNotFoundException.class,
                () -> stepResultService.getPersistedBase64Content(documentId)
        );
    }

    @Test
    void failStep_shouldKeepStartedAtAndCalculateDuration() {
        UUID documentId = UUID.randomUUID();
        LocalDateTime startedAt = LocalDateTime.now().minusSeconds(2);

        StepResult existing = StepResult.builder()
                .documentId(documentId)
                .step(ProcessingStep.NER_PROCESSING)
                .status(StepStatus.PROCESSING)
                .startedAt(startedAt)
                .resultJson("{\"old\":true}")
                .build();

        when(stepResultRepository.findByDocumentIdAndStep(documentId, ProcessingStep.NER_PROCESSING))
                .thenReturn(Optional.of(existing));
        when(stepResultRepository.save(existing)).thenReturn(existing);

        StepResult result = stepResultService.failStep(documentId, ProcessingStep.NER_PROCESSING, "CANCELLED_BY_USER");

        assertEquals(StepStatus.FAILED, result.getStatus());
        assertEquals("CANCELLED_BY_USER", result.getErrorMessage());
        assertEquals(startedAt, result.getStartedAt());
        assertNotNull(result.getCompletedAt());
        assertNotNull(result.getDurationMs());
        assertTrue(result.getDurationMs() >= 0);
    }
}
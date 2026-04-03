package com.example.docprocessing.controller;

import com.example.docprocessing.domain.DocumentWorkflow;
import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.domain.StepResult;
import com.example.docprocessing.dto.DocumentDetailResponse;
import com.example.docprocessing.dto.DocumentListResponse;
import com.example.docprocessing.dto.StepResultResponse;
import com.example.docprocessing.dto.SubmitDocumentRequest;
import com.example.docprocessing.dto.SubmitDocumentResponse;
import com.example.docprocessing.mapper.DocumentMapper;
import com.example.docprocessing.service.StepResultService;
import com.example.docprocessing.service.WorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final WorkflowService workflowService;
    private final StepResultService stepResultService;
    private final DocumentMapper documentMapper;
    private final ObjectMapper objectMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SubmitDocumentResponse submitDocument(@Valid @RequestBody SubmitDocumentRequest request) {
        DocumentWorkflow workflow = workflowService.createWorkflow(request.getDocRef());

        // process submit
        return SubmitDocumentResponse.builder()
                .documentId(workflow.getDocumentId())
                .docRef(workflow.getDocRef())
                .requestUid(workflow.getRequestUid())
                .currentStep(workflow.getCurrentStep())
                .createdAt(workflow.getCreatedAt())
                .build();
    }

    @GetMapping("/{documentId}")
    public DocumentDetailResponse getDocumentById(@PathVariable UUID documentId) {
        DocumentWorkflow workflow = workflowService.getById(documentId);
        List<StepResult> stepResults = stepResultService.getStepResults(documentId);

        return documentMapper.toDetailResponse(workflow, stepResults);
    }

    @GetMapping("/{documentId}/steps/{stepName}")
    public StepResultResponse getStepResult(@PathVariable UUID documentId, @PathVariable String stepName) {
        ProcessingStep step = ProcessingStep.getStepFromValue(stepName);
        StepResult stepResult = stepResultService.getStepResult(documentId, step);

        Object parsedResult = null;
        if (stepResult.getResultJson() != null) {
            try {
                parsedResult = objectMapper.readValue(stepResult.getResultJson(), Object.class);
            } catch (Exception ignored) {
            }
        }

        return StepResultResponse.builder()
                .status(stepResult.getStatus())
                .startedAt(stepResult.getStartedAt())
                .completedAt(stepResult.getCompletedAt())
                .durationMs(stepResult.getDurationMs())
                .result(parsedResult)
                .errorMessage(stepResult.getErrorMessage())
                .build();
    }

    @GetMapping
    public List<DocumentListResponse> listDocuments(@RequestParam(required = false) ProcessingStep status) {
        List<DocumentWorkflow> workflows;

        if (status == null) {
            workflows = workflowService.listAll();
        } else {
            workflows = workflowService.listByStatus(status);
        }

        return workflows.stream()
                .map(documentMapper::toListResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/{documentId}/retry")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> retry(@PathVariable UUID documentId) {
        DocumentWorkflow workflow = workflowService.retry(documentId);

        // resume job
        return Map.of(
                "documentId", workflow.getDocumentId(),
                "restartedFromStep", workflow.getCurrentStep(),
                "currentStep", workflow.getCurrentStep(),
                "retryCount", workflow.getRetryCount()
        );
    }

    @PostMapping("/{documentId}/cancel")
    public Map<String, Object> cancel(@PathVariable UUID documentId) {
        DocumentWorkflow workflow = workflowService.cancel(documentId);

        return Map.of(
                "documentId", workflow.getDocumentId(),
                "previousStep", workflow.getFailedAtStep(),
                "currentStep", workflow.getCurrentStep(),
                "reason", workflow.getFailureReason(),
                "cancelledAt", workflow.getUpdatedAt()
        );
    }
}
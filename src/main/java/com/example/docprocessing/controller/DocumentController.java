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
import com.example.docprocessing.pipeline.PipelineOrchestrator;
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
    private final PipelineOrchestrator pipelineOrchestrator;
    private final DocumentMapper documentMapper;
    private final ObjectMapper objectMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SubmitDocumentResponse submitDocument(@Valid @RequestBody SubmitDocumentRequest request) {
        DocumentWorkflow workflow = workflowService.createWorkflow(request.getDocRef());

        pipelineOrchestrator.startProcessing(workflow.getDocumentId());

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
}
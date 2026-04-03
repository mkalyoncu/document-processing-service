package com.example.docprocessing.pipeline;

import com.example.docprocessing.domain.DocumentWorkflow;
import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.exception.InvalidTransitionException;
import com.example.docprocessing.exception.ProcessingAbortedException;
import com.example.docprocessing.pipeline.command.PipelineCommand;
import com.example.docprocessing.pipeline.context.PipelineContext;
import com.example.docprocessing.service.StepResultService;
import com.example.docprocessing.service.WorkflowLogService;
import com.example.docprocessing.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineOrchestrator {

    private final WorkflowService workflowService;
    private final StepResultService stepResultService;
    private final WorkflowLogService workflowLogService;
    private final List<PipelineCommand> pipelineCommands;

    @Async("pipelineExecutor")
    public void startProcessing(UUID documentId) {
        log.info("Starting pipeline for documentId={}", documentId);

        try {
            DocumentWorkflow workflow = workflowService.getById(documentId);

            PipelineContext context = PipelineContext.builder()
                    .workflow(workflow)
                    .build();

            runCommands(context, pipelineCommands);
            completeWorkflow(documentId);

            log.info("Pipeline completed for documentId={}", documentId);
        } catch (ProcessingAbortedException e) {
            log.info("Pipeline aborted for documentId={}, reason={}", documentId, e.getMessage());
        } catch (Exception e) {
            log.warn("Pipeline failed for documentId={}, error={}", documentId, e.getMessage(), e);
        }
    }

    private void completeWorkflow(UUID documentId) {
    }

    private void runCommands(PipelineContext context, List<PipelineCommand> pipelineCommands) {
        
    }
}
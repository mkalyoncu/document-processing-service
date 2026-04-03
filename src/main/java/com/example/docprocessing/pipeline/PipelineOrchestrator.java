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

    @Async("pipelineExecutor")
    public void resumeProcessing(UUID documentId) {
        log.info("Resuming pipeline for documentId={}", documentId);

        try {
            DocumentWorkflow workflow = workflowService.getById(documentId);

            PipelineContext context = PipelineContext.builder()
                    .workflow(workflow)
                    .base64Content(loadBase64IfNeeded(workflow))
                    .build();

            List<PipelineCommand> remainingCommands = getRemainingCommands(workflow.getCurrentStep());

            runCommands(context, remainingCommands);
            completeWorkflow(documentId);

            log.info("Pipeline resumed and completed for documentId={}", documentId);
        } catch (ProcessingAbortedException e) {
            log.info("Pipeline aborted for documentId={}, reason={}", documentId, e.getMessage());
        } catch (Exception e) {
            log.warn("Retry failed for documentId={}, error={}", documentId, e.getMessage(), e);
        }
    }

    private void runCommands(PipelineContext context, List<PipelineCommand> commands) {
        for (PipelineCommand command : commands) {
            ensureNotCancelledOrTerminal(context.getWorkflow().getDocumentId());
            command.execute(context);
            context.setWorkflow(workflowService.getById(context.getWorkflow().getDocumentId()));
        }
    }

    private List<PipelineCommand> getRemainingCommands(ProcessingStep currentStep) {
        int startIndex = findStartIndex(currentStep);

        return pipelineCommands.subList(startIndex, pipelineCommands.size());
    }

    private int findStartIndex(ProcessingStep currentStep) {
        for (int i = 0; i < pipelineCommands.size(); i++) {
            if (pipelineCommands.get(i).processingStep() == currentStep) {
                return i;
            }
        }

        throw new IllegalStateException("No pipeline command found for step: " + currentStep);
    }

    private String loadBase64IfNeeded(DocumentWorkflow workflow) {
        if (workflow.getCurrentStep() == ProcessingStep.DMS_FETCHING) {
            return null;
        }

        return stepResultService.getPersistedBase64Content(workflow.getDocumentId());
    }

    private void completeWorkflow(UUID documentId) {
        DocumentWorkflow workflowBefore = workflowService.getById(documentId);
        ProcessingStep previousStep = workflowBefore.getCurrentStep();

        workflowService.updateStep(documentId, ProcessingStep.COMPLETED);

        DocumentWorkflow workflowAfter = workflowService.getById(documentId);
        workflowLogService.logTransition(workflowAfter, previousStep, ProcessingStep.COMPLETED, null);
    }

    private void ensureNotCancelledOrTerminal(UUID documentId) {
        DocumentWorkflow workflow = workflowService.getById(documentId);

        if (workflow.getCurrentStep() == ProcessingStep.FAILED ||
                workflow.getCurrentStep() == ProcessingStep.COMPLETED) {
            throw new ProcessingAbortedException("Workflow is already terminal: " + workflow.getCurrentStep());
        }
    }
}
package com.example.docprocessing.pipeline.command;

import com.example.docprocessing.domain.DocumentWorkflow;
import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.domain.StepResult;
import com.example.docprocessing.exception.ProcessingAbortedException;
import com.example.docprocessing.pipeline.context.PipelineContext;
import com.example.docprocessing.service.StepResultService;
import com.example.docprocessing.service.WorkflowLogService;
import com.example.docprocessing.service.WorkflowService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractPipelineCommand implements PipelineCommand {

    protected static final String CANCELLED_BY_USER = "CANCELLED_BY_USER";

    protected final WorkflowService workflowService;
    protected final StepResultService stepResultService;
    protected final WorkflowLogService workflowLogService;

    @Override
    public void execute(PipelineContext context) {
        DocumentWorkflow workflow = workflowService.getById(context.getWorkflow().getDocumentId());
        ProcessingStep previousStep = workflow.getCurrentStep();

        workflowService.updateStep(workflow.getDocumentId(), processingStep());
        stepResultService.startStep(workflow.getDocumentId(), processingStep());

        workflowLogService.logTransition(
                workflowService.getById(workflow.getDocumentId()),
                previousStep,
                processingStep(),
                null
        );

        try {
            Object result = doExecute(context);

            StepResult completedStepResult = stepResultService.completeStep(
                    workflow.getDocumentId(),
                    processingStep(),
                    result
            );

            afterSuccess(context, result);

            workflowService.updateStep(workflow.getDocumentId(), completedStep());

            workflowLogService.logTransition(
                    workflowService.getById(workflow.getDocumentId()),
                    processingStep(),
                    completedStep(),
                    completedStepResult.getDurationMs()
            );
        } catch (ProcessingAbortedException e) {
            stepResultService.failStep(
                    workflow.getDocumentId(),
                    processingStep(),
                    CANCELLED_BY_USER
            );
            throw e;
        } catch (Exception e) {
            if (isCancelled(workflow.getDocumentId())) {
                stepResultService.failStep(
                        workflow.getDocumentId(),
                        processingStep(),
                        CANCELLED_BY_USER);
                throw new ProcessingAbortedException(CANCELLED_BY_USER);
            }

            stepResultService.failStep(
                    workflow.getDocumentId(),
                    processingStep(),
                    e.getMessage());

            workflowService.markFailed(
                    workflow.getDocumentId(),
                    failedStep(),
                    e.getMessage());

            workflowLogService.logFailure(
                    workflowService.getById(workflow.getDocumentId()),
                    failedStep(),
                    e.getMessage());
            throw e;
        }
    }

    protected abstract Object doExecute(PipelineContext context);

    protected void afterSuccess(PipelineContext context, Object result) {
    }

    private boolean isCancelled(java.util.UUID documentId) {
        DocumentWorkflow latestWorkflow = workflowService.getById(documentId);

        return latestWorkflow.getCurrentStep() == ProcessingStep.FAILED
                && CANCELLED_BY_USER.equals(latestWorkflow.getFailureReason());
    }
}
package com.example.docprocessing.pipeline.command;

import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.pipeline.NerService;
import com.example.docprocessing.pipeline.context.PipelineContext;
import com.example.docprocessing.pipeline.model.NerResult;
import com.example.docprocessing.service.StepResultService;
import com.example.docprocessing.service.WorkflowLogService;
import com.example.docprocessing.service.WorkflowService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class NerCommand extends AbstractPipelineCommand {

    private final NerService nerService;

    public NerCommand(WorkflowService workflowService,
                      StepResultService stepResultService,
                      WorkflowLogService workflowLogService,
                      NerService nerService) {
        super(workflowService, stepResultService, workflowLogService);
        this.nerService = nerService;
    }

    @Override
    public ProcessingStep processingStep() {
        return ProcessingStep.NER_PROCESSING;
    }

    @Override
    public ProcessingStep completedStep() {
        return ProcessingStep.NER_COMPLETED;
    }

    @Override
    public ProcessingStep failedStep() {
        return ProcessingStep.NER_FAILED;
    }

    @Override
    protected Object doExecute(PipelineContext context) {
        NerResult result = nerService.extractEntities(
                context.getBase64Content(),
                context.getWorkflow().getRequestUid()
        );

        context.setNerResult(result);
        return result;
    }
}
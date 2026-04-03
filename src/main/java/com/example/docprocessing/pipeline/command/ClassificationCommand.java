package com.example.docprocessing.pipeline.command;

import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.pipeline.ClassifierService;
import com.example.docprocessing.pipeline.context.PipelineContext;
import com.example.docprocessing.pipeline.model.ClassificationResult;
import com.example.docprocessing.service.StepResultService;
import com.example.docprocessing.service.WorkflowLogService;
import com.example.docprocessing.service.WorkflowService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class ClassificationCommand extends AbstractPipelineCommand {

    private final ClassifierService classifierService;

    public ClassificationCommand(WorkflowService workflowService,
                                 StepResultService stepResultService,
                                 WorkflowLogService workflowLogService,
                                 ClassifierService classifierService) {
        super(workflowService, stepResultService, workflowLogService);
        this.classifierService = classifierService;
    }

    @Override
    public ProcessingStep processingStep() {
        return ProcessingStep.CLASSIFYING;
    }

    @Override
    public ProcessingStep completedStep() {
        return ProcessingStep.CLASSIFICATION_COMPLETED;
    }

    @Override
    public ProcessingStep failedStep() {
        return ProcessingStep.CLASSIFICATION_FAILED;
    }

}
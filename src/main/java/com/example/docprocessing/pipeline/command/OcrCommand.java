package com.example.docprocessing.pipeline.command;

import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.pipeline.OcrService;
import com.example.docprocessing.pipeline.context.PipelineContext;
import com.example.docprocessing.pipeline.model.OcrResult;
import com.example.docprocessing.service.StepResultService;
import com.example.docprocessing.service.WorkflowLogService;
import com.example.docprocessing.service.WorkflowService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class OcrCommand extends AbstractPipelineCommand {

    private final OcrService ocrService;

    public OcrCommand(WorkflowService workflowService,
                      StepResultService stepResultService,
                      WorkflowLogService workflowLogService,
                      OcrService ocrService) {
        super(workflowService, stepResultService, workflowLogService);
        this.ocrService = ocrService;
    }

    @Override
    public ProcessingStep processingStep() {
        return ProcessingStep.OCR_PROCESSING;
    }

    @Override
    public ProcessingStep completedStep() {
        return ProcessingStep.OCR_COMPLETED;
    }

    @Override
    public ProcessingStep failedStep() {
        return ProcessingStep.OCR_FAILED;
    }

}
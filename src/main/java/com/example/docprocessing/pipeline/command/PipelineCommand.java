package com.example.docprocessing.pipeline.command;

import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.pipeline.context.PipelineContext;

public interface PipelineCommand {

    ProcessingStep processingStep();

    ProcessingStep completedStep();

    ProcessingStep failedStep();

    void execute(PipelineContext context);
}
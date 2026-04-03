package com.example.docprocessing.pipeline.command;

import com.example.docprocessing.pipeline.context.PipelineContext;

import java.util.UUID;

public abstract class AbstractPipelineCommand implements PipelineCommand {

    @Override
    public void execute(PipelineContext context) {
        context.run();
    }

    private boolean isCancelled(UUID documentId) {
        return false; // todo: will fill later
    }
}
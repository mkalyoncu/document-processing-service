package com.example.docprocessing.pipeline.command;

import com.example.docprocessing.domain.ProcessingStep;
import com.example.docprocessing.exception.DocumentTooLargeException;
import com.example.docprocessing.pipeline.DmsClient;
import com.example.docprocessing.pipeline.context.PipelineContext;
import com.example.docprocessing.pipeline.model.DmsDocumentMetadata;
import com.example.docprocessing.pipeline.model.DmsFetchResult;
import com.example.docprocessing.service.StepResultService;
import com.example.docprocessing.service.WorkflowLogService;
import com.example.docprocessing.service.WorkflowService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DmsFetchCommand extends AbstractPipelineCommand {

    private static final long MAX_DOCUMENT_SIZE_BYTES = 20L * 1024 * 1024;

    private final DmsClient dmsClient;

    public DmsFetchCommand(WorkflowService workflowService,
                           StepResultService stepResultService,
                           WorkflowLogService workflowLogService,
                           DmsClient dmsClient) {
        super(workflowService, stepResultService, workflowLogService);
        this.dmsClient = dmsClient;
    }

    @Override
    public ProcessingStep processingStep() {
        return ProcessingStep.DMS_FETCHING;
    }

    @Override
    public ProcessingStep completedStep() {
        return ProcessingStep.DMS_FETCH_COMPLETED;
    }

    @Override
    public ProcessingStep failedStep() {
        return ProcessingStep.DMS_FETCH_FAILED;
    }

    @Override
    protected Object doExecute(PipelineContext context) {
        DmsDocumentMetadata metadata = dmsClient.getMetadata(context.getWorkflow().getDocRef());

        if (metadata.getSizeBytes() > MAX_DOCUMENT_SIZE_BYTES) {
            throw new DocumentTooLargeException("DOCUMENT_TOO_LARGE");
        }

        String base64Content = dmsClient.getContentAsBase64(context.getWorkflow().getDocRef());

        context.setDmsMetadata(metadata);
        context.setBase64Content(base64Content);

        return new DmsFetchResult(
                metadata.getFileName(),
                metadata.getContentType(),
                metadata.getSizeBytes(),
                base64Content
        );
    }

    @Override
    protected void afterSuccess(PipelineContext context, Object result) {
        DmsDocumentMetadata metadata = context.getDmsMetadata();
        workflowService.updateDocumentMetadata(
                context.getWorkflow().getDocumentId(),
                metadata.getFileName(),
                metadata.getSizeBytes()
        );
    }
}
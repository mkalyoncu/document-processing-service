package com.example.docprocessing.pipeline.command;

import com.example.docprocessing.domain.DocumentWorkflow;
import com.example.docprocessing.exception.DocumentTooLargeException;
import com.example.docprocessing.pipeline.context.PipelineContext;
import com.example.docprocessing.pipeline.model.DmsDocumentMetadata;
import com.example.docprocessing.pipeline.simulated.SimulatedDmsClient;
import com.example.docprocessing.service.StepResultService;
import com.example.docprocessing.service.WorkflowLogService;
import com.example.docprocessing.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DmsFetchCommandTest {

    @Mock
    private WorkflowService workflowService;

    @Mock
    private StepResultService stepResultService;

    @Mock
    private WorkflowLogService workflowLogService;

    @Mock
    private SimulatedDmsClient dmsClient;

    @InjectMocks
    private DmsFetchCommand dmsFetchCommand;

    @Test
    void doExecute_shouldThrowDocumentTooLargeException_whenMetadataExceeds20Mb() {
        UUID docRef = UUID.randomUUID();

        DocumentWorkflow workflow = DocumentWorkflow.builder()
                .documentId(UUID.randomUUID())
                .docRef(docRef)
                .build();

        PipelineContext context = PipelineContext.builder()
                .workflow(workflow)
                .build();

        when(dmsClient.getMetadata(docRef)).thenReturn(
                new DmsDocumentMetadata("large-file.pdf", "application/pdf", 25L * 1024 * 1024)
        );

        assertThrows(DocumentTooLargeException.class, () -> dmsFetchCommand.doExecute(context));

        verify(dmsClient, times(1)).getMetadata(docRef);
        verify(dmsClient, never()).getContentAsBase64(any());
    }
}

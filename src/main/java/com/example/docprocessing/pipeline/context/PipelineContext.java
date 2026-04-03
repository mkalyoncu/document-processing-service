package com.example.docprocessing.pipeline.context;

import com.example.docprocessing.domain.DocumentWorkflow;
import com.example.docprocessing.pipeline.model.ClassificationResult;
import com.example.docprocessing.pipeline.model.DmsDocumentMetadata;
import com.example.docprocessing.pipeline.model.NerResult;
import com.example.docprocessing.pipeline.model.OcrResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineContext {

    private DocumentWorkflow workflow;
    private String base64Content;
    private DmsDocumentMetadata dmsMetadata;
    private OcrResult ocrResult;
    private ClassificationResult classificationResult;
    private NerResult nerResult;
}
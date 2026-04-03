package com.example.docprocessing.pipeline;

import com.example.docprocessing.pipeline.model.DmsDocumentMetadata;

import java.util.UUID;

public interface DmsClient {

    DmsDocumentMetadata getMetadataFromRestClient(UUID docRef);

    DmsDocumentMetadata getMetadata(UUID docRef);

    String getContentAsBase64(UUID docRef);
}
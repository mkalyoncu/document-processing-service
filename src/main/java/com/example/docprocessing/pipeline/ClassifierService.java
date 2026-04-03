package com.example.docprocessing.pipeline;

import com.example.docprocessing.pipeline.model.ClassificationResult;

import java.util.UUID;

public interface ClassifierService {

    ClassificationResult classify(String base64Content, UUID requestUid);
}
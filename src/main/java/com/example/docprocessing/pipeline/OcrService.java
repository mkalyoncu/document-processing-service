package com.example.docprocessing.pipeline;

import com.example.docprocessing.pipeline.model.OcrResult;

import java.util.UUID;

public interface OcrService {

    OcrResult process(String base64Content, UUID requestUid);
}
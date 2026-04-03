package com.example.docprocessing.pipeline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResult {

    private String rawText;
    private int pageCount;
    private int wordCount;
    private double confidence;
}
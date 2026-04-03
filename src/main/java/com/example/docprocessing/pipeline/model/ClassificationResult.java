package com.example.docprocessing.pipeline.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassificationResult {

    private String documentType;
    private double confidence;
    private List<AlternativeType> alternativeTypes;
}
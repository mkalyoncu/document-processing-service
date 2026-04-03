package com.example.docprocessing.pipeline.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlternativeType {

    private String type;
    private double confidence;
}
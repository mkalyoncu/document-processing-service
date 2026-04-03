package com.example.docprocessing.pipeline.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NamedEntity {

    private String type;
    private String value;
    private double confidence;
}
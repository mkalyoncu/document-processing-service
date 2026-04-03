package com.example.docprocessing.pipeline.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DmsFetchResult {

    private String fileName;
    private String contentType;
    private long sizeBytes;
    private String base64Content;
}
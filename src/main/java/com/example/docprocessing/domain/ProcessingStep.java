package com.example.docprocessing.domain;

public enum ProcessingStep {

    RECEIVED,

    DMS_FETCHING,
    DMS_FETCH_COMPLETED,
    DMS_FETCH_FAILED,

    OCR_PROCESSING,
    OCR_COMPLETED,
    OCR_FAILED,

    CLASSIFYING,
    CLASSIFICATION_COMPLETED,
    CLASSIFICATION_FAILED,

    NER_PROCESSING,
    NER_COMPLETED,
    NER_FAILED,

    COMPLETED,
    FAILED;

    public static ProcessingStep getStepFromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Step name cannot be null");
        }

        return switch (value.toLowerCase()) {
            case "dms-fetch" -> DMS_FETCHING;
            case "ocr" -> OCR_PROCESSING;
            case "classification" -> CLASSIFYING;
            case "ner" -> NER_PROCESSING;
            default -> throw new IllegalArgumentException("Invalid step name: " + value);
        };
    }
}
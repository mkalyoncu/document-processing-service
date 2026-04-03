package com.example.docprocessing.exception;

public class StepResultNotFoundException extends RuntimeException {

    public StepResultNotFoundException(String message) {
        super(message);
    }
}
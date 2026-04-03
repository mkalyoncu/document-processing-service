package com.example.docprocessing.exception;

public class ProcessingAbortedException extends RuntimeException {

    public ProcessingAbortedException(String message) {
        super(message);
    }
}
package com.example.docprocessing.exception;

public class DocumentTooLargeException extends RuntimeException {

    public DocumentTooLargeException(String message) {
        super(message);
    }
}
package com.example.docprocessing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SubmitDocumentRequest {

        @NotNull
        private UUID docRef;
}
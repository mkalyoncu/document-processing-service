package com.example.docprocessing.pipeline.simulated;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class SimulationSupport {

    private SimulationSupport() {
    }

    protected static void randomDelay(int minMs, int maxMs) {
        int delay = ThreadLocalRandom.current().nextInt(minMs, maxMs + 1);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted during simulated delay", e);
        }
    }

    protected static boolean shouldFail(double failureRate) {
        return ThreadLocalRandom.current().nextDouble() < failureRate;
    }

    protected static String getDocumentContent(UUID docRef) {
        int bucket = Math.abs(docRef.hashCode()) % 4;

        return switch (bucket) {
            case 0 -> """
                    Invoice #INV-2026-0342
                    Date: 2026-01-15
                    Bill To: Acme Corp
                    Amount: $12,450.00
                    Contact: John Smith
                    """;
            case 1 -> """
                    Service Agreement
                    Effective Date: 2026-02-01
                    Parties: Acme Corp and Beta Ltd
                    Term: 12 months
                    Authorized Signatory: Jane Doe
                    """;
            case 2 -> """
                    Medical Report
                    Patient: Alice Brown
                    Date: 2026-01-18
                    Diagnosis: Mild anemia
                    Physician: Dr. Michael Reed
                    Hospital: City Health Center
                    """;
            default -> """
                    Quarterly Financial Statement
                    Company: Delta Holdings
                    Period: Q1 2026
                    Revenue: $3,240,000
                    Net Profit: $420,000
                    Prepared By: Finance Department
                    """;
        };
    }
}
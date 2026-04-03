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
}
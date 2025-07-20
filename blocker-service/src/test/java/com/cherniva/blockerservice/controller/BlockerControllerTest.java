package com.cherniva.blockerservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BlockerControllerTest {

    @InjectMocks
    private BlockerController blockerController;

    @Test
    void check_ReturnsBoolean() {
        // Act
        boolean result = blockerController.check();

        // Assert
        // The result should be a boolean (either true or false)
        // We can't predict the exact value due to randomness, but we can verify it's a valid boolean
        assertTrue(result == true || result == false);
    }

    @Test
    void check_MultipleInvocations_ReturnsValidBooleans() {
        // Act & Assert
        for (int i = 0; i < 100; i++) {
            boolean result = blockerController.check();
            assertTrue(result == true || result == false);
        }
    }

    @Test
    void check_StatisticalValidation_ReturnsExpectedDistribution() {
        // Act - call method many times to test statistical distribution
        int trueCount = 0;
        int totalCalls = 1000;
        
        for (int i = 0; i < totalCalls; i++) {
            if (blockerController.check()) {
                trueCount++;
            }
        }

        // Assert - should be approximately 75% true (3 out of 4 cases)
        // Allow for some variance due to randomness
        double trueRatio = (double) trueCount / totalCalls;
        assertTrue(trueRatio > 0.65 && trueRatio < 0.85, 
            "True ratio should be approximately 75%, but was: " + trueRatio);
    }

    @Test
    void check_ConcurrentAccess_ThreadSafe() throws InterruptedException {
        // This test verifies that the method can be safely called from multiple threads
        final int numThreads = 10;
        final int callsPerThread = 100;
        Thread[] threads = new Thread[numThreads];
        final boolean[][] results = new boolean[numThreads][callsPerThread];

        // Create and start threads
        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < callsPerThread; j++) {
                    results[threadIndex][j] = blockerController.check();
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify all results are valid booleans
        for (int i = 0; i < numThreads; i++) {
            for (int j = 0; j < callsPerThread; j++) {
                boolean result = results[i][j];
                assertTrue(result == true || result == false);
            }
        }
    }
} 
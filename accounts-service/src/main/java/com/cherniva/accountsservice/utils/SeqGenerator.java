package com.cherniva.accountsservice.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SeqGenerator {

    private static final ConcurrentHashMap<String, AtomicLong> sequences = new ConcurrentHashMap<>();

    // Initialize sequences with starting values
    static {
        sequences.put("user_details", new AtomicLong(1));
        sequences.put("account", new AtomicLong(1));
    }

    /**
     * Gets the next value for a given sequence.
     *
     * @param sequenceName The name of the sequence (e.g., "user_details").
     * @return The next sequence value.
     */
    public static long getNextValue(String sequenceName) {
        AtomicLong seq = sequences.computeIfAbsent(sequenceName, k -> new AtomicLong(1));
        return seq.getAndIncrement();
    }

    /**
     * (Optional) Set the starting value of a sequence.
     */
    public static void setInitialValue(String sequenceName, long startValue) {
        sequences.put(sequenceName, new AtomicLong(startValue));
    }

    public static long getNextAccount() {
        AtomicLong seq = sequences.computeIfAbsent("account", k -> new AtomicLong(1));
        return seq.getAndIncrement();
    }

    public static long getNextUserDetails() {
        AtomicLong seq = sequences.computeIfAbsent("user_details", k -> new AtomicLong(1));
        return seq.getAndIncrement();
    }
}

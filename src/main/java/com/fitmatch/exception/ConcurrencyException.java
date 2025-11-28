// ================== ConcurrencyException.java ==================
package com.fitmatch.exception;

/**
 * Exception thrown when concurrent processing encounters issues.
 * Wraps thread-related exceptions for cleaner handling.
 */
public class ConcurrencyException extends RuntimeException {

    private final String threadName;

    public ConcurrencyException(String message) {
        super(message);
        this.threadName = Thread.currentThread().getName();
    }

    public ConcurrencyException(String message, Throwable cause) {
        super(message, cause);
        this.threadName = Thread.currentThread().getName();
    }

    public ConcurrencyException(String message, String threadName, Throwable cause) {
        super(message, cause);
        this.threadName = threadName;
    }

    public String getThreadName() { return threadName; }

    @Override
    public String toString() {
        return String.format("ConcurrencyException: %s [Thread: %s]",
                getMessage(), threadName);
    }
}
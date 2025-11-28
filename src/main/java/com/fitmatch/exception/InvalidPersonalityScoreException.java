// ================== InvalidPersonalityScoreException.java ==================
package com.fitmatch.exception;

/**
 * Exception thrown when a personality score is outside the valid range (0-100).
 * Extends IllegalArgumentException for unchecked exception behavior.
 */
public class InvalidPersonalityScoreException extends IllegalArgumentException {

    private final int invalidScore;

    /**
     * Constructs exception with a message
     * @param message Descriptive error message
     */
    public InvalidPersonalityScoreException(String message) {
        super(message);
        this.invalidScore = -1;
    }

    /**
     * Constructs exception with message and the invalid score value
     * @param message Descriptive error message
     * @param invalidScore The score that caused the exception
     */
    public InvalidPersonalityScoreException(String message, int invalidScore) {
        super(message);
        this.invalidScore = invalidScore;
    }

    /**
     * Constructs exception with message and cause
     * @param message Descriptive error message
     * @param cause The underlying cause
     */
    public InvalidPersonalityScoreException(String message, Throwable cause) {
        super(message, cause);
        this.invalidScore = -1;
    }

    /**
     * Gets the invalid score that caused this exception
     * @return The invalid score, or -1 if not specified
     */
    public int getInvalidScore() {
        return invalidScore;
    }

    @Override
    public String toString() {
        if (invalidScore != -1) {
            return String.format("InvalidPersonalityScoreException: %s (Score: %d)",
                    getMessage(), invalidScore);
        }
        return super.toString();
    }
}
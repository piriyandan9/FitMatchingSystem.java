// ================== TeamFormationException.java ==================
package com.fitmatch.exception;

/**
 * Checked exception thrown when team formation process fails.
 * Includes issues like insufficient participants, algorithm failures, etc.
 */
public class TeamFormationException extends Exception {

    private final int requiredParticipants;
    private final int availableParticipants;

    public TeamFormationException(String message) {
        super(message);
        this.requiredParticipants = -1;
        this.availableParticipants = -1;
    }

    public TeamFormationException(String message, Throwable cause) {
        super(message, cause);
        this.requiredParticipants = -1;
        this.availableParticipants = -1;
    }

    /**
     * Constructor for participant count mismatch errors
     */
    public TeamFormationException(String message, int required, int available) {
        super(message);
        this.requiredParticipants = required;
        this.availableParticipants = available;
    }

    public int getRequiredParticipants() { return requiredParticipants; }
    public int getAvailableParticipants() { return availableParticipants; }

    @Override
    public String toString() {
        if (requiredParticipants > 0 && availableParticipants >= 0) {
            return String.format("TeamFormationException: %s (Required: %d, Available: %d)",
                    getMessage(), requiredParticipants, availableParticipants);
        }
        return super.toString();
    }
}
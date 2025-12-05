package com.fitmatch.exception;

/**
 * Exception thrown when an invalid playing role is specified.
 */
public class InvalidRoleException extends IllegalArgumentException {

    private final String invalidRole;

    public InvalidRoleException(String message) {
        super(message);
        this.invalidRole = null;
    }

    public InvalidRoleException(String message, String invalidRole) {
        super(message);
        this.invalidRole = invalidRole;
    }

    public String getInvalidRole() {
        return invalidRole;
    }

    @Override
    public String toString() {
        if (invalidRole != null) {
            return String.format("InvalidRoleException: %s (Role: %s)",
                    getMessage(), invalidRole);
        }
        return super.toString();
    }
}

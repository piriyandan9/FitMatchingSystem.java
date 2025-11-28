// ================== EmailFormatException.java ==================
package com.fitmatch.exception;

/**
 * Exception thrown when email format validation fails.
 */
public class EmailFormatException extends ValidationException {

    private final String invalidEmail;

    public EmailFormatException(String message) {
        super(message);
        this.invalidEmail = null;
    }

    public EmailFormatException(String message, String invalidEmail) {
        super(message, "Email", invalidEmail, "username@domain.com");
        this.invalidEmail = invalidEmail;
    }

    public String getInvalidEmail() {
        return invalidEmail;
    }

    @Override
    public String toString() {
        if (invalidEmail != null) {
            return String.format("EmailFormatException: %s (Email: %s)",
                    getMessage(), invalidEmail);
        }
        return super.toString();
    }
}
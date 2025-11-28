// ================== ValidationException.java ==================
package com.fitmatch.exception;

/**
 * Exception thrown when input validation fails.
 * Contains detailed information about which field failed and why.
 */
public class ValidationException extends Exception {

    private final String fieldName;
    private final String invalidValue;
    private final String expectedFormat;

    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.invalidValue = null;
        this.expectedFormat = null;
    }

    public ValidationException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = null;
        this.expectedFormat = null;
    }

    public ValidationException(String message, String fieldName, String invalidValue) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
        this.expectedFormat = null;
    }

    /**
     * Full constructor with all validation details
     */
    public ValidationException(String message, String fieldName,
                               String invalidValue, String expectedFormat) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
        this.expectedFormat = expectedFormat;
    }

    public String getFieldName() { return fieldName; }
    public String getInvalidValue() { return invalidValue; }
    public String getExpectedFormat() { return expectedFormat; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ValidationException: ");
        sb.append(getMessage());
        if (fieldName != null) {
            sb.append(" [Field: ").append(fieldName);
            if (invalidValue != null) {
                sb.append(", Value: '").append(invalidValue).append("'");
            }
            if (expectedFormat != null) {
                sb.append(", Expected: ").append(expectedFormat);
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
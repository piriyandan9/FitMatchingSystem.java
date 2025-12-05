package com.fitmatch.exception;

/**
 * Checked exception thrown when data loading from files fails.
 * This includes file not found, parsing errors, and validation failures.
 */
public class DataLoadException extends Exception {

    private final String filePath;
    private final int lineNumber;

    /**
     * Basic constructor with message only
     */
    public DataLoadException(String message) {
        super(message);
        this.filePath = null;
        this.lineNumber = -1;
    }

    /**
     * Constructor with message and cause
     */
    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
        this.filePath = null;
        this.lineNumber = -1;
    }

    /**
     * Constructor with file path information
     */
    public DataLoadException(String message, String filePath) {
        super(message);
        this.filePath = filePath;
        this.lineNumber = -1;
    }

    /**
     * Constructor with file path and line number for precise error reporting
     */
    public DataLoadException(String message, String filePath, int lineNumber) {
        super(message);
        this.filePath = filePath;
        this.lineNumber = lineNumber;
    }

    /**
     * Full constructor with all details
     */
    public DataLoadException(String message, String filePath, int lineNumber, Throwable cause) {
        super(message, cause);
        this.filePath = filePath;
        this.lineNumber = lineNumber;
    }

    public String getFilePath() { return filePath; }
    public int getLineNumber() { return lineNumber; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DataLoadException: ");
        sb.append(getMessage());
        if (filePath != null) {
            sb.append(" [File: ").append(filePath);
            if (lineNumber > 0) {
                sb.append(", Line: ").append(lineNumber);
            }
            sb.append("]");
        }
        return sb.toString();
    }
}

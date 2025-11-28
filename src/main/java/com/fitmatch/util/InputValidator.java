package com.fitmatch.util;

import com.fitmatch.exception.*;
import java.util.regex.Pattern;

/**
 * Utility class for validating various input types.
 */
public class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PARTICIPANT_ID_PATTERN = Pattern.compile(
            "^P\\d{3,}$"
    );

    private InputValidator() {
        // Utility class
    }

    /**
     * Validates email format and returns trimmed email
     */
    public static String validateEmail(String email) throws EmailFormatException {
        if (email == null || email.trim().isEmpty()) {
            throw new EmailFormatException("Email cannot be null or empty");
        }

        String trimmedEmail = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new EmailFormatException("Invalid email format", trimmedEmail);
        }

        return trimmedEmail;
    }

    /**
     * Validates participant ID format and returns trimmed ID
     */
    public static String validateParticipantId(String id) throws ValidationException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Participant ID cannot be null or empty", "ParticipantID");
        }

        String trimmedId = id.trim();
        if (!PARTICIPANT_ID_PATTERN.matcher(trimmedId).matches()) {
            throw new ValidationException(
                    "Participant ID must follow format P### (e.g., P001, P012)",
                    "ParticipantID",
                    trimmedId,
                    "P001, P002, etc."
            );
        }

        return trimmedId;
    }

    /**
     * Validates participant name and returns trimmed name
     */
    public static String validateName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name cannot be null or empty", "Name");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() < 2) {
            throw new ValidationException(
                    "Name must be at least 2 characters long",
                    "Name",
                    trimmedName
            );
        }

        if (trimmedName.length() > 100) {
            throw new ValidationException(
                    "Name cannot exceed 100 characters",
                    "Name",
                    trimmedName
            );
        }

        return trimmedName;
    }

    /**
     * Validates age range (16-80)
     */
    public static void validateAge(int age) throws ValidationException {
        if (age < 16 || age > 80) {
            throw new ValidationException(
                    String.format("Age must be between 16 and 80. Provided: %d", age),
                    "Age",
                    String.valueOf(age),
                    "16-80"
            );
        }
    }

    /**
     * Validates personality score (20-100 range for scaled survey)
     */
    public static void validatePersonalityScore(int score) throws InvalidPersonalityScoreException {
        if (score < 20 || score > 100) {
            throw new InvalidPersonalityScoreException(
                    String.format("Personality score must be between 20 and 100. Provided: %d", score),
                    score
            );
        }
    }

    /**
     * Validates skill level (1-10)
     */
    public static void validateSkillLevel(int skillLevel) throws ValidationException {
        if (skillLevel < 1 || skillLevel > 10) {
            throw new ValidationException(
                    String.format("Skill level must be between 1 and 10. Provided: %d", skillLevel),
                    "SkillLevel",
                    String.valueOf(skillLevel),
                    "1-10"
            );
        }
    }

    /**
     * Validates team size (min 3)
     */
    public static void validateTeamSize(int teamSize) throws TeamFormationException {
        if (teamSize < 3) {
            throw new TeamFormationException("Team size must be at least 3");
        }

        if (teamSize > 20) {
            throw new TeamFormationException("Team size cannot exceed 20");
        }
    }

    /**
     * Validates file path
     */
    public static void validateFilePath(String filePath) throws ValidationException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ValidationException("File path cannot be null or empty", "FilePath");
        }

        String trimmedPath = filePath.trim();
        if (!trimmedPath.endsWith(".csv")) {
            throw new ValidationException(
                    "File must be a CSV file (.csv extension)",
                    "FilePath",
                    trimmedPath,
                    "filename.csv"
            );
        }
    }

    /**
     * Checks if email format is valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Checks if participant ID format is valid
     */
    public static boolean isValidParticipantId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return PARTICIPANT_ID_PATTERN.matcher(id.trim()).matches();
    }
}
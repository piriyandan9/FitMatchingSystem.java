package com.fitmatch.model;

import com.fitmatch.service.PersonalityClassifier;
import com.fitmatch.util.InputValidator;
import com.fitmatch.exception.*;

/**
 * Represents a gaming club participant with personality assessment.
 * Version 2.1 - Updated for auto-personality detection
 */
public class Participant {

    // Core identification
    private String participantId;
    private String name;
    private String email;
    private int age;

    // Personality assessment
    private int personalityScore;
    private PersonalityType personalityType;

    // Gaming preferences
    private GameType preferredGame;
    private int skillLevel; // 1-10 scale
    private PlayingRole preferredRole;

    // Team assignment
    private String assignedTeam;

    /**
     * Constructor with personality score (auto-classified)
     */
    public Participant(String participantId, String name, String email, int age,
                       GameType preferredGame, int skillLevel,
                       PlayingRole preferredRole, int personalityScore)
            throws ValidationException, EmailFormatException {

        // Validate all inputs
        this.participantId = InputValidator.validateParticipantId(participantId);
        this.name = InputValidator.validateName(name);
        this.email = InputValidator.validateEmail(email).toLowerCase();
        InputValidator.validateAge(age);
        InputValidator.validatePersonalityScore(personalityScore);
        InputValidator.validateSkillLevel(skillLevel);

        if (preferredGame == null) {
            throw new ValidationException("Preferred game cannot be null", "PreferredGame");
        }

        if (preferredRole == null) {
            throw new ValidationException("Playing role cannot be null", "PreferredRole");
        }

        // Assign validated values
        this.participantId = participantId.trim();
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.age = age;
        this.preferredGame = preferredGame;
        this.skillLevel = skillLevel;
        this.preferredRole = preferredRole;
        this.assignedTeam = "Unassigned";

        // Auto-classify personality type from score
        this.personalityScore = personalityScore;
        this.personalityType = PersonalityClassifier.classifyPersonality(this.personalityScore);
    }

    // ==================== Getters ====================

    public String getParticipantId() {
        return participantId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public int getPersonalityScore() {
        return personalityScore;
    }

    public PersonalityType getPersonalityType() {
        return personalityType;
    }

    public GameType getPreferredGame() {
        return preferredGame;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public PlayingRole getPreferredRole() {
        return preferredRole;
    }

    public String getAssignedTeam() {
        return assignedTeam;
    }

    // ==================== Setters ====================

    public void setAssignedTeam(String teamName) {
        this.assignedTeam = teamName != null ? teamName : "Unassigned";
    }

    // ==================== Business Logic ====================

    public boolean isAssigned() {
        return !"Unassigned".equals(assignedTeam);
    }

    public double calculateSkillCompatibility(Participant other) {
        int skillDiff = Math.abs(this.skillLevel - other.skillLevel);

        if (skillDiff <= 1) return 1.0;
        if (skillDiff <= 2) return 0.8;
        if (skillDiff <= 3) return 0.6;
        if (skillDiff <= 4) return 0.4;
        return 0.2;
    }

    public boolean hasSameGameInterest(Participant other) {
        return this.preferredGame == other.preferredGame;
    }

    public boolean hasSameRole(Participant other) {
        return this.preferredRole == other.preferredRole;
    }

    public String toCSV() {
        return String.format("%s,%s,%s,%s,%d,%s,%d,%s",
                participantId,
                name,
                email,
                preferredGame.name(),
                skillLevel,
                preferredRole.name(),
                personalityScore,
                personalityType.name()
        );
    }

    @Override
    public String toString() {
        return String.format(
                "Participant[ID=%s, Name=%s, Email=%s, Age=%d, Personality=%s(%d), " +
                        "Game=%s, Skill=%d, Role=%s, Team=%s]",
                participantId, name, email, age, personalityType, personalityScore,
                preferredGame.getDisplayName(), skillLevel,
                preferredRole.getDisplayName(), assignedTeam
        );
    }

    public String getSummary() {
        return String.format(
                "%-6s | %-18s | %-8s | %-12s | Skill:%-2d | %-12s",
                participantId,
                name,
                personalityType.getDisplayName(),
                preferredGame.getDisplayName(),
                skillLevel,
                preferredRole.getDisplayName()
        );
    }
}
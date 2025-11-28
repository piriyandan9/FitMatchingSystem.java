package com.fitmatch.service;

import com.fitmatch.model.PersonalityType;
import com.fitmatch.exception.InvalidPersonalityScoreException;

/**
 * Service class for classifying personality types.
 *
 * Survey: 5 questions, each rated 1-5
 * Total: 5-25, scaled x4 to 20-100
 *
 * Classification:
 * - Leader: 90-100
 * - Balanced: 70-89
 * - Thinker: 50-69
 */
public class PersonalityClassifier {

    public static final int MIN_SCORE = 20;  // 5 questions x 1 x 4
    public static final int MAX_SCORE = 100; // 5 questions x 5 x 4
    public static final int LEADER_MIN = 90;
    public static final int BALANCED_MIN = 70;
    public static final int THINKER_MIN = 50;

    private PersonalityClassifier() {
        // Utility class
    }

    /**
     * Classifies personality type based on score
     *
     * @param score Personality score (20-100)
     * @return PersonalityType
     * @throws InvalidPersonalityScoreException if score invalid
     */
    public static PersonalityType classifyPersonality(int score)
            throws InvalidPersonalityScoreException {

        if (!isValidScore(score)) {
            throw new InvalidPersonalityScoreException(
                    String.format("Personality score must be between %d and %d. Received: %d",
                            MIN_SCORE, MAX_SCORE, score),
                    score
            );
        }

        // Check all personality types
        for (PersonalityType type : PersonalityType.values()) {
            if (type.matchesScore(score)) {
                return type;
            }
        }

        // Fallback to Thinker (lowest category)
        return PersonalityType.THINKER;
    }

    /**
     * Validates if score is within acceptable range (20-100)
     */
    public static boolean isValidScore(int score) {
        return score >= MIN_SCORE && score <= MAX_SCORE;
    }

    /**
     * Validates individual survey response (1-5)
     */
    public static boolean isValidResponse(int response) {
        return response >= 1 && response <= 5;
    }

    /**
     * Calculates total score from 5 survey responses (1-5 each)
     * Returns scaled score (x4) for 20-100 range
     */
    public static int calculateTotalScore(int[] responses) {
        if (responses == null || responses.length != 5) {
            throw new IllegalArgumentException(
                    "Exactly 5 personality responses required. Received: " +
                            (responses == null ? "null" : responses.length)
            );
        }

        int total = 0;
        for (int i = 0; i < responses.length; i++) {
            if (!isValidResponse(responses[i])) {
                throw new IllegalArgumentException(
                        String.format("Response %d is invalid: %d (must be 1-5)",
                                i + 1, responses[i])
                );
            }
            total += responses[i];
        }

        // Scale 5-25 to 20-100
        return total * 4;
    }

    /**
     * Gets detailed description of personality type
     */
    public static String getPersonalityDescription(int score) {
        try {
            PersonalityType type = classifyPersonality(score);
            return String.format("%s (%d points): %s",
                    type.getDisplayName(),
                    score,
                    type.getDescription());
        } catch (InvalidPersonalityScoreException e) {
            return "Invalid score - cannot determine personality type";
        }
    }

    /**
     * Calculates points needed for next tier
     */
    public static int getPointsToNextTier(int currentScore) {
        if (!isValidScore(currentScore)) {
            return -1;
        }

        if (currentScore >= LEADER_MIN) {
            return 0; // Already at highest
        } else if (currentScore >= BALANCED_MIN) {
            return LEADER_MIN - currentScore;
        } else {
            return BALANCED_MIN - currentScore;
        }
    }

    /**
     * Gets the next personality tier
     */
    public static PersonalityType getNextTier(int currentScore) {
        if (!isValidScore(currentScore)) {
            return null;
        }

        if (currentScore >= LEADER_MIN) {
            return null; // Already at highest
        } else if (currentScore >= BALANCED_MIN) {
            return PersonalityType.LEADER;
        } else {
            return PersonalityType.BALANCED;
        }
    }

    /**
     * Provides team role recommendation
     */
    public static String getTeamRoleRecommendation(PersonalityType type) {
        switch (type) {
            case LEADER:
                return "Recommended for: Team Captain, Shot Caller, " +
                        "Strategic Decision Maker. Best suited to coordinate " +
                        "team actions and maintain morale.";

            case BALANCED:
                return "Recommended for: Flex Player, Adaptable Roles. " +
                        "Excellent at filling gaps in team composition and " +
                        "mediating between team members.";

            case THINKER:
                return "Recommended for: Strategist, Analyst, Planner. " +
                        "Best suited for developing tactics, analyzing " +
                        "opponents, and identifying optimal strategies.";

            default:
                return "Unable to provide recommendation.";
        }
    }

    /**
     * Analyzes compatibility between two personality types
     */
    public static String getCompatibilityAnalysis(PersonalityType type1,
                                                  PersonalityType type2) {
        double compatibility = type1.getCompatibilityWith(type2);

        if (compatibility >= 0.9) {
            return String.format("%s + %s = Excellent complement! " +
                            "These personalities balance each other well.",
                    type1.getDisplayName(), type2.getDisplayName());
        } else if (compatibility >= 0.7) {
            return String.format("%s + %s = Good synergy. " +
                            "These types work well together.",
                    type1.getDisplayName(), type2.getDisplayName());
        } else {
            return String.format("%s + %s = Similar types. " +
                            "Team may benefit from more diversity.",
                    type1.getDisplayName(), type2.getDisplayName());
        }
    }

    /**
     * Generates personality distribution summary
     */
    public static String getDistributionSummary(int[] scores) {
        if (scores == null || scores.length == 0) {
            return "No scores to analyze.";
        }

        int leaders = 0, balanced = 0, thinkers = 0;

        for (int score : scores) {
            try {
                PersonalityType type = classifyPersonality(score);
                switch (type) {
                    case LEADER: leaders++; break;
                    case BALANCED: balanced++; break;
                    case THINKER: thinkers++; break;
                }
            } catch (InvalidPersonalityScoreException e) {
                // Skip invalid scores
            }
        }

        return String.format(
                "Personality Distribution (n=%d):%n" +
                        "   Leaders:  %d (%.1f%%)%n" +
                        "   Balanced: %d (%.1f%%)%n" +
                        "   Thinkers: %d (%.1f%%)",
                scores.length,
                leaders, (leaders * 100.0 / scores.length),
                balanced, (balanced * 100.0 / scores.length),
                thinkers, (thinkers * 100.0 / scores.length)
        );
    }
}
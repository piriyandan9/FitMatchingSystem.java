package com.fitmatch.model;

/**
 * Personality types based on 5-question survey (scaled to 100).
 * Score Range: 5-25 questions x 4 = 20-100
 *
 * Classification:
 * - Leader: 90-100
 * - Balanced: 70-89
 * - Thinker: 50-69
 */
public enum PersonalityType {
    LEADER("Leader", 90, 100,
            "Confident decision-maker who naturally takes charge"),
    BALANCED("Balanced", 70, 89,
            "Adaptive and communicative team-oriented player"),
    THINKER("Thinker", 50, 69,
            "Observant and analytical, prefers planning before action");

    private final String displayName;
    private final int minScore;
    private final int maxScore;
    private final String description;

    PersonalityType(String displayName, int minScore, int maxScore, String description) {
        this.displayName = displayName;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public int getMinScore() { return minScore; }
    public int getMaxScore() { return maxScore; }
    public String getDescription() { return description; }

    /**
     * Checks if a given score falls within this personality type's range
     */
    public boolean matchesScore(int score) {
        return score >= minScore && score <= maxScore;
    }

    /**
     * Calculates compatibility between two personality types.
     * Different personalities create stronger team dynamics.
     */
    public double getCompatibilityWith(PersonalityType other) {
        if (this == other) {
            return 0.5; // Same type - less diversity
        }

        int difference = Math.abs(this.ordinal() - other.ordinal());
        if (difference == 1) return 0.8;  // Adjacent types
        if (difference == 2) return 1.0;  // Maximum contrast
        return 0.7;
    }

    @Override
    public String toString() { return displayName; }
}
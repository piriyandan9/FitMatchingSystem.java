// ================== SkillLevel.java ==================
package com.fitmatch.model;

/**
 * Enumeration for participant skill levels
 */
public enum SkillLevel {
    BEGINNER("Beginner", 1, "New to the game, learning basics"),
    INTERMEDIATE("Intermediate", 2, "Comfortable with game mechanics"),
    ADVANCED("Advanced", 3, "Highly skilled, competitive experience"),
    EXPERT("Expert", 4, "Professional/semi-professional level");

    private final String displayName;
    private final int level;
    private final String description;

    SkillLevel(String displayName, int level, String description) {
        this.displayName = displayName;
        this.level = level;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public int getLevel() { return level; }
    public String getDescription() { return description; }

    /**
     * Calculates skill compatibility - similar skills work better together
     */
    public double getCompatibilityWith(SkillLevel other) {
        int diff = Math.abs(this.level - other.level);
        switch (diff) {
            case 0: return 1.0;   // Same level - perfect
            case 1: return 0.7;   // Adjacent levels
            case 2: return 0.4;   // Two levels apart
            default: return 0.2; // Very different
        }
    }

    public static SkillLevel fromString(String text) {
        for (SkillLevel skill : SkillLevel.values()) {
            if (skill.displayName.equalsIgnoreCase(text) ||
                    skill.name().equalsIgnoreCase(text)) {
                return skill;
            }
        }
        return INTERMEDIATE; // Default
    }

    @Override
    public String toString() { return displayName; }
}

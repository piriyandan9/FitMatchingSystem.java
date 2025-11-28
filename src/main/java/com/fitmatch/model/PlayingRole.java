// ================== PlayingRole.java ==================
package com.fitmatch.model;

/**
 * Enumeration for general playing roles applicable across games.
 * Updated roles: Strategist, Attacker, Defender, Supporter, Coordinator
 */
public enum PlayingRole {
    STRATEGIST("Strategist", "S",
            "Focuses on tactics and planning"),
    ATTACKER("Attacker", "A",
            "Frontline player with offensive focus"),
    DEFENDER("Defender", "D",
            "Protective player focused on defense"),
    SUPPORTER("Supporter", "Su",
            "Jack-of-all-trades adapting to team needs"),
    COORDINATOR("Coordinator", "C",
            "Communication lead keeping team organized");

    private final String displayName;
    private final String icon;
    private final String description;

    PlayingRole(String displayName, String icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }

    /**
     * Checks if two roles create good variety in a team
     */
    public boolean complementsWith(PlayingRole other) {
        return this != other;
    }

    public static PlayingRole fromString(String text) {
        for (PlayingRole role : PlayingRole.values()) {
            if (role.displayName.equalsIgnoreCase(text) ||
                    role.name().equalsIgnoreCase(text)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown playing role: " + text);
    }

    @Override
    public String toString() { return displayName; }
}
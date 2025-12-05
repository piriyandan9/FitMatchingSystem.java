package com.fitmatch.model;

import java.util.Arrays;
import java.util.List;

public enum GameType {
    CHESS("Chess", GameCategory.SPORT, 2,
            Arrays.asList("Strategic Player", "Tactical Player")),
    FIFA("FIFA", GameCategory.ESPORT, 2,
            Arrays.asList("Attacker", "Midfielder", "Defender", "Goalkeeper")),
    BASKETBALL("Basketball", GameCategory.SPORT, 5,
            Arrays.asList("Point Guard", "Shooting Guard", "Small Forward",
                    "Power Forward", "Center")),
    CSGO("CS:GO", GameCategory.ESPORT, 5,
            Arrays.asList("Entry Fragger", "AWPer", "Support", "Lurker", "IGL")),
    DOTA("DOTA 2", GameCategory.ESPORT, 5,
            Arrays.asList("Carry", "Mid", "Offlane", "Support", "Hard Support")),
    VALORANT("Valorant", GameCategory.ESPORT, 5,
            Arrays.asList("Duelist", "Controller", "Sentinel", "Initiator"));

    private final String displayName;
    private final GameCategory category;
    private final int recommendedTeamSize;
    private final List<String> specificRoles;

    GameType(String displayName, GameCategory category,
             int recommendedTeamSize, List<String> specificRoles) {
        this.displayName = displayName;
        this.category = category;
        this.recommendedTeamSize = recommendedTeamSize;
        this.specificRoles = specificRoles;
    }

    public String getDisplayName() { return displayName; }
    public GameCategory getCategory() { return category; }
    public int getRecommendedTeamSize() { return recommendedTeamSize; }
    public List<String> getSpecificRoles() { return specificRoles; }

    /**
     * Parses a string to GameType enum
     */
    public static GameType fromString(String text) {
        for (GameType game : GameType.values()) {
            if (game.displayName.equalsIgnoreCase(text) ||
                    game.name().equalsIgnoreCase(text)) {
                return game;
            }
        }
        throw new IllegalArgumentException("Unknown game type: " + text);
    }

    @Override
    public String toString() { return displayName; }
}
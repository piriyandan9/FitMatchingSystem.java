package com.fitmatch.model;

public enum GameCategory {
    ESPORT("E-Sport", "Electronic/Video Game Competition"),
    SPORT("Sport", "Physical Sport Activity");

    private final String displayName;
    private final String description;

    GameCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
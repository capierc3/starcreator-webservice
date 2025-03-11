package com.brickroad.starcreator_webservice.model.enums;

public enum HabitableZone {
    EXCELLENT("Long lifespans, stable, less extreme flares.","15 - 30 billion years"),
    GOOD("Stable but shorter-lived than K-dwarfs.", "10 billion years"),
    MIXED("Abundant but face flare activity and tidal locking issues","Over 100 billion years"),
    POSSIBLE("Short-lived, higher UV","2 - 4 billion years"),
    POOR("Lifespan too short for life to develop", "500 million - 2 billion years"),
    NOT_VIABLE("Lifespan too short for life to develop","A few million years");

    private final String description;
    private final String lifespanDescription;

    HabitableZone(String description, String lifespanDescription) {
        this.description = description;
        this.lifespanDescription = lifespanDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getLifespanDescription() {
        return lifespanDescription;
    }
}

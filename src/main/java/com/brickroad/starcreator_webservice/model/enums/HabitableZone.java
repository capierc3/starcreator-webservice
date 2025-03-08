package com.brickroad.starcreator_webservice.model.enums;

public enum HabitableZone {
    EXCELLENT("Long lifespans, stable, less extreme flares.", .3,.8,"15 - 30 billion years"),
    GOOD("Stable but shorter-lived than K-dwarfs.", .95,1.4,"10 billion years"),
    MIXED("Abundant but face flare activity and tidal locking issues", .05,.2,"Over 100 billion years"),
    POSSIBLE("Short-lived, higher UV", 1.5,2.5,"2 - 4 billion years"),
    POOR("Lifespan too short for life to develop", 5,15,"500 million - 2 billion years"),
    NOT_VIABLE("Lifespan too short for life to develop", 20,200,"A few million years");

    private final String description;
    private final double minHabitableZone;
    private final double maxHabitableZone;
    private final String lifespanDescription;

    HabitableZone(String description, double minHabitableZone, double maxHabitableZone, String lifespanDescription) {
        this.description = description;
        this.minHabitableZone = minHabitableZone;
        this.maxHabitableZone = maxHabitableZone;
        this.lifespanDescription = lifespanDescription;
    }

    public String getDescription() {
        return description;
    }

    public double getMinHabitableZone() {
        return minHabitableZone;
    }

    public double getMaxHabitableZone() {
        return maxHabitableZone;
    }

    public String getLifespanDescription() {
        return lifespanDescription;
    }
}

package com.brickroad.starcreator_webservice.utils.tarot;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TarotCard {
    private String name;
    private String orientation;
    private String meaning;
    @JsonIgnore
    private boolean reversed;
    @JsonIgnore
    private String uprightMeaning;
    @JsonIgnore
    private String reversedMeaning;

    public TarotCard(String name, String uprightMeaning, String reversedMeaning) {
        this.name = name;
        this.uprightMeaning = uprightMeaning;
        this.reversedMeaning = reversedMeaning;
        this.reversed = false;
    }

    public String getName() {
        return name;
    }

    public String getMeaning() {
        return meaning;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUprightMeaning() {
        return uprightMeaning;
    }

    public void setUprightMeaning(String uprightMeaning) {
        this.uprightMeaning = uprightMeaning;
    }

    public String getReversedMeaning() {
        return reversedMeaning;
    }

    public void setReversedMeaning(String reversedMeaning) {
        this.reversedMeaning = reversedMeaning;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    @Override
    public String toString() {
        String orientation = reversed ? " (Reversed)" : " (Upright)";
        return name + orientation + "\n   Meaning: " + getMeaning();
    }
}
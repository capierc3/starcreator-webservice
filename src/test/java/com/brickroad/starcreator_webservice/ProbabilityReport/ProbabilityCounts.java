package com.brickroad.starcreator_webservice.ProbabilityReport;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProbabilityCounts {
    private int systemCount = 0;
    private int starCount = 0;
    private int planetCount = 0;
    private int moonCount = 0;
    private int ringCount = 0;
    private int beltCount = 0;
    private int asteroidCount = 0;
    private int tempCount = 0;

    public void incrementSystemCount() {systemCount++;}
    public void incrementSystemCount(int count) {systemCount += count;}
    public void incrementStarCount() {
        starCount++;
    }
    public void incrementStarCount(int count) {starCount += count;}
    public void incrementPlanetCount() {
        planetCount++;
    }
    public void incrementPlanetCount(int count) {planetCount += count;}
    public void incrementMoonCount() {
        moonCount++;
    }
    public void incrementMoonCount(int count) {moonCount += count;}
    public void incrementRingCount() {
        ringCount++;
    }
    public void incrementRingCount(int count) {ringCount += count;}
    public void incrementTempCount() {
        tempCount++;
    }
    public void incrementTempCount(int count) {tempCount += count;}
    public void incrementBeltCount() {
        beltCount++;
    }
    public void incrementBeltCount(int count) {beltCount += count;}
    public void incrementAsteroidCount() {
        asteroidCount++;
    }
    public void incrementAsteroidCount(int count) {asteroidCount += count;}

}


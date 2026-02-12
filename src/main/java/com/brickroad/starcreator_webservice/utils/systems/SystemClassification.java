package com.brickroad.starcreator_webservice.utils.systems;

import com.brickroad.starcreator_webservice.enums.SystemArchetype;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SystemClassification {

    // ================================================================
    // ARCHETYPES
    // ================================================================
    private SystemArchetype primaryArchetype;
    private List<SystemArchetype> secondaryArchetypes = new ArrayList<>();

    // ================================================================
    // ECONOMIC PROFILE
    // ================================================================
    private String mineralRichness;       // BARREN, POOR, MODERATE, RICH, EXCEPTIONAL
    private String fuelAvailability;      // NONE, SCARCE, AVAILABLE, ABUNDANT
    private String waterAccessibility;    // NONE, ICE_LOCKED, TRACE, AVAILABLE, ABUNDANT, OCEAN_WORLDS
    private String volatileSupply;        // NONE, SCARCE, MODERATE, RICH (from ice/Kuiper belts)
    private String industrialPotential;   // NEGLIGIBLE, LOW, MODERATE, HIGH, EXCEPTIONAL

    // ================================================================
    // STRATEGIC PROFILE
    // ================================================================
    private int habitableWorldCount;
    private int terraformableWorldCount;
    private int totalPlanetCount;
    private int totalMoonCount;
    private int beltCount;
    private int gasGiantCount;
    private boolean hasDefensiveBelts;
    private String populationCapacity;    // NONE, OUTPOST, COLONY, SETTLEMENT, METROPOLIS

    // ================================================================
    // DANGER ASSESSMENT
    // ================================================================
    private String dangerRating;          // SAFE, CAUTION, HAZARDOUS, DEADLY, EXTREME
    private List<String> dangerSources = new ArrayList<>();
    private String radiationEnvironment;  // BENIGN, MODERATE, HARSH, LETHAL
    private String orbitalStability;      // STABLE, MOSTLY_STABLE, CHAOTIC

    // ================================================================
    // INTEREST RATINGS (1-10 scale, by faction type)
    // ================================================================
    private int miningInterest;
    private int colonizationInterest;
    private int scientificInterest;
    private int militaryInterest;
    private int explorationInterest;
    private int xenobiologyInterest;

    // ================================================================
    // NARRATIVE
    // ================================================================
    private String scoutReport;           // The narrative "scout report" description
    private String systemSummary;         // One-line elevator pitch
    private List<String> notableFeatures = new ArrayList<>();
}

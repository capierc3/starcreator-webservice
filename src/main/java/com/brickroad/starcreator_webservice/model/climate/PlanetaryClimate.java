package com.brickroad.starcreator_webservice.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Climate and atmospheric conditions for a planet or moon.
 * <p>
 * This is a transient POJO — fully regenerated from the persisted {@code climateSeed}
 * via {@link com.brickroad.starcreator_webservice.creator.ClimateCreator} at load time.
 * Nothing in this class is persisted to the database (tables dropped in V108).
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Climate and atmospheric conditions for a planet or moon")
public class PlanetaryClimate {

    // ================================================================
    // ATMOSPHERIC STRUCTURE
    // ================================================================
    private Double scaleHeightKm;
    private Double tropopauseAltitudeKm;
    private Double totalAtmosphereHeightKm;
    private String skyColor;
    private String skyColorDescription;
    private Double twilightDurationMinutes;

    // ================================================================
    // TEMPERATURE & CLIMATE
    // ================================================================
    private Double dayNightTempRangeK;
    private Double seasonalTempAmplitudeK;
    private String seasonDescription;
    private String thermalInertiaClass;
    private Double meanEquatorialTempK;
    private Double meanPolarTempK;

    // ================================================================
    // ORBITAL LIGHT CYCLE
    // ================================================================
    private OrbitalLightCycle orbitalLightCycle;

    // ================================================================
    // WIND & CIRCULATION
    // ================================================================
    private String circulationPattern;
    private Integer circulationCellCount;
    private Integer jetStreamCount;
    private Double jetStreamSpeedMs;
    private Double meanSurfaceWindSpeedMs;
    private Double maxGustSpeedMs;
    private String windIntensity;
    private Boolean hasSuperRotation = false;
    private Double superRotationFactor;

    // ================================================================
    // CLOUDS
    // ================================================================
    private Double cloudCoveragePercent;
    private String cloudCoverageClass;
    private String primaryCloudComposition;

    // ================================================================
    // PRECIPITATION
    // ================================================================
    private Boolean hasPrecipitation = false;
    private String primaryPrecipitationType;
    private String precipitationFrequency;
    private Boolean precipitationReachesSurface = true;

    // ================================================================
    // STORMS
    // ================================================================
    private String stormFrequency;
    private Double typicalStormWindSpeedMs;
    private Boolean hasDustStorms = false;
    private Boolean dustStormsCanBeGlobal = false;
    private Boolean hasLightning = false;
    private String lightningType;

    // ================================================================
    // TIDAL EFFECTS
    // ================================================================
    private Double tidalRangeMeters;
    private Double dominantTidalPeriodHours;
    private String atmosphericTidalEffect;

    // ================================================================
    // GAS GIANT SPECIFIC
    // ================================================================
    private Integer numberOfBands;
    private Double internalHeatFluxWm2;
    private Double internalToStellarRatio;
    private Boolean hasGreatDarkSpot = false;

    // ================================================================
    // VISUAL / NARRATIVE
    // ================================================================
    private String daytimeSkyDescription;
    private String nighttimeSkyDescription;
    private String sunsetDescription;
    private String outdoorExposureRating;
    private String survivalTimeDescription;

    // ================================================================
    // CLIMATE SUMMARY
    // ================================================================
    private String weatherSummary;
    private String weatherSeverity;

    // ================================================================
    // CHILD COLLECTIONS
    // ================================================================
    private List<ClimateZone> climateZones = new ArrayList<>();
    private List<CloudLayer> cloudLayers = new ArrayList<>();
    private List<PrecipitationType> precipitationTypes = new ArrayList<>();
    private List<ExtremeClimateEvent> extremeClimateEvents = new ArrayList<>();
    private List<MoonSkyAppearance> moonSkyAppearances = new ArrayList<>();
    private List<EclipseData> eclipseData = new ArrayList<>();
    private List<ClimateHazard> climateHazards = new ArrayList<>();
    private List<SurfaceDeposit> surfaceDeposits = new ArrayList<>();

    // ================================================================
    // TRANSIENT FLAGS
    // ================================================================
    @JsonIgnore
    private boolean isMoonClimate = false;
}

package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "planetary_weather", schema = "ud")
public class PlanetaryWeather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "planet_id", unique = true)
    private Long planetId;

    @Transient
    @JsonIgnore
    private Planet planet;

    @Column(name = "moon_id")
    private Long moonId;

    @Transient
    @JsonIgnore
    private Moon moon;

    // ================================================================
    // ATMOSPHERIC STRUCTURE
    // ================================================================
    @Column(name = "scale_height_km")
    private Double scaleHeightKm;

    @Column(name = "tropopause_altitude_km")
    private Double tropopauseAltitudeKm;

    @Column(name = "total_atmosphere_height_km")
    private Double totalAtmosphereHeightKm;

    @Column(name = "sky_color", length = 50)
    private String skyColor;

    @Column(name = "sky_color_description", length = 200)
    private String skyColorDescription;

    @Column(name = "twilight_duration_minutes")
    private Double twilightDurationMinutes;

    // ================================================================
    // TEMPERATURE & CLIMATE
    // ================================================================
    @Column(name = "day_night_temp_range_k")
    private Double dayNightTempRangeK;

    @Column(name = "seasonal_temp_amplitude_k")
    private Double seasonalTempAmplitudeK;

    @Column(name = "season_description", length = 300)
    private String seasonDescription;

    @Column(name = "thermal_inertia_class", length = 30)
    private String thermalInertiaClass;

    @Column(name = "mean_equatorial_temp_k")
    private Double meanEquatorialTempK;

    @Column(name = "mean_polar_temp_k")
    private Double meanPolarTempK;

    // ================================================================
    // WIND & CIRCULATION
    // ================================================================
    @Column(name = "circulation_pattern", length = 50)
    private String circulationPattern;

    @Column(name = "circulation_cell_count")
    private Integer circulationCellCount;

    @Column(name = "jet_stream_count")
    private Integer jetStreamCount;

    @Column(name = "jet_stream_speed_ms")
    private Double jetStreamSpeedMs;

    @Column(name = "mean_surface_wind_speed_ms")
    private Double meanSurfaceWindSpeedMs;

    @Column(name = "max_gust_speed_ms")
    private Double maxGustSpeedMs;

    @Column(name = "wind_intensity", length = 30)
    private String windIntensity;

    @Column(name = "has_super_rotation")
    private Boolean hasSuperRotation = false;

    @Column(name = "super_rotation_factor")
    private Double superRotationFactor;

    // ================================================================
    // CLOUDS
    // ================================================================
    @Column(name = "cloud_coverage_percent")
    private Double cloudCoveragePercent;

    @Column(name = "cloud_coverage_class", length = 30)
    private String cloudCoverageClass;

    @Column(name = "primary_cloud_composition", length = 100)
    private String primaryCloudComposition;

    // ================================================================
    // PRECIPITATION
    // ================================================================
    @Column(name = "has_precipitation")
    private Boolean hasPrecipitation = false;

    @Column(name = "primary_precipitation_type", length = 50)
    private String primaryPrecipitationType;

    @Column(name = "precipitation_frequency", length = 30)
    private String precipitationFrequency;

    @Column(name = "precipitation_reaches_surface")
    private Boolean precipitationReachesSurface = true;

    // ================================================================
    // STORMS
    // ================================================================
    @Column(name = "storm_frequency", length = 30)
    private String stormFrequency;

    @Column(name = "typical_storm_wind_speed_ms")
    private Double typicalStormWindSpeedMs;

    @Column(name = "has_dust_storms")
    private Boolean hasDustStorms = false;

    @Column(name = "dust_storms_can_be_global")
    private Boolean dustStormsCanBeGlobal = false;

    @Column(name = "has_lightning")
    private Boolean hasLightning = false;

    @Column(name = "lightning_type", length = 50)
    private String lightningType;

    // ================================================================
    // TIDAL EFFECTS
    // ================================================================
    @Column(name = "tidal_range_meters")
    private Double tidalRangeMeters;

    @Column(name = "dominant_tidal_period_hours")
    private Double dominantTidalPeriodHours;

    @Column(name = "atmospheric_tidal_effect", length = 200)
    private String atmosphericTidalEffect;

    // ================================================================
    // GAS GIANT SPECIFIC
    // ================================================================
    @Column(name = "number_of_bands")
    private Integer numberOfBands;

    @Column(name = "internal_heat_flux_wm2")
    private Double internalHeatFluxWm2;

    @Column(name = "internal_to_stellar_ratio")
    private Double internalToStellarRatio;

    @Column(name = "has_great_dark_spot")
    private Boolean hasGreatDarkSpot = false;

    // ================================================================
    // VISUAL / NARRATIVE
    // ================================================================
    @Column(name = "daytime_sky_description", length = 500)
    private String daytimeSkyDescription;

    @Column(name = "nighttime_sky_description", length = 500)
    private String nighttimeSkyDescription;

    @Column(name = "sunset_description", length = 300)
    private String sunsetDescription;

    @Column(name = "outdoor_exposure_rating", length = 50)
    private String outdoorExposureRating;

    @Column(name = "survival_time_description", length = 200)
    private String survivalTimeDescription;

    // ================================================================
    // WEATHER SUMMARY
    // ================================================================
    @Column(name = "weather_summary", length = 1000)
    private String weatherSummary;

    @Column(name = "weather_severity", length = 30)
    private String weatherSeverity;

    // ================================================================
    // CHILD COLLECTIONS (transient — managed separately for persistence)
    // ================================================================
    @Transient
    private List<ClimateZone> climateZones = new ArrayList<>();

    @Transient
    private List<CloudLayer> cloudLayers = new ArrayList<>();

    @Transient
    private List<PrecipitationType> precipitationTypes = new ArrayList<>();

    @Transient
    private List<ExtremeWeatherEvent> extremeWeatherEvents = new ArrayList<>();

    @Transient
    private List<MoonSkyAppearance> moonSkyAppearances = new ArrayList<>();

    @Transient
    private List<EclipseData> eclipseData = new ArrayList<>();

    @Transient
    private List<WeatherHazard> weatherHazards = new ArrayList<>();

    // ================================================================
    // METADATA
    // ================================================================
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    // ================================================================
    // CONSTRUCTORS
    // ================================================================
    public PlanetaryWeather() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    // ================================================================
    // PERSISTENCE HELPER
    // ================================================================
    public void preparePersistence() {
        if (this.planet != null && this.planet.getId() != null) {
            this.planetId = this.planet.getId();
        }
        if (this.moon != null && this.moon.getId() != null) {
            this.moonId = this.moon.getId();
        }
    }
}

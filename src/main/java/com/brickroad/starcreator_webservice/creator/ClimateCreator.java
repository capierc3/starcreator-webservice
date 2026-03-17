package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.model.climate.*;
import com.brickroad.starcreator_webservice.utils.CelestialBodyUtils;
import com.brickroad.starcreator_webservice.utils.planets.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ClimateCreator {

    @Autowired
    private AtmosphericStructureCalculator atmosphericStructureCalculator;

    @Autowired
    private TemperatureClimateCalculator temperatureClimateCalculator;

    @Autowired
    private WindCirculationCalculator windCirculationCalculator;

    @Autowired
    private CloudPrecipitationCalculator cloudPrecipitationCalculator;

    @Autowired
    private StormCalculator stormCalculator;

    @Autowired
    private TidalWeatherCalculator tidalWeatherCalculator;

    @Autowired
    private SkyAppearanceCalculator skyAppearanceCalculator;

    @Autowired
    private ClimateNarrativeGenerator climateNarrativeGenerator;

    @Autowired
    private GasGiantFeatureCalculator gasGiantFeatureCalculator;

    public PlanetaryClimate generateClimate(Planet planet, Star parentStar, StarSystem system) {

        String atmClass = planet.getAtmosphereClassification();
        if (atmClass == null || "NONE".equals(atmClass)) {
            // No atmosphere → no weather, but surface deposits can still exist
            // (e.g., intrinsic ice on ice worlds, cryovolcanic ice)
            PlanetaryClimate minimal = new PlanetaryClimate();
            minimal.setSurfaceDeposits(SurfaceDepositCalculator.calculate(minimal, planet, parentStar));
            if (minimal.getSurfaceDeposits() != null && !minimal.getSurfaceDeposits().isEmpty()) {
                return minimal;
            }
            return null;
        }

        PlanetaryClimate climate = new PlanetaryClimate();

        // Phase 2: Atmospheric Structure
        atmosphericStructureCalculator.calculate(climate, planet, parentStar);

        // Phase 3: Temperature & Climate
        temperatureClimateCalculator.calculate(climate, planet, parentStar, system);

        // Phase 4: Wind & Circulation
        windCirculationCalculator.calculate(climate, planet);

        // Phase 5: Clouds & Precipitation
        cloudPrecipitationCalculator.calculate(climate, planet);

        // Phase 6: Storms
        stormCalculator.calculate(climate, planet);

        // Phase 7: Tidal & Visual
        tidalWeatherCalculator.calculate(climate, planet, parentStar, system);
        skyAppearanceCalculator.calculate(climate, planet, parentStar, system);

        // Phase 9: Gas Giant Specialization (before narrative so narrative can use the data)
        if (CelestialBodyUtils.isGasGiantAtmosphere(atmClass)) {
            gasGiantFeatureCalculator.calculate(climate, planet, parentStar);
        }

        // Phase 10: Surface Deposits (after precipitation, uses terrain + hydrology + habitability)
        climate.setSurfaceDeposits(SurfaceDepositCalculator.calculate(climate, planet, parentStar));

        // Phase 8: Narrative (last — uses all prior data)
        climateNarrativeGenerator.generate(climate, planet, parentStar);

        return climate;
    }

    public PlanetaryClimate generateMoonClimate(Moon moon, Planet parentPlanet, Star parentStar,
                                                StarSystem system, List<Moon> siblingMoons) {
        if (!Boolean.TRUE.equals(moon.getHasAtmosphere())) {
            // No atmosphere → check for surface deposits (ice moons, cryovolcanism)
            Planet proxy = buildMoonProxy(moon, parentPlanet, "NONE");
            PlanetaryClimate minimal = new PlanetaryClimate();
            minimal.setMoonClimate(true);
            minimal.setSurfaceDeposits(SurfaceDepositCalculator.calculate(minimal, proxy, parentStar));
            if (minimal.getSurfaceDeposits() != null && !minimal.getSurfaceDeposits().isEmpty()) {
                return minimal;
            }
            return null;
        }

        // Get atmosphere classification from the moon's atmosphere entity
        Atmosphere atm = moon.getAtmosphere();
        if (atm == null) {
            return null;
        }
        String atmClass = atm.getClassification();
        if (atmClass == null || "NONE".equals(atmClass)) {
            return null;
        }

        PlanetaryClimate climate = new PlanetaryClimate();
        climate.setMoonClimate(true);

        // Build a proxy Planet from moon data so existing calculators work unchanged.
        // This avoids duplicating every calculator with moon-specific methods.
        Planet proxy = buildMoonProxy(moon, parentPlanet, atmClass);

        // Phase 2: Atmospheric Structure (has dedicated moon method)
        atmosphericStructureCalculator.calculateForMoon(climate, moon, parentStar);

        // Phase 3: Temperature & Climate
        temperatureClimateCalculator.calculate(climate, proxy, parentStar, system);

        // Phase 4: Wind & Circulation
        windCirculationCalculator.calculate(climate, proxy);

        // Phase 5: Clouds & Precipitation
        cloudPrecipitationCalculator.calculate(climate, proxy);

        // Phase 6: Storms (moons generally have limited storm activity, but the calculator handles thin atmospheres)
        stormCalculator.calculate(climate, proxy);

        // Phase 7: Tidal & Visual — parent planet and sibling moons affect this moon
        tidalWeatherCalculator.calculateForMoon(climate, moon, parentPlanet, parentStar, siblingMoons);
        skyAppearanceCalculator.calculateForMoon(climate, moon, parentPlanet, parentStar, system, siblingMoons);

        // Phase 10: Surface Deposits
        climate.setSurfaceDeposits(SurfaceDepositCalculator.calculate(climate, proxy, parentStar));

        // Phase 8: Narrative (last — uses all prior data)
        climateNarrativeGenerator.generate(climate, proxy, parentStar);

        return climate;
    }

    private Planet buildMoonProxy(Moon moon, Planet parentPlanet, String atmClass) {
        Planet proxy = new Planet();

        // Atmosphere — build proxy Atmosphere entity
        Atmosphere proxyAtm = Atmosphere.builder()
                .classification(atmClass)
                .surfacePressureBar(moon.getSurfacePressure())
                .compositionSummary(moon.getAtmosphereComposition())
                .isStripped(false)
                .components(new ArrayList<>())
                .build();
        proxy.setAtmosphere(proxyAtm);

        // Physical properties
        proxy.setSurfaceTemp(moon.getSurfaceTemp());
        proxy.setEarthMass(moon.getEarthMass());
        proxy.setEarthRadius(moon.getEarthRadius());
        proxy.setSurfaceGravity(moon.getSurfaceGravity());
        proxy.setEscapeVelocity(moon.getEscapeVelocity());
        proxy.setAlbedo(moon.getAlbedo());

        // Rotation and orbital
        proxy.setRotationPeriodHours(moon.getRotationPeriodHours());
        proxy.setAxialTilt(moon.getAxialTilt());
        proxy.setTidallyLocked(moon.getTidallyLocked());

        // Build a proxy orbit for weather calculations
        OrbitalElements proxyOrbit = new OrbitalElements();
        proxyOrbit.setEccentricity(moon.getEccentricity());
        if (parentPlanet != null) {
            proxyOrbit.setSemiMajorAxis(parentPlanet.getSemiMajorAxisAU());
            proxyOrbit.setOrbitalPeriodDays(parentPlanet.getOrbitalPeriodDays());
        }
        proxy.setOrbit(proxyOrbit);

        // Surface water/ice properties
        HydrologyProperties proxyWater = new HydrologyProperties();
        proxyWater.setLiquidCoveragePercent(moon.getLiquidCoveragePercent());
        proxyWater.setLiquidSurfaceCoveragePercent(moon.getLiquidSurfaceCoveragePercent());
        proxyWater.setWaterIceCoveragePercent(moon.getWaterIceCoveragePercent());
        proxyWater.setVolatileType(moon.getVolatileType());
        proxy.setHydrology(proxyWater);

        // Terrain data (used by StormCalculator, SurfaceDepositCalculator, etc.)
        TerrainProperties proxyTerrain = new TerrainProperties();
        proxyTerrain.setErosionLevel(moon.getErosionLevel());
        proxyTerrain.setGeologicalActivity(moon.getGeologicalActivity());
        proxyTerrain.setHasVolcanicActivity(moon.getTerrain() != null ? moon.getTerrain().getHasVolcanicActivity() : null);
        proxyTerrain.setVolcanismType(moon.getVolcanismType());
        proxyTerrain.setVolcanicIntensity(moon.getVolcanicIntensity());
        proxyTerrain.setHasCryovolcanism(moon.getHasCryovolcanism());
        proxy.setTerrain(proxyTerrain);

        // Storm data — moons don't have these planet-level fields
        proxy.setHasGreatStorm(false);
        proxy.setNumberOfMajorStorms(0);
        proxy.setAtmosphericConvectionLevel(null);

        // No sub-moons
        proxy.setMoons(Collections.emptyList());

        // Carry over transient magnetic field and habitability if available
        proxy.setMagneticField(moon.getMagneticField());
        proxy.setHabitability(moon.getHabitability());

        return proxy;
    }

}

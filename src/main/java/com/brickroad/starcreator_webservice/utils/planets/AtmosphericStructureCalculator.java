package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.utils.CelestialBodyUtils;
import org.springframework.stereotype.Component;

@Component
public class AtmosphericStructureCalculator {

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public void calculate(PlanetaryWeather weather, Planet planet, Star parentStar) {
        String atmClass = planet.getAtmosphereClassification();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        double surfaceGravity = planet.getSurfaceGravity() != null ? planet.getSurfaceGravity() : 1.0;
        double surfaceGravityMs2 = surfaceGravity * CelestialBodyUtils.EARTH_GRAVITY_MS2;
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;
        String composition = planet.getAtmosphereComposition() != null ? planet.getAtmosphereComposition() : "";

        // Mean molecular weight
        double meanMolWeight = CelestialBodyUtils.calculateMeanMolecularWeightFromString(composition);
        if (meanMolWeight <= 0) {
            meanMolWeight = CelestialBodyUtils.estimateMolecularWeightFromClassification(atmClass);
        }

        // Scale height: H = kT / (μ * g)
        double scaleHeightM = (CelestialBodyUtils.BOLTZMANN_K * surfaceTemp)
                / (meanMolWeight * CelestialBodyUtils.AMU_KG * surfaceGravityMs2);
        double scaleHeightKm = scaleHeightM / 1000.0;
        weather.setScaleHeightKm(round2(scaleHeightKm));

        // Atmospheric layers
        calculateLayers(weather, atmClass, scaleHeightKm, surfaceTemp, pressureAtm);

        // Sky color
        calculateSkyColor(weather, atmClass, composition, pressureAtm, parentStar);

        // Twilight duration
        calculateTwilightDuration(weather, scaleHeightKm, pressureAtm, planet);
    }

    public void calculateForMoon(PlanetaryWeather weather, Moon moon, Star parentStar) {
        double surfaceTemp = moon.getSurfaceTemp() != null ? moon.getSurfaceTemp() : 100.0;
        double surfaceGravityMs2 = moon.getSurfaceGravity() != null ? moon.getSurfaceGravity() : 1.0;
        // Moon surfaceGravity is already in m/s² (not multiples of Earth g)
        double pressureAtm = moon.getSurfacePressure() != null ? moon.getSurfacePressure() : 0.0;
        String composition = moon.getAtmosphereComposition() != null ? moon.getAtmosphereComposition() : "";

        String atmClass = "NONE";
        if (moon.getAtmosphere() != null) {
            atmClass = moon.getAtmosphere().getClassification();
        }

        double meanMolWeight = CelestialBodyUtils.calculateMeanMolecularWeightFromString(composition);
        if (meanMolWeight <= 0) {
            meanMolWeight = CelestialBodyUtils.estimateMolecularWeightFromClassification(atmClass);
        }

        if (surfaceGravityMs2 > 0 && pressureAtm > 0.0001) {
            double scaleHeightM = (CelestialBodyUtils.BOLTZMANN_K * surfaceTemp)
                    / (meanMolWeight * CelestialBodyUtils.AMU_KG * surfaceGravityMs2);
            double scaleHeightKm = scaleHeightM / 1000.0;
            weather.setScaleHeightKm(round2(scaleHeightKm));

            calculateLayers(weather, atmClass, scaleHeightKm, surfaceTemp, pressureAtm);
        }
        calculateSkyColor(weather, atmClass, composition, pressureAtm, parentStar);
        double scaleHeightForTwilight = weather.getScaleHeightKm() != null ? weather.getScaleHeightKm() : 0.0;
        calculateTwilightDuration(weather, scaleHeightForTwilight, pressureAtm, null);
    }

    // ================================================================
    // ATMOSPHERIC LAYERS
    // ================================================================

    private void calculateLayers(PlanetaryWeather weather, String atmClass,
                                 double scaleHeightKm, double surfaceTemp, double pressureAtm) {

        if (CelestialBodyUtils.isGasGiantAtmosphere(atmClass)) {
            // Gas giants: no solid surface, "layers" are cloud deck boundaries
            // Tropopause at ~1-2 scale heights above 1-bar level
            weather.setTropopauseAltitudeKm(round2(scaleHeightKm * 1.5));
            // Effective atmosphere extends many scale heights
            weather.setTotalAtmosphereHeightKm(round2(scaleHeightKm * 15));
            return;
        }

        // Rocky worlds: layer structure depends on atmosphere thickness
        if (pressureAtm < 0.01) {
            // Mars-like thin: single convective layer, no real stratosphere
            weather.setTropopauseAltitudeKm(round2(scaleHeightKm * 1.2));
            weather.setTotalAtmosphereHeightKm(round2(scaleHeightKm * 4));
        } else if (pressureAtm < 5.0) {
            // Earth-like moderate: troposphere + stratosphere + mesosphere
            // Tropopause: Earth ~12 km at 1 atm, 8.5 km scale height → ~1.4 scale heights
            // Higher pressure pushes tropopause up, lower temp gradient pushes down
            double tropopauseFactor = 1.2 + 0.2 * Math.log10(Math.max(0.01, pressureAtm));
            weather.setTropopauseAltitudeKm(round2(scaleHeightKm * tropopauseFactor));
            weather.setTotalAtmosphereHeightKm(round2(scaleHeightKm * 10));
        } else {
            // Dense atmosphere (Venus-like): thick troposphere dominates
            // Venus: troposphere extends to ~65 km, scale height ~15 km at surface → ~4.3 scale heights
            double tropopauseFactor = 2.5 + 0.5 * Math.log10(pressureAtm);
            weather.setTropopauseAltitudeKm(round2(scaleHeightKm * Math.min(6.0, tropopauseFactor)));
            weather.setTotalAtmosphereHeightKm(round2(scaleHeightKm * 12));
        }
    }

    // ================================================================
    // SKY COLOR
    // ================================================================

    private void calculateSkyColor(PlanetaryWeather weather, String atmClass,
                                   String composition, double pressureAtm, Star parentStar) {
        String skyColor;
        String description;

        switch (atmClass != null ? atmClass : "NONE") {
            case "EARTH_LIKE" -> {
                // N2/O2 Rayleigh scattering — pressure affects depth of blue
                if (pressureAtm > 3.0) {
                    skyColor = "DEEP_BLUE";
                    description = "Dense N2/O2 atmosphere produces a rich, deep blue sky";
                } else if (pressureAtm > 0.5) {
                    skyColor = "BLUE";
                    description = "Rayleigh scattering in N2/O2 atmosphere creates a blue sky";
                } else {
                    skyColor = "DARK_BLUE";
                    description = "Thin N2/O2 atmosphere produces a dark blue sky with visible stars near zenith";
                }
                // Adjust for star color — hotter stars shift bluer, cooler shift slightly
                if (parentStar != null && parentStar.getSurfaceTemp() != 0) {
                    double starTemp = parentStar.getSurfaceTemp();
                    if (starTemp > 7000) {
                        description += ". Illumination from " + getStarColorDesc(starTemp) + " star enhances blue tones";
                    } else if (starTemp < 4000) {
                        skyColor = pressureAtm > 3.0 ? "BLUE" : "PALE_BLUE";
                        description = "Red dwarf illumination shifts N2/O2 sky toward pale lavender-blue";
                    }
                }
            }
            case "VENUS_LIKE" -> {
                skyColor = "ORANGE";
                description = "Dense CO2 atmosphere with sulfuric acid clouds creates a diffuse orange-yellow sky. Surface barely lit.";
            }
            case "MARS_LIKE" -> {
                skyColor = "BUTTERSCOTCH";
                description = "Thin CO2 with suspended iron oxide dust produces a salmon-butterscotch sky";
                if (pressureAtm < 0.001) {
                    skyColor = "NEAR_BLACK";
                    description = "Near-vacuum atmosphere — sky is nearly black with stars visible in daytime";
                }
            }
            case "TITAN_LIKE" -> {
                skyColor = "ORANGE";
                description = "Dense nitrogen with photochemical hydrocarbon haze layers creates a deep orange sky";
            }
            case "JOVIAN" -> {
                skyColor = "PALE_BLUE";
                description = "Hydrogen Rayleigh scattering above the ammonia cloud deck produces a pale blue sky";
            }
            case "ICE_GIANT" -> {
                skyColor = "CYAN";
                description = "H2 scattering combined with methane red-light absorption creates a cyan-teal sky";
            }
            case "VOLCANIC" -> {
                skyColor = "YELLOW_GREY";
                description = "SO2 and sulfur aerosols create a hazy yellow-grey sky with poor visibility";
            }
            case "AMMONIA" -> {
                skyColor = "PALE_YELLOW";
                description = "Ammonia atmosphere with whitish-yellow aerosol haze";
            }
            case "REDUCING" -> {
                skyColor = "BROWN_ORANGE";
                description = "Hydrogen-methane atmosphere with photochemical haze produces a brownish-orange sky";
            }
            case "CORROSIVE" -> {
                double cl2Pct = CelestialBodyUtils.parseGasPercentage(composition, "Cl2");
                if (cl2Pct > 10) {
                    skyColor = "GREEN";
                    description = "Chlorine gas absorption produces a sickly green sky";
                } else {
                    skyColor = "YELLOW_GREEN";
                    description = "Corrosive atmosphere with yellow-green haze from halogen gases";
                }
            }
            case "EXOTIC" -> {
                skyColor = "RED_ORANGE";
                description = "Metallic vapor scattering on this ultrahot world creates a glowing red-orange sky";
            }
            default -> {
                skyColor = "BLACK";
                description = "No significant atmosphere — black sky with stars visible at all times";
            }
        }

        weather.setSkyColor(skyColor);
        weather.setSkyColorDescription(description);
    }

    private String getStarColorDesc(double surfaceTemp) {
        if (surfaceTemp > 10000) return "blue-white";
        if (surfaceTemp > 7500) return "white";
        if (surfaceTemp > 6000) return "yellow-white";
        if (surfaceTemp > 5200) return "yellow";
        if (surfaceTemp > 3700) return "orange";
        return "red";
    }

    // ================================================================
    // TWILIGHT DURATION
    // ================================================================

    private void calculateTwilightDuration(PlanetaryWeather weather, double scaleHeightKm,
                                           double pressureAtm, Planet planet) {
        if (pressureAtm < 0.0001) {
            weather.setTwilightDurationMinutes(0.0); // No atmosphere = instant transition
            return;
        }

        // Base twilight from atmospheric density: thicker = longer
        // Earth: ~8.5 km scale height, 1 atm → ~30 min
        double baseTwilight = 30.0 * (scaleHeightKm / CelestialBodyUtils.EARTH_SCALE_HEIGHT_KM)
                * Math.sqrt(pressureAtm);

        // Rotation rate adjustment: slower rotation = sun moves slower across horizon = longer twilight
        if (planet != null && planet.getRotationPeriodHours() != null && planet.getRotationPeriodHours() > 0) {
            double rotationFactor = planet.getRotationPeriodHours() / CelestialBodyUtils.EARTH_ROTATION_HOURS;
            baseTwilight *= Math.sqrt(rotationFactor); // sqrt to dampen — 4x rotation → 2x twilight
        }

        // Tidally locked: no traditional twilight (permanent day/night), but terminator zone has permanent twilight
        if (planet != null && Boolean.TRUE.equals(planet.getTidallyLocked())) {
            // Terminator band width rather than duration — set to a large value representing perpetual twilight
            baseTwilight = 999.0; // Flag: permanent twilight zone exists at terminator
        }

        // Clamp to reasonable range (0 to 999 for special cases)
        weather.setTwilightDurationMinutes(round2(Math.min(999.0, Math.max(0.0, baseTwilight))));
    }

    // ================================================================
    // UTILITY
    // ================================================================

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

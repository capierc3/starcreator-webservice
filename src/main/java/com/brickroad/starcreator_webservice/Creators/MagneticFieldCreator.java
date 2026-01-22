package com.brickroad.starcreator_webservice.Creators;

import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.model.planets.PlanetaryMagneticField;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.stereotype.Service;

@Service
public class MagneticFieldCreator {

    private static final double EARTH_SURFACE_FIELD_MICROTESLAS = 50.0;
    private static final double EARTH_MAGNETIC_MOMENT = 7.91e22; // A·m²

    public PlanetaryMagneticField generateMagneticField(Planet planet) {
        PlanetaryMagneticField field = new PlanetaryMagneticField();
        field.setPlanet(planet);

        boolean canHaveDynamo = canGenerateDynamo(planet);
        if (!canHaveDynamo) {
            generateWeakOrNoField(field, planet);
        } else {
            generateActiveDynamoField(field, planet);
        }
        return field;
    }

    private boolean canGenerateDynamo(Planet planet) {
        String coreType = planet.getCoreType();

        if (coreType == null || coreType.contains("Ice")) {
            return false;
        }

        if (coreType.contains("Solid")) {
            if (planet.getAgeMY() != null && planet.getAgeMY() < 500) {
                return RandomUtils.rollRange(0, 100) < 30;
            }
            return false;
        }

        return true;
    }

    private void generateActiveDynamoField(PlanetaryMagneticField field, Planet planet) {
        double rotationFactor = calculateRotationFactor(planet);
        double massFactor = planet.getEarthMass() != null ? planet.getEarthMass() : 1.0;
        double densityFactor = calculateDensityFactor(planet);
        double ageFactor = calculateAgeFactor(planet);

        double baseStrength = rotationFactor * Math.sqrt(massFactor) * densityFactor * ageFactor;
        baseStrength *= RandomUtils.rollRange(0.5, 2.0);
        
        field.setStrengthComparedToEarth(baseStrength);

        double avgField = EARTH_SURFACE_FIELD_MICROTESLAS * baseStrength;
        double minField = avgField * 0.6;
        double maxField = avgField * 1.4;
        
        field.setSurfaceFieldMicroteslasAvg(avgField);
        field.setSurfaceFieldMicroteslasMin(minField);
        field.setSurfaceFieldMicroteslasMax(maxField);

        determineDynamoType(field, planet);
        determineFieldGeometry(field, planet, baseStrength);
        determineSpatialVariation(field, planet);
        determineTemporalProperties(field, planet, baseStrength);

        if (baseStrength > 0.1) {
            calculateMagnetosphere(field, planet, baseStrength);
        }
        determineProtectionLevel(field, baseStrength);

        calculateScientificProperties(field, planet, baseStrength);
    }

    private void generateWeakOrNoField(PlanetaryMagneticField field, Planet planet) {
        String coreType = planet.getCoreType();

        boolean hasRemnant = false;
        boolean hasInduced = false;
        
        if (coreType != null && coreType.contains("Solid") && planet.getAgeMY() != null) {
            hasRemnant = RandomUtils.rollRange(0, 100) < 40;
        }

        if (planet.getAtmosphereComposition() != null && planet.getSemiMajorAxisAU() != null) {
            if (planet.getSemiMajorAxisAU() < 2.0) {
                hasInduced = RandomUtils.rollRange(0, 100) < 50;
            }
        }
        
        if (hasRemnant) {
            generateRemnantField(field, planet);
        } else if (hasInduced) {
            generateInducedField(field, planet);
        } else {
            generateNoField(field, planet);
        }
    }

    private void generateRemnantField(PlanetaryMagneticField field, Planet planet) {
        field.setDynamoType(PlanetaryMagneticField.DynamoType.REMNANT);
        field.setDynamoType(PlanetaryMagneticField.DynamoType.REMNANT);
        field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.CHAOTIC);
        field.setTemporalStability(PlanetaryMagneticField.TemporalStability.STABLE);

        double massModifier = 1.0;
        if (planet.getEarthMass() != null) {
            massModifier = Math.sqrt(planet.getEarthMass());
        }

        double activityModifier = 1.0;
        if (planet.getHasVolcanicActivity() != null && planet.getHasVolcanicActivity()) {
            activityModifier = 1.5; // Recent activity = stronger frozen field
        } else if (planet.getActivityScore() != null && planet.getActivityScore() > 3.0) {
            activityModifier = 1.2;
        }

        double baseStrength = RandomUtils.rollRange(0.0001, 0.01);
        double strength = baseStrength * massModifier * activityModifier;
        field.setStrengthComparedToEarth(strength);

        double avgField = EARTH_SURFACE_FIELD_MICROTESLAS * strength;
        field.setSurfaceFieldMicroteslasAvg(avgField);
        field.setSurfaceFieldMicroteslasMin(avgField * 0.1);
        field.setSurfaceFieldMicroteslasMax(avgField * 3.0);

        field.setVariationPattern(PlanetaryMagneticField.VariationPattern.CRUSTAL_ANOMALIES);
        field.setMagnetosphereExists(false);
        field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.NONE);
        field.setHasPaleomagneticRecord(true);

        if (planet.getAgeMY() != null) {
            field.setOldestMagneticRocksMillionYears(planet.getAgeMY() * RandomUtils.rollRange(0.5, 0.9));
        }

        double baseLossRate = 5.0;
        if (planet.getSemiMajorAxisAU() != null && planet.getSemiMajorAxisAU() > 2.0) {
            baseLossRate *= 0.8;
        }
        field.setAtmosphericLossRateFactor(baseLossRate);
    }

    private void generateInducedField(PlanetaryMagneticField field, Planet planet) {
        field.setDynamoType(PlanetaryMagneticField.DynamoType.INDUCED);
        field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.COMPRESSED);
        field.setTemporalStability(PlanetaryMagneticField.TemporalStability.FLUXING);

        double distanceFactor = 1.0;
        if (planet.getSemiMajorAxisAU() != null) {
            distanceFactor = 1.0 / Math.max(0.5, planet.getSemiMajorAxisAU());
            distanceFactor = Math.min(distanceFactor, 4.0);
        }

        double atmosphereFactor = 1.0;
        if (planet.getSurfacePressure() != null) {
            atmosphereFactor = Math.sqrt(planet.getSurfacePressure());
            atmosphereFactor = Math.min(atmosphereFactor, 10.0);
        }

        double baseStrength = RandomUtils.rollRange(0.00001, 0.001);
        double strength = baseStrength * distanceFactor * atmosphereFactor;
        field.setStrengthComparedToEarth(strength);

        double avgField = EARTH_SURFACE_FIELD_MICROTESLAS * strength;
        field.setSurfaceFieldMicroteslasAvg(avgField);
        field.setSurfaceFieldMicroteslasMin(avgField * 0.5);
        field.setSurfaceFieldMicroteslasMax(avgField * 2.0);

        field.setVariationPattern(PlanetaryMagneticField.VariationPattern.HIGHER_AT_EQUATOR);

        int fluxPeriod;
        if (planet.getRotationPeriodHours() != null && planet.getRotationPeriodHours() < 1000) {
            fluxPeriod = (int)(planet.getRotationPeriodHours() * RandomUtils.rollRange(0.8, 1.5));
        } else {
            fluxPeriod = RandomUtils.rollRange(24, 240);
        }
        field.setFluxPeriodHours(fluxPeriod);
        field.setFluxAmplitudePercent(RandomUtils.rollRange(30.0, 70.0));

        field.setMagnetosphereExists(false);
        field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.NONE);

        double lossRate = 8.0 / (1.0 + strength * 1000); // Slightly less loss if stronger induced field
        field.setAtmosphericLossRateFactor(Math.max(5.0, lossRate));
    }

    private void generateNoField(PlanetaryMagneticField field, Planet planet) {
        field.setDynamoType(PlanetaryMagneticField.DynamoType.NONE);
        field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.NONE);
        field.setTemporalStability(PlanetaryMagneticField.TemporalStability.STABLE);

        field.setStrengthComparedToEarth(0.0);
        field.setSurfaceFieldMicroteslasAvg(0.0);
        field.setSurfaceFieldMicroteslasMin(0.0);
        field.setSurfaceFieldMicroteslasMax(0.0);

        field.setVariationPattern(PlanetaryMagneticField.VariationPattern.NO_REGIONAL_VARIANCE);
        field.setMagnetosphereExists(false);
        field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.NONE);

        double lossRate = 10.0;
        if (planet.getSemiMajorAxisAU() != null && planet.getSemiMajorAxisAU() > 2.0) {
            lossRate *= 0.7;
        }
        if (planet.getEarthMass() != null && planet.getEarthMass() > 2.0) {
            lossRate *= 0.8;
        }
        if (planet.getSurfacePressure() != null && planet.getSurfacePressure() > 50.0) {
            lossRate *= 0.9;
        }

        field.setAtmosphericLossRateFactor(lossRate);
    }

    private double calculateRotationFactor(Planet planet) {
        if (planet.getRotationPeriodHours() == null || planet.getRotationPeriodHours() <= 0) {
            return 0.0;
        }
        
        double rotationHours = planet.getRotationPeriodHours();
        if (rotationHours > 500) {
            return RandomUtils.rollRange(0.0, 0.1); // Essentially no field
        }

        double factor = 24.0 / rotationHours;
        if (factor > 20) {
            factor = 20 + Math.log(factor - 20);
        }
        
        return factor;
    }

    private double calculateDensityFactor(Planet planet) {
        if (planet.getDensity() == null) {
            return 1.0;
        }

        double density = planet.getDensity();
        double factor = density / 5.5;

        if (density < 2.0) {
            factor = RandomUtils.rollRange(0.5, 3.0);
        }

        return Math.max(0.1, Math.min(factor, 3.0));
    }

    private double calculateAgeFactor(Planet planet) {
        if (planet.getAgeMY() == null) {
            return 1.0;
        }
        
        double age = planet.getAgeMY();
        if (age < 500) {
            return RandomUtils.rollRange(1.2, 2.0);
        }
        if (age < 6000) {
            return RandomUtils.rollRange(0.8, 1.2);
        }
        if (age < 10000) {
            return RandomUtils.rollRange(0.4, 0.9);
        }
        return RandomUtils.rollRange(0.1, 0.5);
    }

    private void determineDynamoType(PlanetaryMagneticField field, Planet planet) {
        String planetType = planet.getPlanetType();
        
        if (planetType != null && (planetType.contains("Gas") || planetType.contains("Ice Giant"))) {
            field.setDynamoType(PlanetaryMagneticField.DynamoType.METALLIC_HYDROGEN);
            field.setDynamoEfficiency(RandomUtils.rollRange(0.6, 0.95));
            field.setCoreConvectionIntensity(PlanetaryMagneticField.CoreConvectionIntensity.EXTREME);
        } else {
            field.setDynamoType(PlanetaryMagneticField.DynamoType.CORE_DYNAMO);
            field.setDynamoEfficiency(RandomUtils.rollRange(0.3, 0.8));

            if (planet.getHasVolcanicActivity() != null && planet.getHasVolcanicActivity()) {
                field.setCoreConvectionIntensity(PlanetaryMagneticField.CoreConvectionIntensity.STRONG);
            } else {
                int roll = RandomUtils.rollRange(1, 100);
                if (roll < 30) {
                    field.setCoreConvectionIntensity(PlanetaryMagneticField.CoreConvectionIntensity.WEAK);
                } else if (roll < 70) {
                    field.setCoreConvectionIntensity(PlanetaryMagneticField.CoreConvectionIntensity.MODERATE);
                } else {
                    field.setCoreConvectionIntensity(PlanetaryMagneticField.CoreConvectionIntensity.STRONG);
                }
            }
        }
    }

    private void determineFieldGeometry(PlanetaryMagneticField field, Planet planet, double baseStrength) {
        int roll = RandomUtils.rollRange(1, 100);

        if (baseStrength > 1.5) {
            if (roll < 70) {
                field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.DIPOLE);
                field.setDipoleTiltDegrees(RandomUtils.rollRange(0.0, 30.0));
            } else if (roll < 85) {
                field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.DIPOLE);
                field.setDipoleTiltDegrees(RandomUtils.rollRange(30.0, 90.0));
                field.setMagneticAxisOffsetKm(planet.getRadius() * RandomUtils.rollRange(0.1, 0.4));
            } else {
                field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.QUADRUPOLE);
            }
        } else if (baseStrength > 0.5) {
            if (roll < 60) {
                field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.DIPOLE);
                field.setDipoleTiltDegrees(RandomUtils.rollRange(0.0, 45.0));
            } else if (roll < 80) {
                field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.QUADRUPOLE);
            } else {
                field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.MULTIPOLE);
            }
        } else {
            if (roll < 40) {
                field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.DIPOLE);
                field.setDipoleTiltDegrees(RandomUtils.rollRange(0.0, 60.0));
            } else if (roll < 60) {
                field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.QUADRUPOLE);
            } else if (roll < 80) {
                field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.MULTIPOLE);
            } else {
                field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.CHAOTIC);
            }
        }
    }

    private void determineSpatialVariation(PlanetaryMagneticField field, Planet planet) {
        PlanetaryMagneticField.FieldGeometry geometry = field.getFieldGeometry();

        if (geometry == PlanetaryMagneticField.FieldGeometry.DIPOLE) {
            int roll = RandomUtils.rollRange(1, 100);

            boolean fastRotation = planet.getRotationPeriodHours() != null &&
                    planet.getRotationPeriodHours() < 12;

            if (roll < 40 || fastRotation) {
                field.setVariationPattern(PlanetaryMagneticField.VariationPattern.HIGHER_AT_BOTH_POLES);
                field.setPoleFieldStrengthMultiplier(RandomUtils.rollRange(1.5, 2.5));
                field.setEquatorialFieldStrengthMultiplier(RandomUtils.rollRange(0.5, 0.8));
            } else if (roll < 50) {
                field.setVariationPattern(PlanetaryMagneticField.VariationPattern.HIGHER_AT_NORTH_POLE);
                field.setPoleFieldStrengthMultiplier(RandomUtils.rollRange(1.8, 2.8));
            } else if (roll < 60) {
                field.setVariationPattern(PlanetaryMagneticField.VariationPattern.HIGHER_AT_SOUTH_POLE);
                field.setPoleFieldStrengthMultiplier(RandomUtils.rollRange(1.8, 2.8));
            } else if (roll < 75) {
                field.setVariationPattern(PlanetaryMagneticField.VariationPattern.NO_REGIONAL_VARIANCE);
                field.setPoleFieldStrengthMultiplier(1.0);
                field.setEquatorialFieldStrengthMultiplier(1.0);
            } else {
                field.setVariationPattern(PlanetaryMagneticField.VariationPattern.HIGHER_IN_RANDOM_SPOTS);
            }
        } else if (geometry == PlanetaryMagneticField.FieldGeometry.COMPRESSED) {
            field.setVariationPattern(PlanetaryMagneticField.VariationPattern.HIGHER_AT_EQUATOR);
            field.setEquatorialFieldStrengthMultiplier(RandomUtils.rollRange(1.5, 2.0));
        } else if (geometry == PlanetaryMagneticField.FieldGeometry.CHAOTIC) {
            if (planet.getActivityScore() != null && planet.getActivityScore() > 5.0) {
                field.setVariationPattern(PlanetaryMagneticField.VariationPattern.CRUSTAL_ANOMALIES);
            } else {
                field.setVariationPattern(PlanetaryMagneticField.VariationPattern.HIGHER_IN_RANDOM_SPOTS);
            }
        } else {
            field.setVariationPattern(PlanetaryMagneticField.VariationPattern.BANDED_ANOMALIES);
        }
    }

    private void determineTemporalProperties(PlanetaryMagneticField field, Planet planet, double baseStrength) {
        int roll = RandomUtils.rollRange(1, 100);

        boolean isYoung = planet.getAgeMY() != null && planet.getAgeMY() < 1000;
        boolean veryStrong = baseStrength > 2.0;
        boolean weak = baseStrength < 0.5;
        boolean isGasGiant = planet.getPlanetType() != null &&
                (planet.getPlanetType().contains("Gas") || planet.getPlanetType().contains("Ice Giant"));
        boolean fastRotation = planet.getRotationPeriodHours() != null &&
                planet.getRotationPeriodHours() < 10;

        if (veryStrong && !isYoung) {
            field.setTemporalStability(PlanetaryMagneticField.TemporalStability.STABLE);
        } else if ((isGasGiant || fastRotation) && roll < 60) {
            field.setTemporalStability(PlanetaryMagneticField.TemporalStability.FLUXING);

            int basePeriod = planet.getRotationPeriodHours() != null ?
                    planet.getRotationPeriodHours().intValue() : 24;
            field.setFluxPeriodHours(RandomUtils.rollRange(basePeriod / 2, basePeriod * 10));

            double avgField = field.getSurfaceFieldMicroteslasAvg();
            double amplitude = baseStrength > 1.0 ?
                    RandomUtils.rollRange(10.0, 30.0) :
                    RandomUtils.rollRange(20.0, 40.0);
            field.setFluxAmplitudePercent(amplitude);
            double amplitudeValue = avgField * (amplitude / 100.0);

            field.setFluxPeakMicroteslas(avgField + amplitudeValue);
            field.setFluxLowMicroteslas(Math.max(0, avgField - amplitudeValue));
        } else if ((weak || isYoung) && roll < 70) {
            field.setTemporalStability(PlanetaryMagneticField.TemporalStability.UNSTABLE);

            if (isYoung) {
                field.setInstabilityFrequency(PlanetaryMagneticField.InstabilityFrequency.FREQUENT);
                field.setRandomFluctuationPercent(RandomUtils.rollRange(30.0, 60.0));
            } else {
                field.setInstabilityFrequency(PlanetaryMagneticField.InstabilityFrequency.OCCASIONAL);
                field.setRandomFluctuationPercent(RandomUtils.rollRange(15.0, 40.0));
            }
        } else if (roll < 85) {
            field.setTemporalStability(PlanetaryMagneticField.TemporalStability.REVERSING);
            field.setHasReversals(true);

            double periodBase = baseStrength > 1.0 ?
                    RandomUtils.rollRange(0.2, 1.0) :
                    RandomUtils.rollRange(0.05, 0.5);
            field.setReversalPeriodMillionYears(periodBase);
            field.setTimeSinceLastReversalMillionYears(
                    RandomUtils.rollRange(0.0, field.getReversalPeriodMillionYears())
            );
            field.setReversalTransitionDurationYears(RandomUtils.rollRange(1000, 10000));
            field.setCurrentReversalState(PlanetaryMagneticField.ReversalState.NORMAL);
        } else if (planet.getAgeMY() != null && planet.getAgeMY() > 8000) {
            field.setTemporalStability(PlanetaryMagneticField.TemporalStability.DECAYING);
        } else {
            field.setTemporalStability(PlanetaryMagneticField.TemporalStability.STABLE);
        }
    }

    private void calculateMagnetosphere(PlanetaryMagneticField field, Planet planet, double baseStrength) {
        field.setMagnetosphereExists(true);

        double baseMagnetopause = 10.0 * Math.sqrt(baseStrength);
        field.setMagnetopauseDistancePlanetRadii(baseMagnetopause * RandomUtils.rollRange(0.8, 1.2));

        field.setBowShockDistancePlanetRadii(field.getMagnetopauseDistancePlanetRadii() * RandomUtils.rollRange(1.3, 1.6));
        field.setMagnetotailLengthPlanetRadii(baseMagnetopause * RandomUtils.rollRange(15, 30));

        if (baseStrength > 0.5) {
            field.setHasRadiationBelts(true);
            
            if (baseStrength > 2.0) {
                field.setInnerBeltIntensity(RandomUtils.flipCoin() == 1 ? PlanetaryMagneticField.BeltIntensity.HIGH : PlanetaryMagneticField.BeltIntensity.EXTREME);
                field.setOuterBeltIntensity(PlanetaryMagneticField.BeltIntensity.MODERATE);
            } else {
                field.setInnerBeltIntensity(PlanetaryMagneticField.BeltIntensity.MODERATE);
                field.setOuterBeltIntensity(PlanetaryMagneticField.BeltIntensity.LOW);
            }
        }

        if (planet.getAtmosphereComposition() != null && baseStrength > 0.3) {
            field.setHasAuroras(true);

            if (field.getFieldGeometry() == PlanetaryMagneticField.FieldGeometry.DIPOLE) {
                field.setAuroralZoneLatitudeDegrees(RandomUtils.rollRange(60.0, 75.0));
            } else {
                field.setAuroralZoneLatitudeDegrees(RandomUtils.rollRange(30.0, 80.0));
            }

            if (baseStrength > 1.5) {
                field.setAuroralFrequency(PlanetaryMagneticField.AuroralFrequency.FREQUENT);
                field.setAuroralIntensity(PlanetaryMagneticField.AuroralIntensity.BRIGHT);
            } else if (baseStrength > 0.8) {
                field.setAuroralFrequency(PlanetaryMagneticField.AuroralFrequency.OCCASIONAL);
                field.setAuroralIntensity(PlanetaryMagneticField.AuroralIntensity.MODERATE);
            } else {
                field.setAuroralFrequency(PlanetaryMagneticField.AuroralFrequency.RARE);
                field.setAuroralIntensity(PlanetaryMagneticField.AuroralIntensity.FAINT);
            }

            field.setAuroralColors(determineAuroralColors(planet.getAtmosphereComposition()));
        }
    }

    private String determineAuroralColors(String atmosphereComposition) {
        if (atmosphereComposition == null || atmosphereComposition.equals("None")) {
            return null;
        }

        StringBuilder colors = new StringBuilder();
        String atmos = atmosphereComposition.toUpperCase();

        if (atmos.contains("O2") || atmos.contains("O3")) {
            colors.append("Green (low altitude), Red (high altitude)");
        }

        if (atmos.contains("N2") || atmos.contains("N ") || atmos.contains("NH3")) {
            if (!colors.isEmpty()) colors.append(", ");
            colors.append("Blue-Purple");
        }

        if (atmos.contains("H2") || atmos.contains("H ")) {
            if (!colors.isEmpty()) colors.append(", ");
            colors.append("Deep Red-Magenta");
        }

        if (atmos.contains("HE")) {
            if (!colors.isEmpty()) colors.append(", ");
            colors.append("Pale Yellow-Gold");
        }

        if (atmos.contains("CO2")) {
            if (!colors.isEmpty()) colors.append(", ");
            colors.append("White-Blue");
        }

        if (atmos.contains("SO2") || atmos.contains("H2S") || atmos.contains("S ")) {
            if (!colors.isEmpty()) colors.append(", ");
            colors.append("Yellow-Orange");
        }

        if (atmos.contains("CH4")) {
            if (!colors.isEmpty()) colors.append(", ");
            colors.append("Teal-Turquoise");
        }

        if (atmos.contains("NE")) {
            if (!colors.isEmpty()) colors.append(", ");
            colors.append("Orange-Red");
        }

        if (atmos.contains("AR")) {
            if (!colors.isEmpty()) colors.append(", ");
            colors.append("Lavender");
        }

        if (atmos.contains("H2O")) {
            if (!colors.isEmpty()) colors.append(", ");
            colors.append("Blue-Green");
        }

        if (colors.isEmpty()) {
            colors.append("Pale White-Blue (unknown composition)");
        }

        return colors.toString();
    }

    private void determineProtectionLevel(PlanetaryMagneticField field, double baseStrength) {
        if (baseStrength < 0.1) {
            field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.NONE);
            field.setShieldsFromStellarWind(false);
            field.setShieldsFromCosmicRays(false);
            field.setAtmosphericLossRateFactor(10.0);
        } else if (baseStrength < 0.5) {
            field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.MINIMAL);
            field.setShieldsFromStellarWind(true);
            field.setShieldsFromCosmicRays(false);
            field.setAtmosphericLossRateFactor(RandomUtils.rollRange(3.0, 5.0));
        } else if (baseStrength < 1.0) {
            field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.MODERATE);
            field.setShieldsFromStellarWind(true);
            field.setShieldsFromCosmicRays(true);
            field.setAtmosphericLossRateFactor(RandomUtils.rollRange(0.8, 1.5));
        } else if (baseStrength < 2.0) {
            field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.STRONG);
            field.setShieldsFromStellarWind(true);
            field.setShieldsFromCosmicRays(true);
            field.setAtmosphericLossRateFactor(RandomUtils.rollRange(0.3, 0.7));
        } else {
            field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.EXCEPTIONAL);
            field.setShieldsFromStellarWind(true);
            field.setShieldsFromCosmicRays(true);
            field.setAtmosphericLossRateFactor(RandomUtils.rollRange(0.1, 0.3));
        }
    }

    private void calculateScientificProperties(PlanetaryMagneticField field, Planet planet, double baseStrength) {
        double planetRadiusM = planet.getRadius() * 1000.0;
        double planetVolume = (4.0/3.0) * Math.PI * Math.pow(planetRadiusM, 3);

        field.setMagneticMoment(EARTH_MAGNETIC_MOMENT * baseStrength * (planetVolume / 1.08e21));
        field.setSurfacePowerFluxWattsPerM2(baseStrength * RandomUtils.rollRange(0.01, 0.1));

        if (planet.getCoreType() != null && !planet.getPlanetType().contains("Gas")) {
            field.setHasPaleomagneticRecord(RandomUtils.rollRange(0, 100) < 70);
            if (field.getHasPaleomagneticRecord() && planet.getAgeMY() != null) {
                field.setOldestMagneticRocksMillionYears(
                    planet.getAgeMY() * RandomUtils.rollRange(0.3, 0.8)
                );
            }
        }
    }
}

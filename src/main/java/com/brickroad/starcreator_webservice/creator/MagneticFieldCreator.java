package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.PlanetaryMagneticField;
import com.brickroad.starcreator_webservice.entity.ud.Star;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.planets.StellarEnvironment;
import org.springframework.stereotype.Service;

@Service
public class MagneticFieldCreator {

    private static final double EARTH_SURFACE_FIELD_MICROTESLAS = 50.0;
    private static final double EARTH_MAGNETIC_MOMENT = 7.91e22; // A·m²

    public PlanetaryMagneticField generateMagneticField(Planet planet) {
        return generateMagneticField(planet, planet.getParentStar());
    }

    public PlanetaryMagneticField generateMagneticField(Planet planet, Star parentStar) {
        PlanetaryMagneticField field = new PlanetaryMagneticField();
        field.setPlanet(planet);

        boolean canHaveDynamo = canGenerateDynamo(planet);
        if (!canHaveDynamo) {
            generateWeakOrNoField(field, planet, parentStar);
        } else {
            generateActiveDynamoField(field, planet, parentStar);
        }
        return field;
    }

    private boolean canGenerateDynamo(Planet planet) {
        String coreType = planet.getCoreType();
        String planetType = planet.getPlanetType();

        if (planetType != null && (planetType.contains("Ice Giant") ||
                planetType.contains("Sub-Neptune") ||
                planetType.contains("Mini-Neptune"))) {
            if (planet.getEarthMass() != null && planet.getEarthMass() > 5.0) {
                return true;
            }
        }

        if (coreType == null) {
            return false;
        }

        if (coreType.contains("Ice") && !coreType.contains("Rock")) {
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

    private void generateActiveDynamoField(PlanetaryMagneticField field, Planet planet, Star parentStar) {
        double rotationFactor = calculateRotationFactor(planet);
        double massFactor = planet.getEarthMass() != null ? planet.getEarthMass() : 1.0;
        double densityFactor = calculateDensityFactor(planet);
        double ageFactor = calculateAgeFactor(planet);
        //double massLimit = planet.getEarthMass() * 3.0;
        double massLimit = planet.getEarthMass() * densityFactor * 2.5;
        double baseStrength = rotationFactor * Math.sqrt(massFactor) * densityFactor * ageFactor;
        baseStrength *= RandomUtils.rollRange(0.5, 2.0);
        baseStrength = Math.min(baseStrength, massLimit);
        
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
            calculateMagnetosphere(field, planet, baseStrength, parentStar);
        }
        determineProtectionLevel(field, baseStrength, parentStar, planet);

        calculateAuroralProperties(field, planet, baseStrength, parentStar);

        calculateScientificProperties(field, planet, baseStrength);
    }

    private void generateWeakOrNoField(PlanetaryMagneticField field, Planet planet, Star parentStar) {
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
        if (planet.getParentStar() != null && planet.getSemiMajorAxisAU() != null) {
            double threat = StellarEnvironment.atmosphericStrippingFactor(
                    planet.getParentStar(), planet.getSemiMajorAxisAU());
            baseLossRate = 5.0 * Math.sqrt(Math.max(1.0, threat));
        } else if (planet.getSemiMajorAxisAU() != null && planet.getSemiMajorAxisAU() > 2.0) {
            baseLossRate *= 0.8;
        }
        field.setAtmosphericLossRateFactor(Math.min(20.0, baseLossRate));
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

        double lossRate = 8.0 / (1.0 + strength * 1000);
        if (planet.getParentStar() != null && planet.getSemiMajorAxisAU() != null) {
            double threat = StellarEnvironment.atmosphericStrippingFactor(
                    planet.getParentStar(), planet.getSemiMajorAxisAU());
            lossRate *= Math.sqrt(Math.max(1.0, threat));
        }
        field.setAtmosphericLossRateFactor(Math.min(20.0, Math.max(3.0, lossRate)));
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
        if (planet.getParentStar() != null && planet.getSemiMajorAxisAU() != null) {
            double threat = StellarEnvironment.atmosphericStrippingFactor(
                    planet.getParentStar(), planet.getSemiMajorAxisAU());
            lossRate = 10.0 * Math.sqrt(Math.max(1.0, threat));
        } else {
            if (planet.getSemiMajorAxisAU() != null && planet.getSemiMajorAxisAU() > 2.0) {
                lossRate *= 0.7;
            }
        }
        if (planet.getEarthMass() != null && planet.getEarthMass() > 2.0) {
            lossRate *= 0.8;
        }
        if (planet.getSurfacePressure() != null && planet.getSurfacePressure() > 50.0) {
            lossRate *= 0.9;
        }
        field.setAtmosphericLossRateFactor(Math.min(20.0, lossRate));
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

        factor *= RandomUtils.rollRange(0.8, 1.2);

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

        if (planetType != null) {
            if (planetType.contains("Gas") && !planetType.contains("Ice")) {
                field.setDynamoType(PlanetaryMagneticField.DynamoType.METALLIC_HYDROGEN);
                field.setDynamoEfficiency(RandomUtils.rollRange(0.6, 0.95));
                field.setCoreConvectionIntensity(PlanetaryMagneticField.CoreConvectionIntensity.EXTREME);
            }
            // Ice giants & Sub-Neptunes: ionic fluid dynamo
            else if (planetType.contains("Ice Giant") ||
                    planetType.contains("Sub-Neptune") ||
                    planetType.contains("Mini-Neptune")) {
                field.setDynamoType(PlanetaryMagneticField.DynamoType.IONIC_FLUID);
                field.setDynamoEfficiency(RandomUtils.rollRange(0.4, 0.7));
                field.setCoreConvectionIntensity(PlanetaryMagneticField.CoreConvectionIntensity.STRONG);
            }
            // Rocky planets: iron core dynamo
            else {
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

    private void calculateMagnetosphere(PlanetaryMagneticField field, Planet planet,
                                        double baseStrength, Star parentStar) {
        field.setMagnetosphereExists(true);

        double magnetopauseRadii;

        if (parentStar != null && planet.getSemiMajorAxisAU() != null) {
            double ramPressure = StellarEnvironment.windRamPressureAtDistance(
                    parentStar, planet.getSemiMajorAxisAU());

            // Magnetic moment scales with field strength and planet volume
            double planetRadiusM = planet.getRadius() * 1000.0;
            double magneticMoment = EARTH_MAGNETIC_MOMENT * baseStrength
                    * Math.pow(planetRadiusM / 6.371e6, 3);

            // Chapman-Ferraro standoff distance (in meters)
            // R_mp = (μ₀/(4π) * M² / (2 * P_ram))^(1/6)
            double mu0_over_4pi = 1e-7; // T·m/A
            double standoffM = Math.pow(
                    mu0_over_4pi * magneticMoment * magneticMoment / (2.0 * ramPressure),
                    1.0 / 6.0);

            // Convert to planet radii
            magnetopauseRadii = standoffM / planetRadiusM;

            // Clamp to physical bounds (minimum ~1.5 radii, max ~100 radii)
            magnetopauseRadii = Math.max(1.5, Math.min(100.0, magnetopauseRadii));

            // Apply variance
            magnetopauseRadii *= RandomUtils.rollRange(0.85, 1.15);
        } else {
            // Fallback: original generic scaling
            magnetopauseRadii = 10.0 * Math.sqrt(baseStrength) * RandomUtils.rollRange(0.8, 1.2);
        }

        field.setMagnetopauseDistancePlanetRadii(magnetopauseRadii);

        // Bow shock: typically 1.3-1.5x the magnetopause distance
        // Higher wind Mach number → shock closer to magnetopause
        double machFactor = 1.45; // default
        if (parentStar != null && parentStar.getStellarWindVelocityKmS() != null) {
            double windSpeed = parentStar.getStellarWindVelocityKmS();
            // Faster wind → higher Mach → compression ratio closer to 4 → shock closer
            if (windSpeed > 600) machFactor = 1.3;
            else if (windSpeed > 400) machFactor = 1.35;
            else machFactor = 1.45;
        }
        field.setBowShockDistancePlanetRadii(magnetopauseRadii * machFactor * RandomUtils.rollRange(0.95, 1.05));

        // Magnetotail: length scales with wind speed (faster wind stretches tail further)
        double tailMultiplier = 20.0; // default Earth-like
        if (parentStar != null && parentStar.getStellarWindVelocityKmS() != null) {
            tailMultiplier = 15.0 + 10.0 * (parentStar.getStellarWindVelocityKmS() / 400.0);
            tailMultiplier = Math.min(60.0, tailMultiplier); // cap at 60x planet radii
        }
        field.setMagnetotailLengthPlanetRadii(magnetopauseRadii * tailMultiplier * RandomUtils.rollRange(0.8, 1.2));

        // Radiation belts (unchanged logic, but compressed magnetosphere = more intense belts)
        if (baseStrength > 0.5) {
            field.setHasRadiationBelts(true);

            boolean compressed = magnetopauseRadii < 6.0;

            if (baseStrength > 2.0 || compressed) {
                field.setInnerBeltIntensity(RandomUtils.flipCoin() == 1
                        ? PlanetaryMagneticField.BeltIntensity.EXTREME
                        : PlanetaryMagneticField.BeltIntensity.HIGH);
                field.setOuterBeltIntensity(PlanetaryMagneticField.BeltIntensity.HIGH);
            } else if (baseStrength > 1.0) {
                field.setInnerBeltIntensity(PlanetaryMagneticField.BeltIntensity.HIGH);
                field.setOuterBeltIntensity(PlanetaryMagneticField.BeltIntensity.MODERATE);
            } else {
                field.setInnerBeltIntensity(PlanetaryMagneticField.BeltIntensity.MODERATE);
                field.setOuterBeltIntensity(PlanetaryMagneticField.BeltIntensity.LOW);
            }
        } else {
            field.setHasRadiationBelts(false);
            field.setInnerBeltIntensity(PlanetaryMagneticField.BeltIntensity.NONE);
            field.setOuterBeltIntensity(PlanetaryMagneticField.BeltIntensity.NONE);
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

        if (colors.toString().isEmpty()) {
            colors.append("Pale White-Blue (unknown composition)");
        }

        return colors.toString();
    }

    private void determineProtectionLevel(PlanetaryMagneticField field, double baseStrength,
                                           Star parentStar, Planet planet) {
        double threatFactor = 1.0;
        if (parentStar != null && planet.getSemiMajorAxisAU() != null) {
            threatFactor = StellarEnvironment.atmosphericStrippingFactor(
                    parentStar, planet.getSemiMajorAxisAU());
        }

        // Protection ratio: field strength vs. threat
        // Using cube root instead of square root — still dampened (a 1000x threat
        // doesn't need a 1000x field to deflect) but less forgiving than sqrt.
        // Also apply a log boost for high-threat environments so VERY_ACTIVE/HYPERACTIVE
        // stars maintain pressure even at moderate distances.
        double effectiveThreat = Math.cbrt(threatFactor);
        if (threatFactor > 3.0) {
            // Bonus penalty for genuinely active environments
            effectiveThreat *= (1.0 + 0.3 * Math.log10(threatFactor));
        }

        double protectionRatio = baseStrength / Math.max(0.01, effectiveThreat);

        if (protectionRatio < 0.05) {
            field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.NONE);
            field.setShieldsFromStellarWind(false);
            field.setShieldsFromCosmicRays(false);
            field.setAtmosphericLossRateFactor(calculateLossRate(baseStrength, threatFactor, 8.0));
        } else if (protectionRatio < 0.3) {
            field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.MINIMAL);
            field.setShieldsFromStellarWind(false);
            field.setShieldsFromCosmicRays(false);
            field.setAtmosphericLossRateFactor(calculateLossRate(baseStrength, threatFactor, 5.0));
        } else if (protectionRatio < 0.8) {
            field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.MODERATE);
            field.setShieldsFromStellarWind(true);
            field.setShieldsFromCosmicRays(false);
            field.setAtmosphericLossRateFactor(calculateLossRate(baseStrength, threatFactor, 2.0));
        } else if (protectionRatio < 2.5) {
            field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.STRONG);
            field.setShieldsFromStellarWind(true);
            field.setShieldsFromCosmicRays(true);
            field.setAtmosphericLossRateFactor(calculateLossRate(baseStrength, threatFactor, 0.5));
        } else {
            field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.EXCEPTIONAL);
            field.setShieldsFromStellarWind(true);
            field.setShieldsFromCosmicRays(true);
            field.setAtmosphericLossRateFactor(calculateLossRate(baseStrength, threatFactor, 0.2));
        }
    }

    private double calculateLossRate(double fieldStrength, double threatFactor, double baseLossRate) {
        // Higher threat = higher loss rate; stronger field = lower loss rate
        double ratio = threatFactor / Math.max(0.01, fieldStrength * fieldStrength);
        double lossRate = baseLossRate * Math.sqrt(Math.max(1.0, ratio));
        return Math.min(20.0, Math.max(0.05, lossRate * RandomUtils.rollRange(0.8, 1.2)));
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

    private void calculateAuroralProperties(PlanetaryMagneticField field, Planet planet,
                                            double baseStrength, Star parentStar) {
        // No magnetosphere = no auroras (particles reach surface directly)
        if (!Boolean.TRUE.equals(field.getMagnetosphereExists()) || baseStrength < 0.05) {
            field.setHasAuroras(false);
            return;
        }

        field.setHasAuroras(true);

        // Auroral zone latitude: stronger dipole field = auroras closer to poles
        double baseLatitude = 65.0 + RandomUtils.rollRange(-5, 5); // Earth-like default
        if (baseStrength > 5.0) {
            baseLatitude = 75.0 + RandomUtils.rollRange(-3, 3); // Strong field pushes auroras poleward
        } else if (baseStrength < 0.3) {
            baseLatitude = 45.0 + RandomUtils.rollRange(-10, 10); // Weak field = auroras at lower latitudes
        }
        field.setAuroralZoneLatitudeDegrees(baseLatitude);

        if (parentStar == null) {
            field.setAuroralFrequency(baseStrength > 1.0
                    ? PlanetaryMagneticField.AuroralFrequency.OCCASIONAL
                    : PlanetaryMagneticField.AuroralFrequency.RARE);
            field.setAuroralIntensity(baseStrength > 1.0
                    ? PlanetaryMagneticField.AuroralIntensity.MODERATE
                    : PlanetaryMagneticField.AuroralIntensity.FAINT);
            return;
        }

        // --- Star-driven auroral activity ---

        // Particle flux = wind density at planet * wind velocity
        double windDensity = StellarEnvironment.windDensityAtDistance(
                parentStar, planet.getSemiMajorAxisAU() != null ? planet.getSemiMajorAxisAU() : 1.0);
        double velocity = parentStar.getStellarWindVelocityKmS() != null
                ? parentStar.getStellarWindVelocityKmS() : 400.0;
        double particleFluxNormalized = (windDensity * velocity) / (6.0 * 400.0); // Normalized to solar at 1 AU

        // Flare contribution: each flare event = auroral storm
        double flareContribution = 0.0;
        if (parentStar.getFlareFrequencyPerDay() != null) {
            flareContribution = parentStar.getFlareFrequencyPerDay();
            // X-class and superflares produce spectacular auroras
            if ("SUPERFLARE".equals(parentStar.getFlareClass())) {
                flareContribution *= 10.0;
            } else if ("X_CLASS".equals(parentStar.getFlareClass())) {
                flareContribution *= 3.0;
            }
        }

        // Activity cycle phase: near maximum = more frequent auroras
        double cycleModifier = 1.0;
        if (parentStar.getActivityCyclePhase() != null) {
            // Phase 0.7-1.0 = near solar max → more auroras
            cycleModifier = 0.5 + parentStar.getActivityCyclePhase();
        }

        double auroralScore = (particleFluxNormalized + flareContribution) * cycleModifier;

        // Frequency
        if (auroralScore > 50) {
            field.setAuroralFrequency(PlanetaryMagneticField.AuroralFrequency.CONSTANT);
        } else if (auroralScore > 10) {
            field.setAuroralFrequency(PlanetaryMagneticField.AuroralFrequency.FREQUENT);
        } else if (auroralScore > 2) {
            field.setAuroralFrequency(PlanetaryMagneticField.AuroralFrequency.OCCASIONAL);
        } else {
            field.setAuroralFrequency(PlanetaryMagneticField.AuroralFrequency.RARE);
        }

        // Intensity
        if (auroralScore > 100 || "SUPERFLARE".equals(parentStar.getFlareClass())) {
            field.setAuroralIntensity(PlanetaryMagneticField.AuroralIntensity.SPECTACULAR);
        } else if (auroralScore > 20) {
            field.setAuroralIntensity(PlanetaryMagneticField.AuroralIntensity.BRIGHT);
        } else if (auroralScore > 3) {
            field.setAuroralIntensity(PlanetaryMagneticField.AuroralIntensity.MODERATE);
        } else {
            field.setAuroralIntensity(PlanetaryMagneticField.AuroralIntensity.FAINT);
        }
    }
}

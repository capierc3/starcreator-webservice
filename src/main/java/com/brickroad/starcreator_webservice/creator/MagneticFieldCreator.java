package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.Moon;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.PlanetaryMagneticField;
import com.brickroad.starcreator_webservice.entity.ud.Star;
import com.brickroad.starcreator_webservice.utils.PhysicsFormulas;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.planets.StellarEnvironment;
import org.springframework.stereotype.Service;

@Service
public class MagneticFieldCreator {

    // Constants delegate to PhysicsFormulas (single source of truth)
    private static final double EARTH_SURFACE_FIELD_MICROTESLAS = PhysicsFormulas.EARTH_SURFACE_FIELD_uT;
    private static final double EARTH_MAGNETIC_MOMENT = PhysicsFormulas.EARTH_MAGNETIC_MOMENT;

    public PlanetaryMagneticField generateMagneticField(Planet planet) {
        return generateMagneticField(planet, planet.getParentStar());
    }

    public PlanetaryMagneticField generateMagneticField(Planet planet, Star parentStar) {
        PlanetaryMagneticField field = new PlanetaryMagneticField();

        boolean canHaveDynamo = canGenerateDynamo(planet);
        if (!canHaveDynamo) {
            generateWeakOrNoField(field, planet, parentStar);
        } else {
            generateActiveDynamoField(field, planet, parentStar);
        }
        return field;
    }

    public PlanetaryMagneticField generateMoonMagneticField(Moon moon, Planet parentPlanet) {
        PlanetaryMagneticField field = new PlanetaryMagneticField();

        MoonFieldType fieldType = determineMoonFieldType(moon, parentPlanet);

        switch (fieldType) {
            case INTRINSIC_DYNAMO:
                generateMoonIntrinsicField(field, moon, parentPlanet);
                break;
            case INDUCED:
                generateMoonInducedField(field, moon, parentPlanet);
                break;
            case REMNANT:
                generateMoonRemnantField(field, moon);
                break;
            case NONE:
            default:
                generateMoonNoField(field, moon);
                break;
        }

        return field;
    }

    private boolean canGenerateDynamo(Planet planet) {
        String coreType = planet.getCoreType();
        String planetType = planet.getPlanetType();

        // Neptune-class and ice giant types above 5 M⊕ have enough internal pressure
        // and heat for ionic fluid dynamos
        if (isNeptuneClass(planetType)) {
            if (planet.getEarthMass() != null && planet.getEarthMass() > 5.0) {
                return true;
            }
        }

        if (coreType == null) {
            return false;
        }

        // Pure ice cores have no conductive fluid for dynamo generation
        if (coreType.contains("Ice") && !coreType.contains("Rock")) {
            return false;
        }

        if (planetType != null && (planetType.contains("Ice World") || planetType.contains("Dwarf Planet"))) {
            double mass = planet.getEarthMass() != null ? planet.getEarthMass() : 0.0;
            if (mass < 0.5) {
                return false;
            }
            // Probability scales with mass: rocky core fraction increases with size
            // 0.5 M⊕ → ~5%, 2 M⊕ → ~20%, 5 M⊕ → ~35%
            double dynamoChance = Math.min(40.0, mass * 8.0);
            return RandomUtils.rollRange(0, 100) < dynamoChance;
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
        double massFactor = planet.getEarthMass() != null ? planet.getEarthMass() : 1.0;
        double ageFactor = calculateAgeFactor(planet);

        String pType = planet.getPlanetType();
        boolean isGasGiant = pType != null && pType.contains("Gas") && !pType.contains("Ice");
        boolean isNeptune = isNeptuneClass(pType);

        double baseStrength;

        if (isGasGiant) {
            // Metallic hydrogen dynamo: convection-driven, only weakly rotation-dependent.
            // Jupiter ~20× Earth, Saturn ~0.7× Earth. Scales roughly with mass^0.5-0.8.
            // Tidally-locked hot Jupiters still produce strong fields.
            double rotFloor = RandomUtils.rollRange(0.6, 1.0);
            double rotationFactor = Math.max(calculateRotationFactor(planet), rotFloor);
            baseStrength = rotationFactor * Math.pow(massFactor, 0.6) * ageFactor;
            baseStrength *= RandomUtils.rollRange(0.5, 2.0);
            // Cap: even the largest gas giant shouldn't exceed ~50× Earth
            baseStrength = Math.min(baseStrength, 50.0);

        } else if (isNeptune) {
            // Ionic fluid dynamo: driven by convection in water/ammonia/methane ices
            // under extreme pressure. Neptune ~27× Earth, Uranus ~0.75× Earth.
            // Less rotation-dependent than rocky core dynamos.
            double rotFloor = RandomUtils.rollRange(0.3, 0.7);
            double rotationFactor = Math.max(calculateRotationFactor(planet), rotFloor);
            baseStrength = rotationFactor * Math.pow(massFactor, 0.5) * ageFactor;
            baseStrength *= RandomUtils.rollRange(0.3, 1.5);
            // Cap: Neptune-class tops out around ~30× Earth
            baseStrength = Math.min(baseStrength, 30.0);

        } else {
            // Rocky core dynamo: strongly rotation-dependent (Earth, Mars, Mercury).
            // Original formula: rotation × √mass × density_ratio × age
            double rotationFactor = calculateRotationFactor(planet);
            double densityFactor = calculateDensityFactor(planet);
            double massLimit = massFactor * densityFactor * 2.5;
            baseStrength = rotationFactor * Math.sqrt(massFactor) * densityFactor * ageFactor;

            if (pType != null && (pType.contains("Ice World") || pType.contains("Dwarf Planet"))) {
                baseStrength = RandomUtils.rollRange(0.005, 0.03);
            } else {
                baseStrength *= RandomUtils.rollRange(0.5, 2.0);
            }
            baseStrength = Math.min(baseStrength, massLimit);
        }
        
        field.setStrengthComparedToEarth(baseStrength);

        double avgField = PhysicsFormulas.surfaceFieldAvgMicrotesla(baseStrength);
        field.setSurfaceFieldMicroteslasAvg(avgField);
        field.setSurfaceFieldMicroteslasMin(PhysicsFormulas.surfaceFieldMinMicrotesla(avgField));
        field.setSurfaceFieldMicroteslasMax(PhysicsFormulas.surfaceFieldMaxMicrotesla(avgField));

        determineDynamoType(field, planet);
        determineFieldGeometry(field, planet, baseStrength);
        determineSpatialVariation(field, planet);
        determineTemporalProperties(field, planet, baseStrength);

        // Any measurable active dynamo gets the full Chapman-Ferraro magnetosphere
        // calculation. The physics formula naturally handles weak fields by producing
        // small, compressed magnetospheres (Mercury-like). Previously the threshold
        // was 0.1, which excluded tidally-locked terrestrial planets that have real
        // core dynamos but weak fields (e.g. 0.03× Earth).
        if (baseStrength > 0.001) {
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
        if (planet.getRotationPeriodHours() != null && Math.abs(planet.getRotationPeriodHours()) < 1000) {
            fluxPeriod = (int)(Math.abs(planet.getRotationPeriodHours()) * RandomUtils.rollRange(0.8, 1.5));
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
        if (planet.getRotationPeriodHours() == null || Math.abs(planet.getRotationPeriodHours()) <= 0) {
            return 0.0;
        }
        
        double rotationHours = Math.abs(planet.getRotationPeriodHours());
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
            // Ice giants & Neptune-class: ionic fluid dynamo
            else if (isNeptuneClass(planetType)) {
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
                    Math.abs(planet.getRotationPeriodHours()) < 12;

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
                (planet.getPlanetType().contains("Gas") || isNeptuneClass(planet.getPlanetType()));
        boolean fastRotation = planet.getRotationPeriodHours() != null &&
                Math.abs(planet.getRotationPeriodHours()) < 10;

        if (veryStrong && !isYoung) {
            field.setTemporalStability(PlanetaryMagneticField.TemporalStability.STABLE);
        } else if ((isGasGiant || fastRotation) && roll < 60) {
            field.setTemporalStability(PlanetaryMagneticField.TemporalStability.FLUXING);

            int basePeriod = planet.getRotationPeriodHours() != null ?
                    (int) Math.abs(planet.getRotationPeriodHours()) : 24;
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
        double magnetopauseRadii;

        if (parentStar != null && planet.getSemiMajorAxisAU() != null) {
            double ramPressure = StellarEnvironment.windRamPressureAtDistance(
                    parentStar, planet.getSemiMajorAxisAU());

            double planetRadiusM = planet.getRadius() * 1000.0;
            double moment = PhysicsFormulas.magneticMoment(baseStrength, planet.getRadius());

            // Deterministic standoff (shared with DerivedFieldCalculator)
            magnetopauseRadii = PhysicsFormulas.magnetopauseStandoffRadii(moment, ramPressure, planetRadiusM);

            // Creator-only random variance (±15%)
            magnetopauseRadii *= RandomUtils.rollRange(0.85, 1.15);
        } else {
            // Fallback: original generic scaling
            magnetopauseRadii = 10.0 * Math.sqrt(baseStrength) * RandomUtils.rollRange(0.8, 1.2);
        }

        // Let the physics decide: if the magnetopause is at the formula floor (1.5 radii),
        // stellar wind has overwhelmed the field and there's no meaningful magnetosphere.
        // Above that, even a small compressed magnetosphere exists (Mercury-like).
        if (magnetopauseRadii <= 1.5) {
            field.setMagnetosphereExists(false);
            return;
        }

        field.setMagnetosphereExists(true);
        field.setMagnetopauseDistancePlanetRadii(magnetopauseRadii);

        // Bow shock: deterministic multiplier (shared) + creator-only variance
        double windSpeed = (parentStar != null && parentStar.getStellarWindVelocityKmS() != null)
                ? parentStar.getStellarWindVelocityKmS() : 400.0;
        double machFactor = PhysicsFormulas.bowShockMultiplier(windSpeed);
        field.setBowShockDistancePlanetRadii(magnetopauseRadii * machFactor * RandomUtils.rollRange(0.95, 1.05));

        // Magnetotail: deterministic multiplier (shared) + creator-only variance
        double tailMultiplier = PhysicsFormulas.magnetotailMultiplier(windSpeed);
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

        // Core protection classification — shared with DerivedFieldCalculator
        PhysicsFormulas.ProtectionResult result =
                PhysicsFormulas.calculateProtectionLevel(baseStrength, threatFactor);
        field.setProtectionLevel(result.level());
        field.setShieldsFromStellarWind(result.shieldsFromStellarWind());
        field.setShieldsFromCosmicRays(result.shieldsFromCosmicRays());

        // Atmospheric loss rate — creator-only (persisted, uses randomness)
        double baseLossRate = switch (result.level()) {
            case NONE -> 8.0;
            case MINIMAL -> 5.0;
            case MODERATE -> 2.0;
            case STRONG -> 0.5;
            case EXCEPTIONAL -> 0.2;
        };
        field.setAtmosphericLossRateFactor(calculateLossRate(baseStrength, threatFactor, baseLossRate));
    }

    private double calculateLossRate(double fieldStrength, double threatFactor, double baseLossRate) {
        // Higher threat = higher loss rate; stronger field = lower loss rate
        double ratio = threatFactor / Math.max(0.01, fieldStrength * fieldStrength);
        double lossRate = baseLossRate * Math.sqrt(Math.max(1.0, ratio));
        return Math.min(20.0, Math.max(0.05, lossRate * RandomUtils.rollRange(0.8, 1.2)));
    }

    private void calculateScientificProperties(PlanetaryMagneticField field, Planet planet, double baseStrength) {
        field.setMagneticMoment(PhysicsFormulas.magneticMoment(baseStrength, planet.getRadius()));
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

    private MoonFieldType determineMoonFieldType(Moon moon, Planet parentPlanet) {
        double mass = moon.getEarthMass() != null ? moon.getEarthMass() : 0;
        String composition = moon.getCompositionType() != null ? moon.getCompositionType() : "";
        boolean hasSubsurfaceOcean = Boolean.TRUE.equals(moon.getHasSubsurfaceOcean());
        String tidalHeating = moon.getTidalHeatingLevel() != null ? moon.getTidalHeatingLevel() : "NONE";
        double ageMY = moon.getAgeMY() != null ? moon.getAgeMY() : 5000;

        // ---- INTRINSIC DYNAMO ----
        // Requires: large moon + rocky/mixed composition + possible liquid core
        // Tidal heating helps sustain liquid core longer
        // Ganymede: 0.025 Earth masses, rocky/ice, tidal heating from Laplace resonance
        if (mass >= 0.005 && ("ROCKY".equals(composition) || "MIXED".equals(composition))) {
            double dynamoChance = 0;

            // Mass factor: bigger = more likely to have liquid core
            if (mass >= 0.02) dynamoChance += 25;       // Ganymede-class
            else if (mass >= 0.01) dynamoChance += 12;   // Titan-class
            else dynamoChance += 5;

            // Tidal heating keeps core liquid
            switch (tidalHeating) {
                case "EXTREME" -> dynamoChance += 30;
                case "HIGH" -> dynamoChance += 20;
                case "MODERATE" -> dynamoChance += 10;
            }

            // Old moons have cooled cores
            if (ageMY > 8000) dynamoChance *= 0.3;
            else if (ageMY > 6000) dynamoChance *= 0.6;

            // Rocky composition more likely to have iron core
            if ("ROCKY".equals(composition)) dynamoChance *= 1.3;

            if (RandomUtils.rollRange(0, 100) < dynamoChance) {
                return MoonFieldType.INTRINSIC_DYNAMO;
            }
        }

        // ---- INDUCED FIELD ----
        // Requires: parent planet with strong magnetosphere + moon has conductive layer (ocean)
        if (hasSubsurfaceOcean && parentPlanet.getMagneticField() != null) {
            PlanetaryMagneticField parentField = parentPlanet.getMagneticField();
            if (parentField.getMagnetosphereExists() != null && parentField.getMagnetosphereExists()
                    && parentField.getStrengthComparedToEarth() != null
                    && parentField.getStrengthComparedToEarth() > 0.5) {

                // Check if moon is within parent's magnetosphere
                if (moon.getSemiMajorAxisKm() != null && parentPlanet.getRadius() > 0
                        && parentField.getMagnetopauseDistancePlanetRadii() != null) {
                    double moonDistRadii = moon.getSemiMajorAxisKm() / parentPlanet.getRadius();
                    double magnetopause = parentField.getMagnetopauseDistancePlanetRadii();

                    if (moonDistRadii < magnetopause) {
                        return MoonFieldType.INDUCED;
                    }
                }
            }
        }

        // ---- REMNANT FIELD ----
        // Old moons that COULD have had a dynamo once (mass > 0.003, rocky/mixed)
        if (mass >= 0.003 && ("ROCKY".equals(composition) || "MIXED".equals(composition))) {
            double remnantChance = 15; // Base 15% for qualifying moons
            if (ageMY > 5000) remnantChance += 10;  // More time for dynamo to have existed and died
            if (mass >= 0.01) remnantChance += 10;

            if (RandomUtils.rollRange(0, 100) < remnantChance) {
                return MoonFieldType.REMNANT;
            }
        }

        return MoonFieldType.NONE;
    }

    private void generateMoonIntrinsicField(PlanetaryMagneticField field, Moon moon,
                                            Planet parentPlanet) {
        double mass = moon.getEarthMass() != null ? moon.getEarthMass() : 0.01;

        // Moon dynamos are weak compared to planets
        // Ganymede: ~750 nT surface = ~0.015 Earth field
        double rotationFactor = 1.0;
        if (moon.getRotationPeriodHours() != null) {
            double period = Math.abs(moon.getRotationPeriodHours());
            if (period < 24) rotationFactor = 1.2;
            else if (period < 100) rotationFactor = 1.0;
            else if (period < 500) rotationFactor = 0.6;
            else rotationFactor = 0.3;
        }

        // Tidal heating sustains liquid core
        double tidalFactor = 1.0;
        String tidalLevel = moon.getTidalHeatingLevel();
        if ("EXTREME".equals(tidalLevel)) tidalFactor = 1.5;
        else if ("HIGH".equals(tidalLevel)) tidalFactor = 1.3;
        else if ("MODERATE".equals(tidalLevel)) tidalFactor = 1.1;

        // Base strength: much weaker than planets
        // Scale: 0.005 - 0.05 Earth for most moon dynamos
        double baseStrength = 0.005 * Math.pow(mass / 0.01, 0.4) * rotationFactor * tidalFactor;
        baseStrength *= RandomUtils.rollRange(0.6, 1.5);
        baseStrength = Math.max(0.002, Math.min(0.1, baseStrength));

        field.setStrengthComparedToEarth(baseStrength);
        field.setDynamoType(PlanetaryMagneticField.DynamoType.CORE_DYNAMO);
        field.setDynamoEfficiency(RandomUtils.rollRange(0.1, 0.4)); // Less efficient than planets

        double avgField = baseStrength * EARTH_SURFACE_FIELD_MICROTESLAS;
        double minField = avgField * RandomUtils.rollRange(0.5, 0.8);
        double maxField = avgField * RandomUtils.rollRange(1.3, 2.0);
        field.setSurfaceFieldMicroteslasMin(minField);
        field.setSurfaceFieldMicroteslasMax(maxField);
        field.setSurfaceFieldMicroteslasAvg(avgField);

        // Geometry: moons tend toward simpler dipoles
        field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.DIPOLE);
        field.setDipoleTiltDegrees(RandomUtils.rollRange(2.0, 30.0));
        double moonRadiusKm = moon.getRadius() > 0 ? moon.getRadius() : 500.0;
        field.setMagneticAxisOffsetKm(moonRadiusKm * RandomUtils.rollRange(0.0, 0.15));

        // Temporal: moon dynamos can be somewhat unstable
        field.setTemporalStability(RandomUtils.rollRange(0, 100) < 60
                ? PlanetaryMagneticField.TemporalStability.STABLE
                : PlanetaryMagneticField.TemporalStability.FLUXING);

        // Magnetosphere (small but real)
        field.setMagnetosphereExists(true);
        double moonRadiusM = moon.getRadius() * 1000.0;
        double magneticMoment = EARTH_MAGNETIC_MOMENT * baseStrength
                * Math.pow(moonRadiusM / 6.371e6, 3);
        field.setMagneticMoment(magneticMoment);

        // Magnetopause: depends on parent planet's field pressure, not stellar wind
        // Moon inside parent magnetosphere experiences parent's field as "wind"
        double parentFieldAtMoon = estimateParentFieldAtMoon(parentPlanet, moon);
        if (parentFieldAtMoon > 0) {
            // Parent field pressure ~ B²/2μ₀
            double mu0 = 4e-7 * Math.PI;
            double parentPressure = (parentFieldAtMoon * 1e-6) * (parentFieldAtMoon * 1e-6) / (2 * mu0);
            double standoffM = Math.pow(1e-7 * magneticMoment * magneticMoment / (2 * parentPressure), 1.0 / 6.0);
            double standoffRadii = standoffM / moonRadiusM;
            field.setMagnetopauseDistancePlanetRadii(Math.max(1.2, Math.min(10.0, standoffRadii)));
        } else {
            field.setMagnetopauseDistancePlanetRadii(RandomUtils.rollRange(1.5, 5.0));
        }

        // Protection: moon dynamos provide limited but real protection
        field.setShieldsFromStellarWind(true);
        field.setShieldsFromCosmicRays(baseStrength > 0.01);
        field.setProtectionLevel(baseStrength > 0.02
                ? PlanetaryMagneticField.ProtectionLevel.MODERATE
                : PlanetaryMagneticField.ProtectionLevel.MINIMAL);
        field.setAtmosphericLossRateFactor(RandomUtils.rollRange(2.0, 8.0));

        // Auroras: possible if moon has atmosphere
        field.setHasAuroras(Boolean.TRUE.equals(moon.getHasAtmosphere()));
        if (field.getHasAuroras()) {
            field.setAuroralZoneLatitudeDegrees(RandomUtils.rollRange(50.0, 80.0));
            field.setAuroralFrequency(PlanetaryMagneticField.AuroralFrequency.OCCASIONAL);
            field.setAuroralIntensity(PlanetaryMagneticField.AuroralIntensity.FAINT);
        }

        field.setHasRadiationBelts(false); // Too small for radiation belts
        field.setSurfacePowerFluxWattsPerM2(baseStrength * RandomUtils.rollRange(0.001, 0.01));
    }

    private void generateMoonInducedField(PlanetaryMagneticField field, Moon moon,
                                          Planet parentPlanet) {
        field.setDynamoType(PlanetaryMagneticField.DynamoType.INDUCED);
        field.setDynamoEfficiency(0.0); // No internal dynamo

        // Induced field strength depends on parent field at moon's orbit and ocean conductivity
        double parentFieldNT = estimateParentFieldAtMoon(parentPlanet, moon);
        // Induced field is typically 10-50% of the ambient field
        double inductionEfficiency = RandomUtils.rollRange(0.1, 0.5);

        // Ocean depth and salinity affect conductivity
        if (moon.getOceanDepthKm() != null && moon.getOceanDepthKm() > 50) {
            inductionEfficiency *= 1.3; // Deep ocean = better conductor
        }

        double inducedFieldNT = parentFieldNT * inductionEfficiency;
        double strengthEarth = inducedFieldNT / (EARTH_SURFACE_FIELD_MICROTESLAS * 1000.0); // nT to Earth units

        // Europa: ~200-300 nT induced field, Earth surface ~50,000 nT → ~0.004-0.006 Earth
        strengthEarth = Math.max(0.0001, Math.min(0.01, strengthEarth));

        field.setStrengthComparedToEarth(strengthEarth);
        field.setSurfaceFieldMicroteslasAvg(inducedFieldNT / 1000.0); // nT to µT
        field.setSurfaceFieldMicroteslasMin(field.getSurfaceFieldMicroteslasAvg() * 0.3);
        field.setSurfaceFieldMicroteslasMax(field.getSurfaceFieldMicroteslasAvg() * 1.8);

        // Induced fields are time-varying — they oscillate as moon moves through parent field
        field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.DIPOLE);
        field.setDipoleTiltDegrees(RandomUtils.rollRange(0.0, 90.0)); // Orientation varies with orbit
        field.setTemporalStability(PlanetaryMagneticField.TemporalStability.FLUXING);

        // Flux period matches orbital period (field changes as moon orbits through parent magnetosphere)
        if (moon.getOrbitalPeriodDays() != null) {
            field.setFluxPeriodHours((int) (moon.getOrbitalPeriodDays() * 24));
        }
        field.setFluxAmplitudePercent(RandomUtils.rollRange(40.0, 80.0));
        field.setFluxPeakMicroteslas(field.getSurfaceFieldMicroteslasMax());
        field.setFluxLowMicroteslas(field.getSurfaceFieldMicroteslasMin());

        // No real magnetosphere — induced field doesn't create a cavity
        field.setMagnetosphereExists(false);
        field.setMagnetopauseDistancePlanetRadii(null);
        field.setShieldsFromStellarWind(false);
        field.setShieldsFromCosmicRays(false);
        field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.NONE);
        field.setAtmosphericLossRateFactor(RandomUtils.rollRange(8.0, 15.0));

        field.setHasAuroras(false);
        field.setHasRadiationBelts(false);
        field.setMagneticMoment(0.0);
        field.setSurfacePowerFluxWattsPerM2(0.0);
    }

    private void generateMoonRemnantField(PlanetaryMagneticField field, Moon moon) {
        field.setDynamoType(PlanetaryMagneticField.DynamoType.REMNANT);
        field.setDynamoEfficiency(0.0); // Dynamo is dead

        // Remnant fields are very weak and patchy
        // Earth's Moon: 1-100 nT in spots, mostly < 10 nT → ~0.0001 Earth
        double baseStrength = RandomUtils.rollRange(0.00005, 0.001);
        field.setStrengthComparedToEarth(baseStrength);

        double avgField = baseStrength * EARTH_SURFACE_FIELD_MICROTESLAS;
        field.setSurfaceFieldMicroteslasAvg(avgField);
        field.setSurfaceFieldMicroteslasMin(0.0); // Many areas have no field
        field.setSurfaceFieldMicroteslasMax(avgField * RandomUtils.rollRange(5.0, 20.0)); // Patchy hot spots

        // Remnant fields are chaotic and locked into the crust
        field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.CHAOTIC);
        field.setTemporalStability(PlanetaryMagneticField.TemporalStability.STABLE); // Frozen in rock

        // No magnetosphere
        field.setMagnetosphereExists(false);
        field.setMagnetopauseDistancePlanetRadii(null);
        field.setShieldsFromStellarWind(false);
        field.setShieldsFromCosmicRays(false);
        field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.NONE);
        field.setAtmosphericLossRateFactor(RandomUtils.rollRange(10.0, 20.0));

        field.setHasAuroras(false);
        field.setHasRadiationBelts(false);
        field.setMagneticMoment(0.0);
        field.setSurfacePowerFluxWattsPerM2(0.0);

        // Paleomagnetic record exists by definition
        field.setHasPaleomagneticRecord(true);
        if (moon.getAgeMY() != null) {
            field.setOldestMagneticRocksMillionYears(moon.getAgeMY() * RandomUtils.rollRange(0.4, 0.9));
        }
    }

    private void generateMoonNoField(PlanetaryMagneticField field, Moon moon) {
        field.setStrengthComparedToEarth(0.0);
        field.setSurfaceFieldMicroteslasMin(0.0);
        field.setSurfaceFieldMicroteslasMax(0.0);
        field.setSurfaceFieldMicroteslasAvg(0.0);

        field.setDynamoType(PlanetaryMagneticField.DynamoType.NONE);
        field.setDynamoEfficiency(0.0);
        field.setFieldGeometry(PlanetaryMagneticField.FieldGeometry.NONE);
        field.setTemporalStability(PlanetaryMagneticField.TemporalStability.STABLE);

        field.setMagnetosphereExists(false);
        field.setShieldsFromStellarWind(false);
        field.setShieldsFromCosmicRays(false);
        field.setProtectionLevel(PlanetaryMagneticField.ProtectionLevel.NONE);
        field.setAtmosphericLossRateFactor(20.0);

        field.setHasAuroras(false);
        field.setHasRadiationBelts(false);
        field.setMagneticMoment(0.0);
        field.setSurfacePowerFluxWattsPerM2(0.0);
    }

    private double estimateParentFieldAtMoon(Planet parentPlanet, Moon moon) {
        if (parentPlanet.getMagneticField() == null || moon.getSemiMajorAxisKm() == null) return 0;

        PlanetaryMagneticField parentField = parentPlanet.getMagneticField();
        if (parentField.getSurfaceFieldMicroteslasAvg() == null) return 0;

        double surfaceFieldNT = parentField.getSurfaceFieldMicroteslasAvg() * 1000.0; // µT to nT
        double planetRadiusKm = parentPlanet.getRadius();
        double moonDistKm = moon.getSemiMajorAxisKm();

        if (planetRadiusKm <= 0 || moonDistKm <= 0) return 0;

        // Dipole: B(r) = B_surface * (R_planet / r)^3
        double ratio = planetRadiusKm / moonDistKm;
        return surfaceFieldNT * ratio * ratio * ratio;
    }

    /**
     * Returns true for all Neptune-class planet types: Sub-Neptune, Mini-Neptune,
     * Warm Neptune, Hot Neptune. These share ionic fluid interiors and should
     * receive IONIC_FLUID dynamo treatment rather than rocky CORE_DYNAMO.
     */
    private boolean isNeptuneClass(String planetType) {
        return planetType != null && (
                planetType.contains("Sub-Neptune") ||
                planetType.contains("Mini-Neptune") ||
                planetType.contains("Warm Neptune") ||
                planetType.contains("Hot Neptune") ||
                planetType.contains("Ice Giant"));
    }

    private enum MoonFieldType {
        INTRINSIC_DYNAMO,  // Self-sustaining (Ganymede)
        INDUCED,           // From parent magnetosphere (Europa)
        REMNANT,           // Dead ancient field (Luna)
        NONE               // No field at all
    }
}

package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.model.enums.AtmosphereType;
import com.brickroad.starcreator_webservice.model.enums.PlanetType;
import com.brickroad.starcreator_webservice.model.enums.TerrainType;
import com.brickroad.starcreator_webservice.model.planets.MagneticField;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.random.RandomGenerator;

import static com.brickroad.starcreator_webservice.model.constants.PlanetConstants.*;
import static com.brickroad.starcreator_webservice.model.constants.PlanetConstants.SIZE_VALUES;
import static com.brickroad.starcreator_webservice.utils.RandomUtils.getRandomLetter;
import static com.brickroad.starcreator_webservice.utils.RandomUtils.getRandomStringFromTxt;

public class PlanetCreator {

    private static Planet planet;

    public static Planet generateRandomPlanet(String type, String name) {
        planet = new Planet();
        if (StringUtils.isAnyEmpty(type)) {
            planet.setPlanetType(PlanetType.getRandom());
            planet.setType(planet.getPlanetType().getName());
        } else {
            planet.setType(type);
            planet.setPlanetType(PlanetType.getEnum(type));
        }
        if (StringUtils.isAnyEmpty(name)) {
            planet.setName(findName());
        } else {
            planet.setName(name);
        }
        findSize();
        findAtmosphereComposite();
        findDensityAndGravity();
        findAtmosphericPressure();
        findTiltRotate();
        if (!planet.getType().equalsIgnoreCase(PlanetType.GAS.getName())){
            findLiquid();
            findTerrainComposite();
        }
        planet.setOrbitLength((RandomUtils.rollDice(10,10)*RandomUtils.rollD10())/365.0);
        planet.setMagneticField(new MagneticField(planet.getDensityRating(),planet.getRotation()));
        findMoons();

        return planet;
    }

    private static String findName() {
        return new StringBuilder()
                .append(getRandomStringFromTxt(SYSTEM_NAMES_PREFIX_TXT))
                .append(" ")
                .append(getRandomStringFromTxt(SYSTEM_NAMES_SUFFIX_TXT))
                .append(" ")
                .append(getRandomLetter(10))
                .toString();
    }

    private static void findSize(){
        planet.setPlanetSize(RandomGenerator.getDefault().nextInt(planet.getPlanetType().getSizeMin(),planet.getPlanetType().getSizeMax()));
        if (planet.getPlanetSize() <= 1){
            planet.setSize((String) SIZE_VALUES.keySet().toArray()[0]);
        } else if (planet.getPlanetSize() == 2){
            planet.setSize((String) SIZE_VALUES.keySet().toArray()[1]);
        } else if (planet.getPlanetSize() == 3){
            planet.setSize((String) SIZE_VALUES.keySet().toArray()[2]);
        } else if (planet.getPlanetSize() <= 6){
            planet.setSize((String) SIZE_VALUES.keySet().toArray()[3]);
        } else if (planet.getPlanetSize() == 7){
            planet.setSize((String) SIZE_VALUES.keySet().toArray()[4]);
        } else if (planet.getPlanetSize() == 8){
            planet.setSize((String) SIZE_VALUES.keySet().toArray()[5]);
        } else if (planet.getPlanetSize() == 9){
            planet.setSize((String) SIZE_VALUES.keySet().toArray()[6]);
        } else {
            planet.setSize((String) SIZE_VALUES.keySet().toArray()[7]);
        }
        planet.setCircumference(
                RandomUtils.rollDice(
                        (int) SIZE_VALUES.get(planet.getSize())[0],
                        (double) SIZE_VALUES.get(planet.getSize())[1]) * (int) SIZE_VALUES.get(planet.getSize())[2]);
        planet.setRadius(planet.getCircumference() / (2 * Math.PI));
    }

    private static void findAtmosphereComposite() {
        int percentRemaining = 100;
        while (percentRemaining > 0) {
            int percentFound;
            if (percentRemaining != 1) {
                percentFound = RandomGenerator.getDefault().nextInt(1, percentRemaining);
            } else {
                percentFound = 1;
            }
            AtmosphereType randomType = AtmosphereType.getRandom();
            if (planet.getAtmosphere().compositeContainsType(randomType)) {
                planet.getAtmosphere().compositeUpdatePercent(randomType,percentFound);
            } else {
                planet.getAtmosphere().addComposite(randomType, percentFound);
            }
            percentRemaining = percentRemaining - percentFound;
        }
        StringBuilder stringBuilder = new StringBuilder();
        planet.getAtmosphere().getAtmosphericComposite().forEach(sc -> stringBuilder.append(sc.getType().getEffect()).append(", "));
        stringBuilder.delete(stringBuilder.length() - 2,stringBuilder.length());
        planet.getAtmosphere().setDescription(stringBuilder.toString());
    }

    private static void findDensityAndGravity() {
        planet.setDensityRating(planet.getPlanetSize() + RandomUtils.rollD10() - 1);

        if (planet.getDensityRating() < 12) {
            planet.setDensity((String) DENSITY_RATINGS.keySet().toArray()[planet.getDensityRating() - 1]);
        } else {
            planet.setDensity((String) DENSITY_RATINGS.keySet().toArray()[DENSITY_RATINGS.keySet().toArray().length -1]);
        }

        if (planet.getDensityRating() >= 12) {
            planet.setGravity(
                    RandomGenerator.getDefault()
                            .nextDouble(DENSITY_RATINGS.get(planet.getDensity())[0], (planet.getPlanetSize() * DENSITY_RATINGS.get(planet.getDensity())[0])));
        } else if (planet.getDensityRating() == 1) {
            planet.setGravity(0);
        } else {
            planet.setGravity(RandomUtils.getRandomFromArray(DENSITY_RATINGS.get(planet.getDensity())));
        }
    }

    private static void findAtmosphericPressure() {
        double pressureRating = (
                RandomUtils.rollDice(1, 10) - 3) + (planet.getPlanetSize() / 2.0) + (planet.getDensityRating() / 2.0);
        if (Math.floor(pressureRating) >= (ATMOSPHERIC_PRESSURE.keySet().size() - 1)){
            planet.getAtmosphere().setAtmosphereThickness((String) ATMOSPHERIC_PRESSURE.keySet().toArray()[ATMOSPHERIC_PRESSURE.keySet().toArray().length - 1]);
            planet.getAtmosphere().setAtmosphericPressure(pressureRating * ATMOSPHERIC_PRESSURE.get(planet.getAtmosphere().getAtmosphereThickness())[0]);
        } else {
            if ((int) pressureRating<0) pressureRating = 0;
            planet.getAtmosphere().setAtmosphereThickness((String) ATMOSPHERIC_PRESSURE.keySet().toArray()[(int) pressureRating]);
            planet.getAtmosphere().setAtmosphericPressure(RandomUtils.getRandomFromArray(ATMOSPHERIC_PRESSURE.get(planet.getAtmosphere().getAtmosphereThickness())));
        }
    }

    private static void findTiltRotate(){
        planet.setAxisTilt((String) TILTS.keySet().toArray()[RandomGenerator.getDefault().nextInt(0, TILTS.size() - 1)]);
        planet.setTiltDegree(RandomUtils.getRandomFromArray(TILTS.get(planet.getAxisTilt())));
        int rotationMod = planet.getPlanetSize()+(RandomUtils.rollDice(1,10)-1);
        if (rotationMod<3){
            if (rotationMod==1){
                planet.setRotation(RandomUtils.rollDice(1,4));
            } else {
                planet.setRotation(RandomUtils.rollDice(1,8));
            }
        } else if (rotationMod<5){
            planet.setRotation(RandomUtils.rollDice(rotationMod-2,10));
        } else if (rotationMod<12){
            planet.setRotation(RandomUtils.rollDice(rotationMod-1,10));
        } else {
            planet.setRotation(RandomUtils.rollDice(rotationMod,10));
        }
        int roll = RandomUtils.rollDice(1,100);
        if (roll<=70){
            planet.setRotationDir("Prograde");
        } else {
            planet.setRotationDir("Retrograde");
        }
    }

    private static void findLiquid(){
        int roll = RandomUtils.rollDice(1,12);
        planet.getSurface().setLiquidAmt(RandomUtils.getRandomFromArray(LAND_COVER_LIQUID.get(roll)));
        if (planet.getAtmosphere().compositeContainsType(AtmosphereType.EARTH_LIKE)){
            planet.getSurface().setLiquidType("H2O");
        } else {
            String[] types = {"H20","Ammonia","Bromine","Caesium","Francium","Gallium","Liquid Nitrogen",
                    "Liquid Oxygen","Mercury","Rubidium"};
            roll = RandomUtils.rollDice(1,20);
            if (roll<=11){
                planet.getSurface().setLiquidType(types[0]);
            } else {
                planet.getSurface().setLiquidType(types[roll-11]);
            }
        }
    }

    private static void findTerrainComposite() {
        int percentRemaining = 100;
        while (percentRemaining > 0) {
            int percentFound;
            if (percentRemaining != 1) {
                percentFound = RandomGenerator.getDefault().nextInt(1, percentRemaining);
            } else {
                percentFound = 1;
            }
            TerrainType randomType = TerrainType.getRandom();
            if (planet.getSurface().compositeContainsType(randomType)) {
                planet.getSurface().compositeUpdatePercent(randomType,percentFound);
            } else {
                planet.getSurface().addComposite(randomType, percentFound);
            }
            percentRemaining = percentRemaining - percentFound;
        }
    }

    private static void findMoons(){
        int roll = RandomUtils.rollDice(1,20);
        if (roll<=10){
            planet.setOrbitingBodies(new String[0]);
        } else if (roll<=15){
            planet.setOrbitingBodies(new String[1]);
        } else if (roll<20){
            planet.setOrbitingBodies(new String[roll-14]);
        } else {
            planet.setOrbitingBodies(new String[RandomUtils.rollDice(2,4)]);
        }
        for (int i = 0; i < planet.getOrbitingBodies().length; i++) {
            roll = RandomUtils.rollDice(1,20);
            if (roll<=2){
                planet.getOrbitingBodies()[i] = "Dust Cloud";
            } else if (roll==3){
                planet.getOrbitingBodies()[i] = "Gas Cloud";
            } else if (roll<=5){
                planet.getOrbitingBodies()[i] = "Natural Debris";
            } else if (roll<=7){
                planet.getOrbitingBodies()[i] = "Artificial Debris";
            } else if (roll<=16){
                planet.getOrbitingBodies()[i] = "Moon";
            } else if (roll<=18){
                int ringType = RandomUtils.rollDice(1,3)-1;
                String[] ringTypes = {"Ice","Rock","Ice/Rock Mix"};
                planet.getOrbitingBodies()[i] = "Ring of "+ringTypes[ringType];
            } else {
                planet.getOrbitingBodies()[i] = "Artificial Construction";
            }
        }
    }


}

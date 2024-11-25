package com.brickroad.starcreator_webservice.model;

import com.brickroad.starcreator_webservice.model.enums.AtmosphereType;
import com.brickroad.starcreator_webservice.model.enums.PlanetType;
import com.brickroad.starcreator_webservice.model.enums.TerrainType;
import com.brickroad.starcreator_webservice.model.planets.MagneticField;
import com.brickroad.starcreator_webservice.utils.RandomUtils;

import java.util.HashMap;
import java.util.random.RandomGenerator;

import static com.brickroad.starcreator_webservice.model.constants.PlanetConstants.*;
import static com.brickroad.starcreator_webservice.utils.RandomUtils.getRandomLetter;
import static com.brickroad.starcreator_webservice.utils.RandomUtils.getRandomStringFromTxt;

public class Planet extends Body {

    private int planetSize;
    private int densityRating;
    private String axisTilt;
    private double tiltDegree;
    private double rotation;
    private String rotationDir;
    private double liquidAmt;
    private String liquidType;
    private String[] orbitingBodies;
    private String atmosphereThickness;
    private double atmosphericPressure;
    private final String systemName;
    private final PlanetType planetType;

    public Planet(){
        this(PlanetType.getRandom().toString(), "");
    }

    public Planet(String type,String name){
        this.type = type;
        this.planetType = PlanetType.getEnum(type);
        if (name.equalsIgnoreCase("")) {
            this.name = findName();
        } else {
            this.name = name;
        }
        systemName = name;
        findSize();
        findAtmosphereComposite();
        findDensityAndGravity();
        findAtmosphericPressure();
        findTiltRotate();
        if (!type.equalsIgnoreCase("Gas Planet")){
            findLiquid();
            findTerrainComposite();
        }
        magneticField = new MagneticField(densityRating,rotation);
        findMoons();
    }

    private String findName() {
        return new StringBuilder()
                .append(getRandomStringFromTxt(SYSTEM_NAMES_PREFIX_TXT))
                .append(" ")
                .append(getRandomStringFromTxt(SYSTEM_NAMES_SUFFIX_TXT))
                .append(" ")
                .append(getRandomLetter(10))
                .toString();
    }

    private void findMoons(){
        int roll = RandomUtils.rollDice(1,20);
        if (roll<=10){
            orbitingBodies = new String[0];
        } else if (roll<=15){
            orbitingBodies = new String[1];
        } else if (roll<20){
            orbitingBodies = new String[roll-14];
        } else orbitingBodies = new String[RandomUtils.rollDice(2,4)];
        for (int i = 0; i < orbitingBodies.length; i++) {
            roll = RandomUtils.rollDice(1,20);
            if (roll<=2){
                orbitingBodies[i] = "Dust Cloud";
            } else if (roll==3){
                orbitingBodies[i] = "Gas Cloud";
            } else if (roll<=5){
                orbitingBodies[i] = "Natural Debris";
            } else if (roll<=7){
                orbitingBodies[i] = "Artificial Debris";
            } else if (roll<=16){
                orbitingBodies[i] = "Moon";
            } else if (roll<=18){
                int ringType = RandomUtils.rollDice(1,3)-1;
                String[] ringTypes = {"Ice","Rock","Ice/Rock Mix"};
                orbitingBodies[i] = "Ring of "+ringTypes[ringType];
            } else {
                orbitingBodies[i] = "Artificial Construction";
            }
        }
    }

    private void findLiquid(){
        int roll = RandomUtils.rollDice(1,12);
        liquidAmt = RandomUtils.getRandomFromArray(LAND_COVER_LIQUID.get(roll));
        if (atmosphere.containsKey(AtmosphereType.EARTH_LIKE)){
            liquidType = "H2O";
        } else {
            String[] types = {"H20","Ammonia","Bromine","Caesium","Francium","Gallium","Liquid Nitrogen",
                    "Liquid Oxygen","Mercury","Rubidium"};
            roll = RandomUtils.rollDice(1,20);
            if (roll<=11){
                liquidType = types[0];
            } else {
                liquidType = types[roll-11];
            }
        }
    }

    private void findTiltRotate(){
        axisTilt = (String) TILTS.keySet().toArray()[RandomGenerator.getDefault().nextInt(0, TILTS.size() - 1)];
        tiltDegree = RandomUtils.getRandomFromArray(TILTS.get(axisTilt));
        //Rotation(hrs per day)
        int rotationMod = planetSize+(RandomUtils.rollDice(1,10)-1);
        if (rotationMod<3){
            if (rotationMod==1){
                rotation = RandomUtils.rollDice(1,4);
            } else rotation = RandomUtils.rollDice(1,8);
        } else if (rotationMod<5){
            rotation = RandomUtils.rollDice(rotationMod-2,10);
        } else if (rotationMod<12){
            rotation = RandomUtils.rollDice(rotationMod-1,10);
        } else rotation = RandomUtils.rollDice(rotationMod,10);
        //Rotation Direction
        int roll = RandomUtils.rollDice(1,100);
        if (roll<=70){
            rotationDir = "Prograde";
        } else rotationDir = "Retrograde";
    }

    private void findDensityAndGravity() {
        densityRating = planetSize + RandomUtils.rollD10() - 1;

        if (densityRating < 12) {
            density = (String) DENSITY_RATINGS.keySet().toArray()[densityRating - 1];
        } else {
            density = (String) DENSITY_RATINGS.keySet().toArray()[DENSITY_RATINGS.keySet().toArray().length -1];
        }

        if (densityRating >= 12) {
            gravity = RandomGenerator.getDefault().nextDouble(DENSITY_RATINGS.get(density)[0], (planetSize * DENSITY_RATINGS.get(density)[0]));
        } else if (densityRating == 1) {
            gravity = 0;
        } else {
            gravity = RandomUtils.getRandomFromArray(DENSITY_RATINGS.get(density));
        }
    }

    private void findAtmosphericPressure() {
        double pressureRating = (RandomUtils.rollDice(1, 10) - 3) + (planetSize / 2.0) + (densityRating / 2.0);
        if (Math.floor(pressureRating) >= (ATMOSPHERIC_PRESSURE.keySet().size() - 1)){
            atmosphereThickness = (String) ATMOSPHERIC_PRESSURE.keySet().toArray()[ATMOSPHERIC_PRESSURE.keySet().toArray().length - 1];
            atmosphericPressure = pressureRating * ATMOSPHERIC_PRESSURE.get(atmosphereThickness)[0];
        } else {
            if ((int) pressureRating<0) pressureRating = 0;
            atmosphereThickness = (String) ATMOSPHERIC_PRESSURE.keySet().toArray()[(int) pressureRating];
            atmosphericPressure = RandomUtils.getRandomFromArray(ATMOSPHERIC_PRESSURE.get(atmosphereThickness));
        }
    }

    private void findAtmosphereComposite() {
        atmosphere = new HashMap<>();
        int percentRemaining = 100;
        while (percentRemaining > 0) {
            int percentFound;
            if (percentRemaining != 1) {
                percentFound = RandomGenerator.getDefault().nextInt(1, percentRemaining);
            } else {
                percentFound = 1;
            }
            AtmosphereType randomType = AtmosphereType.getRandom();
            if (atmosphere.containsKey(randomType)) {
                atmosphere.compute(randomType, (_, v) -> v + percentFound);
            } else {
                atmosphere.put(randomType, percentFound);
            }
            percentRemaining = percentRemaining - percentFound;
        }
    }

    private void findSize(){
        planetSize = RandomGenerator.getDefault().nextInt(planetType.getSizeMin(),planetType.getSizeMax());
        if (planetSize <= 1){
            size = (String) SIZE_VALUES.keySet().toArray()[0];
        } else if (planetSize == 2){
            size = (String) SIZE_VALUES.keySet().toArray()[1];
        } else if (planetSize == 3){
            size = (String) SIZE_VALUES.keySet().toArray()[2];
        } else if (planetSize <= 6){
            size = (String) SIZE_VALUES.keySet().toArray()[3];
        } else if (planetSize == 7){
            size = (String) SIZE_VALUES.keySet().toArray()[4];
        } else if (planetSize == 8){
            size = (String) SIZE_VALUES.keySet().toArray()[5];
        } else if (planetSize == 9){
            size = (String) SIZE_VALUES.keySet().toArray()[6];
        } else {
            size = (String) SIZE_VALUES.keySet().toArray()[7];
        }
        circumference = RandomUtils.rollDice((int)SIZE_VALUES.get(size)[0],(double)SIZE_VALUES.get(size)[1]) * (int) SIZE_VALUES.get(size)[2];
        radius = (circumference / (2 * Math.PI));
    }

    private void findTerrainComposite() {
        terrain = new HashMap<>();
        int percentRemaining = 100;
        while (percentRemaining > 0) {
            int percentFound;
            if (percentRemaining != 1) {
                percentFound = RandomGenerator.getDefault().nextInt(1, percentRemaining);
            } else {
                percentFound = 1;
            }
            TerrainType randomType = TerrainType.getRandom();
            if (terrain.containsKey(randomType)) {
                terrain.compute(randomType, (_, v) -> v + percentFound);
            } else {
                terrain.put(randomType, percentFound);
            }
            percentRemaining = percentRemaining - percentFound;
        }
    }

    private String printOrbit(){
        StringBuilder sb = new StringBuilder();
        for (String s:orbitingBodies) {
            sb.append("\t").append(s).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return super.toString()+
                "\nAtmosphere: "+atmosphere+
                "\nAtmospheric Pressure: "+ atmosphereThickness +"("+ atmosphericPressure +"APR)"+
                "\nPlanet Density: "+ density+
                "\nGravity: "+String.format("%.2f",gravity)+"g"+
                "\nCircumference: "+circumference+",000 km"+
                "\nTilt: "+ axisTilt +"("+tiltDegree+"*)"+
                "\nRotation: "+rotation+" hr/day "+rotationDir+
                "\nLiquid: "+liquidType+" ("+liquidAmt+"%)"+
                "\nOrbiting Bodies: "+orbitingBodies.length+
                "\n"+printOrbit();
    }

    public String getAxisTilt() {
        return axisTilt;
    }

    public double getTiltDegree() {
        return tiltDegree;
    }

    public double getRotation() {
        return rotation;
    }

    public String getRotationDir() {
        return rotationDir;
    }

    public double getLiquidAmt() {
        return liquidAmt;
    }

    public String getLiquidType() {
        return liquidType;
    }

    public String[] getOrbitingBodies() {
        return orbitingBodies;
    }

    public String getAtmosphereThickness() {
        return atmosphereThickness;
    }

    public double getAtmosphericPressure() {
        return atmosphericPressure;
    }

    public String getSystemName() {
        return systemName;
    }

    public PlanetType getPlanetType() {
        return planetType;
    }

    public int getDensityRating() {
        return densityRating;
    }
}

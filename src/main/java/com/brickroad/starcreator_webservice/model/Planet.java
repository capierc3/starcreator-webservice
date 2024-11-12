package com.brickroad.starcreator_webservice.model;

import com.brickroad.starcreator_webservice.model.enums.AtmosphereType;
import com.brickroad.starcreator_webservice.model.enums.PlanetType;
import com.brickroad.starcreator_webservice.utils.Dice;

import java.util.HashMap;
import java.util.random.RandomGenerator;

import static com.brickroad.starcreator_webservice.model.constants.PlanetConstants.*;

public class Planet extends Body {

    private int planetSize;
    private int densityRating;
    private String axisTilt;
    private double tiltDegree;
    private double rotation;
    private String rotationDir;
    private int liquidAmt;
    private String liquidType;
    private String[] orbitingBodies;
    private String atmosphereThickness;
    private double atmosphericPressure;
    private String systemName;
    private PlanetType planetType;

    public Planet(){
        this(PlanetType.getRandom().toString(),"");
    }

    public Planet(String type,String name){
        this.type = type;
        this.planetType = PlanetType.getEnum(type);
        this.name = name;
        systemName = name;
        findSize();
        findAtmosphereComposite();
        findDensityAndGravity();
        findAtmosphericPressure();
        findTiltRotate();
        if (!type.equalsIgnoreCase("Gas Planet")){
            findLiquid();
        }
        findMoons();
    }

    //TODO moons and space stations
    private void findMoons(){
        int roll = Dice.Roller(1,20);
        if (roll<=10){
            orbitingBodies = new String[0];
        } else if (roll<=15){
            orbitingBodies = new String[1];
        } else if (roll<20){
            orbitingBodies = new String[roll-14];
        } else orbitingBodies = new String[Dice.Roller(2,4)];
        for (int i = 0; i < orbitingBodies.length; i++) {
            roll = Dice.Roller(1,20);
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
                int ringType = Dice.Roller(1,3)-1;
                String[] ringTypes = {"Ice","Rock","Ice/Rock Mix"};
                orbitingBodies[i] = "Ring of "+ringTypes[ringType];
            } else {
                orbitingBodies[i] = "Artificial Construction";
            }
        }
    }

    private void findLiquid(){
        int roll = Dice.Roller(1,12);
        if (roll==1) {
            liquidAmt = 0;
        } else if (roll==2){
            liquidAmt = Dice.Roller(1,5);
        } else if (roll<=11){
            int mod = ((roll-3)*10)+5;
            liquidAmt = Dice.Roller(1,10)+mod;
        } else {
            liquidAmt = Dice.Roller(1,5)+95;
        }
        if (atmosphere.containsKey(AtmosphereType.EARTH_LIKE)){
            liquidType = "H2O";
        } else {
            String[] types = {"H20","Ammonia","Bromine","Caesium","Francium","Gallium","Liquid Nitrogen",
                    "Liquid Oxygen","Mercury","Rubidium"};
            roll = Dice.Roller(1,20);
            if (roll<=11){
                liquidType = types[0];
            } else {
                liquidType = types[roll-11];
            }
        }
    }

    private void findTiltRotate(){
        axisTilt = (String) TILTS.keySet().toArray()[RandomGenerator.getDefault().nextInt(0, TILTS.size() - 1)];
        if (axisTilt.equalsIgnoreCase("None")) {
            tiltDegree = 0;
        } else {
            tiltDegree = RandomGenerator.getDefault().nextDouble(TILTS.get(axisTilt)[0], TILTS.get(axisTilt)[1]);
        }
        //Rotation(hrs per day)
        int rotationMod = planetSize+(Dice.Roller(1,10)-1);
        if (rotationMod<3){
            if (rotationMod==1){
                rotation = Dice.Roller(1,4);
            } else rotation = Dice.Roller(1,8);
        } else if (rotationMod<5){
            rotation = Dice.Roller(rotationMod-2,10);
        } else if (rotationMod<12){
            rotation = Dice.Roller(rotationMod-1,10);
        } else rotation = Dice.Roller(rotationMod,10);
        //Rotation Direction
        int roll = Dice.Roller(1,100);
        if (roll<=70){
            rotationDir = "Prograde";
        } else rotationDir = "Retrograde";
    }

    private void findDensityAndGravity() {
        densityRating = planetSize + Dice.Roller(1,10) - 1;

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
            gravity = RandomGenerator.getDefault().nextDouble(DENSITY_RATINGS.get(density)[0], DENSITY_RATINGS.get(density)[1]);
        }
    }

    private void findAtmosphericPressure(){
        double pressureRating = (Dice.Roller(1, 10) - 3) + (planetSize / 2.0) + (densityRating / 2.0);
        if (Math.floor(pressureRating) >= (ATMOSPHERIC_PRESSURE.keySet().size() - 1)){
            atmosphereThickness = (String) ATMOSPHERIC_PRESSURE.keySet().toArray()[ATMOSPHERIC_PRESSURE.keySet().toArray().length - 1];
            atmosphericPressure = pressureRating * ATMOSPHERIC_PRESSURE.get(atmosphereThickness)[0];
        } else {
            if ((int) pressureRating<0) pressureRating = 0;
            atmosphereThickness = (String) ATMOSPHERIC_PRESSURE.keySet().toArray()[(int) pressureRating];
            atmosphericPressure = RandomGenerator.getDefault().nextDouble(ATMOSPHERIC_PRESSURE.get(atmosphereThickness)[0],ATMOSPHERIC_PRESSURE.get(atmosphereThickness)[1]);
        }
    }

    private void findAtmosphereComposite(){
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
        circumference = Dice.Roller((int) SIZE_VALUES.get(size)[0],(int) SIZE_VALUES.get(size)[1]) * (int) SIZE_VALUES.get(size)[2];
        radius = (circumference / (2 * Math.PI));
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

    public int getLiquidAmt() {
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
}

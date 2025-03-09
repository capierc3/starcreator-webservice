package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.model.*;
import com.brickroad.starcreator_webservice.model.enums.Population;
import com.brickroad.starcreator_webservice.model.enums.StarType;
import com.brickroad.starcreator_webservice.model.enums.SystemType;
import com.brickroad.starcreator_webservice.request.SystemRequest;
import com.brickroad.starcreator_webservice.utils.CreatorUtils;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.brickroad.starcreator_webservice.model.enums.SystemType.MAX_LOW_MASS;
import static com.brickroad.starcreator_webservice.model.enums.SystemType.getRandomSystemType;

public class SystemCreator {

    private static StarSystem system = null;

    public static StarSystem createStarSystem(SystemRequest systemRequest) {
        system = new StarSystem();
        system.setName(StringUtils.isAnyEmpty(systemRequest.getName()) ? CreatorUtils.generateSystemName() : systemRequest.getName());
        system.setPopulation(systemRequest.getPopulation() == null ? Population.getRandom() : systemRequest.getPopulation());
        generateStars(systemRequest.getSystemType());
//        system.setBodies(generateBodies());
//        createBodies();
//        createSize();
//        placeBodies();
//        setBodyNames();
//        createTemp();
//        id = sectorName.toCharArray()[0]+name.toCharArray()[0]+ x + y + Double.toString(z);
        return system;
    }

    private static void generateStars(SystemType systemType) {
        ArrayList<Star> stars = new ArrayList<>();
        Star star = StarCreator.createStar(null,system.getName(),false);
        while (SystemType.TRINARY.equals(systemType) && star.getSolarMass() < MAX_LOW_MASS) {
            star = StarCreator.createStar(null,system.getName(),false);
        }
        stars.add(star);
        system.setSystemType(systemType == null ? getRandomSystemType(star) : systemType);
        if (system.getSystemType().getStarCount() != 1) {
            stars.getFirst().setName(system.getName() + " 1");
            for (int i = 1; i < system.getSystemType().getStarCount(); i++) {
                if (RandomUtils.flipCoin() == 1) {
                    stars.add(StarCreator.createStar(stars.getLast().getStarType(), (system.getName() + " " + (i+1)),  false));
                } else {
                    if (RandomUtils.flipCoin() == 1) {
                        stars.add(StarCreator.createStar(StarType.getStarTypeBelow(stars.getLast().getStarType()), (system.getName() + " " + (i+1)),  false));
                    } else {
                        stars.add(StarCreator.createStar(StarType.getStarTypeAbove(stars.getLast().getStarType()), (system.getName() + " " + (i+1)), false));
                    }
                }
            }
        }
        system.setStars(stars);
    }

    private static ArrayList<Body> generateBodies() {

        return null;
    }

//    private void setBodyNames(){
//        int i = 1;
//        for (Body b:orderSystem) {
//            if (b!=null){
//                b.setName(Integer.toString(i));
//                i++;
//            }
//        }
//    }
//
//    private void createSize(){
//        int sizeMod = RandomUtils.rollDice(1,20)+(bodies.size()+planets.size());
//        if (sizeMod<7){
//            double[] aus = {.25,.33,.5,.66,.75,1};
//            size = aus[sizeMod-1];
//        } else if (sizeMod<9){
//            size = RandomUtils.rollDice(sizeMod-6,4);
//        } else if (sizeMod<11){
//            size = RandomUtils.rollDice(sizeMod-7,6);
//        } else if (sizeMod==11){
//            size = RandomUtils.rollDice(2,8);
//        } else{
//            size = RandomUtils.rollDice(sizeMod-10,10);
//        }
//
//
//    }
//
//    private void createBodies(){
//        int bodyNum = RandomUtils.rollDice(1,10);
//        if (bodyNum == 10) bodyNum = 10+ RandomUtils.rollDice(1,10);
//        bodies = new ArrayList<>();
//        planets = new ArrayList<>();
//        for (int i = 0; i < bodyNum; i++) {
//            int roll = RandomUtils.rollDice(1,20);
//            if (roll <= 1) {
//                bodies.add(new OtherBody("Anomaly",name));
//            } else if (roll <=2){
//                bodies.add(new OtherBody("Structure/Item",name));
//            } else if (roll <=3){
//                bodies.add(new Asteroid("Single/Group",name));
//            } else if (roll <=6){
//                bodies.add(new Asteroid("Belt",name));
//            } else if (roll <=7){
//                bodies.add(new Asteroid("Comet",name));
//            } else if (roll <=8){
//                bodies.add(new OtherBody("Dust Cloud",name));
//            } else if (roll <=9){
//                bodies.add(new OtherBody("Oort Cloud",name));
//            } else if (roll <=12){
//                planets.add(PlanetCreator.generateRandomPlanet("Dwarf Planet",name));
//            } else if (roll <=16){
//                planets.add(PlanetCreator.generateRandomPlanet("Gas Planet",name));
//            } else {
//                planets.add(PlanetCreator.generateRandomPlanet("Terrestrial Planet",name));
//            }
//        }
//    }
//
//    private void placeBodies(){
//        String[] sTypes = {"M", "K", "G", "F", "A", "B", "O"};
//        double[][] goldilocks = {
//                {.51, 200.85},
//                {107, 480},
//                {351, 872.9},
//                {607, 1890},
//                {1357, 4948},
//                {3731, 41133},
//                {32565, 99018},
//        };
//        String sType = "";
//        int typeLoc = 0;
//        boolean main = false;
//        for (Star s : stars) {
//            if (s.type.contains("Main Sequence")) {
//                main = true;
//                for (int i = 0; i < sTypes.length; i++) {
//                    sType = s.type.replace("Main Sequence(", "");
//                    sType = sType.replace(")", "");
//                    if (sType.equalsIgnoreCase(sTypes[i])) {
//                        if (typeLoc < i) {
//                            typeLoc = i;
//                        }
//                    }
//                }
//            }
//        }
//        ArrayList<String> fullTypes = new ArrayList<>() {{
//            add("Anomaly");
//            add("Structure/Item");
//            add("Dwarf Planet");}};
//        ArrayList<String> hotHabitTypes = new ArrayList<>(Arrays.asList("Single/Group","Belt","Terrestrial Planet"));
//        ArrayList<Double> placedLocs = new ArrayList<>();
//        double lsSize = SpaceTravel.AUtoLS(size);
//        if (main){
//            habitLow = goldilocks[typeLoc][0];
//            habitHigh = goldilocks[typeLoc][1];
//        } else {
//            habitLow = 0;
//            habitHigh = lsSize/2;
//        }
//        for (Body b:bodies) {
//            if (fullTypes.contains(b.type)){
//                b.distanceSun = SpaceTravel.LStoAU(placeInSystem(placedLocs,0,lsSize));
//            } else if (hotHabitTypes.contains(b.type)){
//                b.distanceSun = SpaceTravel.LStoAU(placeInSystem(placedLocs,0,habitHigh+((habitHigh-habitLow)/2)));
//            } else {
//                b.distanceSun = SpaceTravel.LStoAU(placeInSystem(placedLocs,habitHigh,lsSize));
//            }
//        }
//        for (Body b:planets) {
//            if (fullTypes.contains(b.type)){
//                b.distanceSun = SpaceTravel.LStoAU(placeInSystem(placedLocs,0,lsSize));
//            } else if (hotHabitTypes.contains(b.type)){
//                b.distanceSun = SpaceTravel.LStoAU(placeInSystem(placedLocs,0,habitHigh+((habitHigh-habitLow)/2)));
//            } else {
//                b.distanceSun = SpaceTravel.LStoAU(placeInSystem(placedLocs,habitHigh,lsSize));
//            }
//        }
//        orderSystem = bodies;
//        orderSystem.addAll(planets);
//        orderSystem.sort(Body::compareTo);
//    }
//
//    private double placeInSystem(ArrayList<Double> locs,double low, double high){
//        double dist = RandomUtils.rollDice(1,high-low)+low;
//        if (!locs.contains(dist)){
//            return dist;
//        } else {
//            return placeInSystem(locs,low,high);
//        }
//    }
//
//    private double[] RollTemps(){
//        return new double[]{
//                (401 + (RandomUtils.rollDice(4, 4) * 100)),
//                300 + RandomUtils.rollDice(1, 100),
//                200 + RandomUtils.rollDice(1, 100),
//                100 + RandomUtils.rollDice(1, 100),
//                50 + RandomUtils.rollDice(1, 50),
//                RandomUtils.rollDice(1, 50),
//                RandomUtils.rollDice(1, 50) * -1,
//                (RandomUtils.rollDice(1, 50) + 50) * -1,
//                (100 + RandomUtils.rollDice(1, 100)) * -1,
//                (200 + RandomUtils.rollDice(1, 100)) * -1,
//                (300 + RandomUtils.rollDice(1, 100)) * -1,
//                RandomUtils.rollDice(4, 4) * -100,
//        };
//    }
//
//    private void createTemp() {
//        if (habitLow!=0){
//            double prevTemp = 2002;
//            double newTemp;
//            for (Body b:orderSystem) {
//                double lsDist = SpaceTravel.AUtoLS(b.distanceSun);
//                double range;
//                double rangeLow;
//                int tableMod;
//                if (lsDist<habitLow){
//                    range = habitLow/4;
//                    rangeLow = 0;
//                    tableMod = -1;
//                } else if (lsDist<=habitHigh){
//                    range = (habitHigh-habitLow)/4;
//                    rangeLow = habitLow;
//                    tableMod = 3;
//                } else {
//                    range = (SpaceTravel.AUtoLS(size)-habitHigh)/4;
//                    rangeLow = habitHigh;
//                    tableMod = 7;
//                }
//                for (int i = 1; i <= 4; i++) {
//                    double[] temps = RollTemps();
//                    if (lsDist<=(range*i)+rangeLow) {
//                        b.setTemp(temps[i+tableMod]);
//                        break;
//                    }
//                }
//            }
//        }
//    }

}

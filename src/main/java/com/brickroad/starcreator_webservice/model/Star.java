package com.brickroad.starcreator_webservice.model;

import com.brickroad.starcreator_webservice.utils.RandomUtils;

/**
 * Class for the creation and holding of the Stars in the system
 */
public class Star extends Body {

    /**Index value of the type String*/
    private int typeNum;
    /**Mass of the star, solarMass X 1030 = Mass in kg*/
    private double solarMass;
    /**Radius of the Star in relation to earth, 6371X(100 X solarRadius)km*/
    private double solarRadius;
    /**Array that holds the int value of the type(typeNum) and the type String*/
    private final Object[][] starTypes = {
            {1,"Proto"},
            {2,"T Tauri"},
            {3,"Main Sequence"},
            {14,"Red Giant"},
            {15,"White Dwarf"},
            {17,"Red Dwarf"},
            {19,"Neutron"},
            {20,"Supergiant"}};

    /**
     * Constructor for the Star that creates a random star and sets the type, if its a main sequence star it runs the
     * setup for finding the type.
     */
    public Star(String name) {
        this.name = name;
        systemName = name;
        int roll = RandomUtils.rollDice(1,20);
        for (int i = 0; i < starTypes.length; i++) {
            if (roll >= (int) starTypes[i][0]) {
                type = (String) starTypes[i][1];
                typeNum = i;
            }
        }
        if (typeNum == 2){
            mainSeq();
        }
    }
    /**
     * Constructor for the Star that creates a random star based on the type a star it shares the system with, making
     * it one lower in rating or one higher only. Then sets the type, if its a main sequence star it runs the
     * setup for finding the type.
     */
    public Star(int num, int i, String systemName){
        if (num<0){
            this.type = (String) starTypes[starTypes.length-1][1];
            this.typeNum = starTypes.length-1;
        } else if (num>=starTypes.length){
            this.type = (String) starTypes[0][1];
            this.typeNum = 0;
        } else {
            this.type = (String) starTypes[num][1];
            this.typeNum = num;
        }
        if (typeNum == 2){
            mainSeq();
        }
        name = systemName+" "+i;
        this.systemName = systemName;
    }

    /**
     * Randomly assigns the type of main sequence star based on star type probability. Then sets it radius and mass.
     */
    private void mainSeq(){
        int roll = RandomUtils.rollD100();
        if (roll<=3){
            type = type+ "(O)";
            solarMass = 16;
            solarRadius = 6;
        } else if (roll<=6){
            type = type+ "(B)";
            int r2 = RandomUtils.rollD8();
            int r3 = RandomUtils.rollD8();
            solarMass = r2+r3;
            solarRadius = RandomUtils.rollD6();
        } else if (roll<=9){
            type = type+ "(A)";
            int r2 = RandomUtils.rollD4();
            int r3 = RandomUtils.rollD4();
            solarMass = r2+r3;
            solarRadius = RandomUtils.rollD4();
        } else if (roll<=15){
            solarMass = RandomUtils.rollD4();
            solarRadius = 2;
            type = type+ "(F)";
        } else if (roll<=20){
            type = type+ "(G)";
            solarRadius = 1;
            solarMass = 1;
        } else if (roll<=30){
            type = type+ "(K)";
            solarRadius = .5;
            solarMass = .5;
        } else {
            type = type+ "(M)";
            solarRadius = .25;
            solarMass = .25;
        }
    }

    /**
     * Getter for the typeNum of the star
     * @return int
     */
    public int getTypeNum() {
        return typeNum;
    }

    /**
     * Getter for the Solar Mass of the star
     * @return double
     */
    public double getSolarMass() {
        return solarMass;
    }

    /**
     * Getter for the Solar Radius of the star
     * @return double
     */
    public double getSolarRadius() {
        return solarRadius;
    }


}

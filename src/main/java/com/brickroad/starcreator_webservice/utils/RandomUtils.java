package com.brickroad.starcreator_webservice.utils;

import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;

public class RandomUtils {

    private final int d;
    private final double dD;
    private int roll;
    private double dRoll;

    public RandomUtils(int dSize){
        this.d=dSize;
        this.dD=dSize;
    }
    public RandomUtils(double dSize){
        this.dD = dSize;
        this.d = (int) dSize;
    }

    public int Roll(){
        Random r = new Random();
        int low = 1;
        int high = this.d+1;
        this.roll = r.nextInt(high - low)+low;
        return this.roll;
    }
    public double Roll(int x){
        Random r = new Random();
        double low = 1.0;
        double high = this.dD+1.0;
        this.dRoll = low + (high - low) * r.nextDouble();
        return this.dRoll;
    }

    public int getRoll() {
        if (roll==0){
            Roll();
        }
        return roll;
    }

    public static int Roller(int times, int sides){
        RandomUtils die = new RandomUtils(sides);
        int value = 0;
        for (int i = 0; i < times; i++) {
            value = value + die.Roll();
        }
        return value;
    }
    public static double Roller(int times,double sides){
        RandomUtils die = new RandomUtils(sides);
        double value = 0;
        for (int i = 0; i < times; i++) {
            value = value + die.Roll(1);
        }
        return value;
    }

    public static int rollRange(int low, int high) {
        RandomGenerator randomGenerator = RandomGenerator.getDefault();
        return randomGenerator.nextInt(low,high);
    }

    public static Double getRandomFromArray(Integer[] array){
        if (Objects.equals(array[0], array[1])) {
            return  array[0].doubleValue();
        } else {
            return RandomGenerator.getDefault().nextDouble(array[0],array[1]);
        }
    }

    public static Double getRandomFromArray(Double[] array){
        if (Objects.equals(array[0], array[1])) {
            return  array[0];
        } else {
            return RandomGenerator.getDefault().nextDouble(array[0],array[1]);
        }
    }
}

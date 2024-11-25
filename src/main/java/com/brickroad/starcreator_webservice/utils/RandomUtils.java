package com.brickroad.starcreator_webservice.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

public class RandomUtils {

    public static int rollD100() {
        return rollDice(100);
    }

    public static int rollD20() {
        return rollDice(20);
    }

    public static int rollD12() {
        return rollDice(12);
    }

    public static int rollD10() {
        return rollDice(10);
    }

    public static int rollD8() {
        return rollDice(8);
    }

    public static int rollD6() {
        return rollDice(6);
    }

    public static int rollD4() {
        return rollDice(4);
    }

    public static int flipCoin() {
        return rollDice(2);
    }

    public static int rollDice(int sides) {
        return RandomGenerator.getDefault().nextInt(1, sides);
    }

    public static int rollDice(int times, int sides) {
        int value = 0;
        for (int i = 0; i < times; i++) {
            value = value + RandomGenerator.getDefault().nextInt(1, sides);
        }
        return value;
    }

    public static double rollDice(double sides) {
        return RandomGenerator.getDefault().nextDouble(1, sides);
    }

    public static double rollDice(int times, double sides) {
        double value = 0;
        for (int i = 0; i < times; i++) {
            value = value + RandomGenerator.getDefault().nextDouble(1, sides);
        }
        return value;
    }

    public static int rollRange(int low, int high) {
        RandomGenerator randomGenerator = RandomGenerator.getDefault();
        return randomGenerator.nextInt(low, (high + 1));
    }

    public static double rollRange(double low, double high) {
        RandomGenerator randomGenerator = RandomGenerator.getDefault();
        return randomGenerator.nextDouble(low, high);
    }

    public static Double getRandomFromArray(Integer[] array) {
        if (Objects.equals(array[0], array[1])) {
            return array[0].doubleValue();
        } else {
            return RandomGenerator.getDefault().nextDouble(array[0], array[1]);
        }
    }

    public static Double getRandomFromArray(Double[] array) {
        if (Objects.equals(array[0], array[1])) {
            return array[0];
        } else {
            return RandomGenerator.getDefault().nextDouble(array[0], array[1]);
        }
    }

    public static String getStringFromArray(Object[][] randomMap) {
        int randNum = RandomUtils.rollDice((Integer) randomMap[randomMap.length - 1][0]);
        for (Object[] entry : randomMap) {
            if (randNum <= (Integer) entry[0]) {
                return (String) entry[1];
            }
        }
        return "Error";
    }

    public static String getRandomStringFromTxt(String fileName) {
        try {
            URL fileURL = ClassLoader.getSystemResource(fileName);
            Path filePath = Paths.get(fileURL.toURI());
            List<String> stringList = FileUtils.readLines(filePath.toFile(), Charset.defaultCharset());
            return stringList.get(rollRange(0, stringList.size() - 1));
        } catch (IOException | URISyntaxException e) {
            return "ERROR";
        }
    }

    public static String getRandomLetter(int maxLetter) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return Character.toString(alphabet.toCharArray()[rollRange(0,maxLetter - 1)]);
    }


}


package com.brickroad.starcreator_webservice.utils;

import static com.brickroad.starcreator_webservice.utils.RandomUtils.getRandomStringFromTxt;

public class CreatorUtils {

    public static final String SYSTEM_NAMES_PREFIX_TXT = "static/SystemNamesPrefix.txt";
    public static final String SYSTEM_NAMES_SUFFIX_TXT = "static/SystemNamesSuffix.txt";

    public static String generateSystemName(){
        return generateSectorName() + " " +
                getRandomStringFromTxt(SYSTEM_NAMES_SUFFIX_TXT);
    }

    public static String generateSystemName(String sectorName){
        return sectorName + " " +
                getRandomStringFromTxt(SYSTEM_NAMES_SUFFIX_TXT);
    }

    public static String generateSectorName(){
        return getRandomStringFromTxt(SYSTEM_NAMES_PREFIX_TXT);
    }

}

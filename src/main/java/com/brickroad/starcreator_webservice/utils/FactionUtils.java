package com.brickroad.starcreator_webservice.utils;

public class FactionUtils {

    private static final String[] ALIGNMENT_ROWS = {"Good", "Neutral", "Evil"};
    private static final String[] ALIGNMENT_COLS = {"Lawful", "Neutral", "Chaotic"};

    public static String getRandomAlignment() {
        return ALIGNMENT_ROWS[RandomUtils.rollRange(0, ALIGNMENT_ROWS.length - 1)] + ALIGNMENT_COLS[RandomUtils.rollRange(0, ALIGNMENT_COLS.length - 1)];
    }


}

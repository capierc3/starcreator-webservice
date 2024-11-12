package com.brickroad.starcreator_webservice.model.enums;

import com.brickroad.starcreator_webservice.utils.Dice;
import org.apache.commons.text.WordUtils;

public enum AtmosphereType {

    AMMONIA("Toxic"),
    ARGON("Inert"),
    CARBON_DIOXIDE("Greenhouse"),
    CHLORINE("Corrosive"),
    EARTH_LIKE("Standard"),
    HELIUM("Inert"),
    HYDROGEN("Combustive"),
    METHANE("Toxic"),
    NITROGEN("Suffocating"),
    OXYGEN("Combustive"),
    SULPHUR("Volcanic");


    private final String effect;

    AtmosphereType(String effect) {
        this.effect = effect;
    }

    public static AtmosphereType getRandom() {
        return AtmosphereType.values()[Dice.Roller(1,AtmosphereType.values().length) - 1];
    }

    public String getEffect() {
        return effect;
    }

    @Override
    public String toString() {
        return WordUtils.capitalize(super.toString().replace("_", " "));
    }
}

package com.brickroad.starcreator_webservice.utils.atmospheres;

import com.brickroad.starcreator_webservice.enums.AtmosphereGas;

public record AtmosphereComponent(AtmosphereGas gas, double percentage) {

    public String getFormula() {
        return gas.getFormula();
    }

    public String getEffect() {
        return gas.getEffect();
    }

    @Override
    public String toString() {
        if (percentage >= 1.0) {
            return String.format("%s %.1f%%", gas.getFormula(), percentage);
        } else if (percentage >= 0.1) {
            return String.format("%s %.2f%%", gas.getFormula(), percentage);
        } else {
            return String.format("%s (trace)", gas.getFormula());
        }
    }
}

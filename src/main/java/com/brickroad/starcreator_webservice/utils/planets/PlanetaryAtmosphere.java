package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.AtmosphereComponent;
import com.brickroad.starcreator_webservice.enums.AtmosphereClassification;
import com.brickroad.starcreator_webservice.enums.AtmosphereGas;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record PlanetaryAtmosphere(List<AtmosphereComponent> components, AtmosphereClassification classification) {

    public double getPercentage(AtmosphereGas gas) {
        return components.stream()
                .filter(c -> c.gas() == gas)
                .mapToDouble(AtmosphereComponent::percentage)
                .sum();
    }

    public boolean isBreathable() {
        double o2 = getPercentage(AtmosphereGas.OXYGEN);
        double co2 = getPercentage(AtmosphereGas.CARBON_DIOXIDE);
        double toxics = getToxicPercentage();

        // Earth-like: 16-25% O2, <5% CO2, minimal toxics
        return o2 >= 16.0 && o2 <= 25.0 && co2 < 5.0 && toxics < 1.0;
    }

    public double getToxicPercentage() {
        return components.stream()
                .filter(c -> c.getEffect().equals("Toxic") || c.getEffect().equals("Corrosive"))
                .mapToDouble(AtmosphereComponent::percentage)
                .sum();
    }

    public AtmosphereClassification getClassification() {
        return classification;
    }

    public String getPrimaryEffect() {
        if (isBreathable()) return "Breathable";
        if (getToxicPercentage() > 10) return "Toxic";
        if (getPercentage(AtmosphereGas.CARBON_DIOXIDE) > 50) return "Greenhouse";
        if (getPercentage(AtmosphereGas.HYDROGEN) > 50) return "Combustive";
        return "Suffocating";
    }

    public String toCompactString() {
        return components.stream()
                .sorted((a, b) -> Double.compare(b.percentage(), a.percentage()))
                .map(AtmosphereComponent::toString)
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return classification.getName() + ": " + toCompactString();
    }

    public static class Builder {
        private final List<AtmosphereComponent> components = new ArrayList<>();
        private AtmosphereClassification classification = AtmosphereClassification.CUSTOM;

        public Builder addGas(AtmosphereGas gas, double percentage) {
            AtmosphereComponent component = new AtmosphereComponent();
            component.setGas(gas);
            component.setGasFormula(gas.getFormula());
            component.setPercentage(percentage);
            component.setIsTrace(percentage < 0.1);
            components.add(component);
            return this;
        }

        public Builder classification(AtmosphereClassification classification) {
            this.classification = classification;
            return this;
        }

        public PlanetaryAtmosphere build() {
            double total = components.stream()
                    .mapToDouble(AtmosphereComponent::getPercentage)
                    .sum();

            if (total > 0 && Math.abs(total - 100.0) > 0.01) {
                double normalizationFactor = 100.0 / total;
                components.forEach(component -> {
                    double normalizedPercentage = component.getPercentage() * normalizationFactor;
                    component.setPercentage(normalizedPercentage);
                    component.setIsTrace(normalizedPercentage < 0.1);
                });
            }

            return new PlanetaryAtmosphere(components, classification);
        }
    }
}

package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.enums.CompositionClassification;
import com.brickroad.starcreator_webservice.enums.CompositionMineral;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Getter
public class PlanetaryComposition {
    private CompositionClassification classification;
    private List<CompositionComponent> interiorComponents;
    private List<CompositionComponent> envelopeComponents;

    @Getter
    public static class CompositionComponent {
        private CompositionMineral mineral;
        private double percentage;

        public CompositionComponent(CompositionMineral mineral, double percentage) {
            this.mineral = mineral;
            this.percentage = percentage;
        }

    }

    public static class Builder {
        private CompositionClassification classification;
        private List<CompositionComponent> interiorComponents = new ArrayList<>();
        private List<CompositionComponent> envelopeComponents = new ArrayList<>();

        public Builder classification(CompositionClassification classification) {
            this.classification = classification;
            return this;
        }

        public void addInteriorMineral(CompositionMineral mineral, double percentage) {
            interiorComponents.add(new CompositionComponent(mineral, percentage));
        }

        public void addEnvelopeMineral(CompositionMineral mineral, double percentage) {
            envelopeComponents.add(new CompositionComponent(mineral, percentage));
        }

        public PlanetaryComposition build() {
            PlanetaryComposition composition = new PlanetaryComposition();
            composition.classification = this.classification;
            composition.interiorComponents = this.interiorComponents;
            composition.envelopeComponents = this.envelopeComponents;
            return composition;
        }
    }

    public String toInteriorString() {
        if (interiorComponents.isEmpty()) return "Unknown";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < interiorComponents.size(); i++) {
            CompositionComponent comp = interiorComponents.get(i);
            sb.append(comp.mineral.getDisplayName())
                    .append(String.format(" %.1f%%", comp.percentage));
            if (i < interiorComponents.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    public String toEnvelopeString() {
        if (envelopeComponents.isEmpty()) return "Unknown";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < envelopeComponents.size(); i++) {
            CompositionComponent comp = envelopeComponents.get(i);
            sb.append(comp.mineral.getDisplayName())
                    .append(String.format(" %.1f%%", comp.percentage));
            if (i < envelopeComponents.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

}

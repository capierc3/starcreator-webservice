package com.brickroad.starcreator_webservice.model.enums;

import com.brickroad.starcreator_webservice.utils.RandomUtils;

import java.util.List;

public enum Population {
    UNINHABITED(List.of("Uninhabited", "Ruins", "Silent"), new int[]{65, 20, 10, 5}),
    LOW_POPULATION(List.of("Frontier", "Wilderness", "Outposts"), new int[]{50, 30, 15, 5}),
    MODERATELY_POPULATED(List.of("Hub System", "Colonies", "Civilized"), new int[]{30, 25, 25, 20}),
    HIGHLY_POPULATED(List.of("Metropolis", "Conglomerate", "Home World"), new int[]{20, 30, 30, 20});

    private final List<String> description;
    private final int[] weights;

    Population(List<String> description, int[] weights) {
        this.description = description;
        this.weights = weights;
    }

    public List<String> getDescription() {
        return description;
    }

    public int[] getWeights() {
        return weights;
    }

    public static Population getRandom() {
        return Population.values()[RandomUtils.rollDice(1,Population.values().length) - 1];
    }

    public static Population getWeightedRandom(Population population) {
        int totalWeight = 0;
        for (int weight : population.getWeights()) {
            totalWeight += weight;
        }
        int random = RandomUtils.rollRange(1, totalWeight);
        int cumulativeWeight = 0;
        for (int i = 0; i < population.getWeights().length; i++) {
            cumulativeWeight += population.getWeights()[i];
            if (random <= cumulativeWeight) {
                return Population.values()[i];
            }
        }
        throw new IllegalStateException("Failed to select a weighted random Population.");
    }
}

package com.brickroad.starcreator_webservice.model.Prompts;

import com.brickroad.starcreator_webservice.model.factions.Faction;
import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.utils.TarotCard;

public class Prompt {

    private Faction mainFaction;
    private Faction secondaryFaction;
    private Planet planet;
    private TarotCard hero;
    private TarotCard villain;
    private TarotCard statusQuo;
    private TarotCard incitingIncident;
    private TarotCard risingTension;
    private TarotCard falseResolution;
    private TarotCard hiddenObstacle;
    private TarotCard climax;
    private TarotCard resolutionConsequence;

    public Prompt(Faction mainFaction, Faction secondaryFaction, Planet planet) {
        this.mainFaction = mainFaction;
        this.secondaryFaction = secondaryFaction;
        this.planet = planet;
    }

    public Prompt() {}

    public Faction getMainFaction() {
        return mainFaction;
    }

    public void setMainFaction(Faction mainFaction) {
        this.mainFaction = mainFaction;
    }

    public Faction getSecondaryFaction() {
        return secondaryFaction;
    }

    public void setSecondaryFaction(Faction secondaryFaction) {
        this.secondaryFaction = secondaryFaction;
    }

    public Planet getPlanet() {
        return planet;
    }

    public void setPlanet(Planet planet) {
        this.planet = planet;
    }

    public TarotCard getHero() {
        return hero;
    }

    public void setHero(TarotCard hero) {
        this.hero = hero;
    }

    public TarotCard getVillain() {
        return villain;
    }

    public void setVillain(TarotCard villain) {
        this.villain = villain;
    }

    public TarotCard getStatusQuo() {
        return statusQuo;
    }

    public void setStatusQuo(TarotCard statusQuo) {
        this.statusQuo = statusQuo;
    }

    public TarotCard getIncitingIncident() {
        return incitingIncident;
    }

    public void setIncitingIncident(TarotCard incitingIncident) {
        this.incitingIncident = incitingIncident;
    }

    public TarotCard getRisingTension() {
        return risingTension;
    }

    public void setRisingTension(TarotCard risingTension) {
        this.risingTension = risingTension;
    }

    public TarotCard getFalseResolution() {
        return falseResolution;
    }

    public void setFalseResolution(TarotCard falseResolution) {
        this.falseResolution = falseResolution;
    }

    public TarotCard getHiddenObstacle() {
        return hiddenObstacle;
    }

    public void setHiddenObstacle(TarotCard hiddenObstacle) {
        this.hiddenObstacle = hiddenObstacle;
    }

    public TarotCard getClimax() {
        return climax;
    }

    public void setClimax(TarotCard climax) {
        this.climax = climax;
    }

    public TarotCard getResolutionConsequence() {
        return resolutionConsequence;
    }

    public void setResolutionConsequence(TarotCard resolutionConsequence) {
        this.resolutionConsequence = resolutionConsequence;
    }
}

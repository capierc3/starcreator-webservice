package com.brickroad.starcreator_webservice.utils;

public class TarotSpread {

    private TarotCard hero;
    private TarotCard villain;
    private TarotCard statusQuo;
    private TarotCard incitingIncident;
    private TarotCard risingTension;
    private TarotCard falseResolution;
    private TarotCard hiddenObstacle;
    private TarotCard climax;
    private TarotCard resolutionConsequence;

    public TarotSpread() {
        TarotDeck deck = new TarotDeck();
        deck.shuffle();
        this.hero = deck.drawCard(true);
        this.villain = deck.drawCard(false);
        this.statusQuo = deck.drawCard(false);
        this.incitingIncident = deck.drawCard(false);
        this.risingTension = deck.drawCard(false);
        this.falseResolution = deck.drawCard(false);
        this.hiddenObstacle = deck.drawCard(false);
        this.climax = deck.drawCard(false);
        this.resolutionConsequence = deck.drawCard(false);
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

package com.brickroad.starcreator_webservice.model.prompts;

import com.brickroad.starcreator_webservice.model.CelestialBody;
import com.brickroad.starcreator_webservice.model.factions.Faction;
import com.brickroad.starcreator_webservice.model.starSystems.StarSystem;
import com.brickroad.starcreator_webservice.utils.TarotSpread;

public class Prompt {

    private Faction mainFaction;
    private Faction secondaryFaction;
    private StarSystem system;
    private CelestialBody focusPlanet;
    private TarotSpread storySpread;

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

    public CelestialBody getPlanet() {
        return focusPlanet;
    }

    public void setPlanet(CelestialBody planet) {
        this.focusPlanet = planet;
    }

    public StarSystem getSystem() {
        return system;
    }

    public void setSystem(StarSystem system) {
        this.system = system;
    }

    public CelestialBody getFocusPlanet() {
        return focusPlanet;
    }

    public void setFocusPlanet(CelestialBody focusPlanet) {
        this.focusPlanet = focusPlanet;
    }

    public TarotSpread getStorySpread() {
        return storySpread;
    }

    public void setStorySpread(TarotSpread storySpread) {
        this.storySpread = storySpread;
    }
}

package com.brickroad.starcreator_webservice.utils.prompts;

import com.brickroad.starcreator_webservice.entity.ud.CelestialBody;
import com.brickroad.starcreator_webservice.entity.ud.Faction;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.brickroad.starcreator_webservice.utils.tarot.TarotSpread;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Prompt {

    private Faction mainFaction;
    private Faction secondaryFaction;
    private StarSystem system;
    private CelestialBody focusPlanet;
    private TarotSpread storySpread;

    public Prompt() {}

    public CelestialBody getPlanet() {
        return focusPlanet;
    }

    public void setPlanet(CelestialBody planet) {
        this.focusPlanet = planet;
    }

}

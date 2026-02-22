package com.brickroad.starcreator_webservice.utils.prompts;

import com.brickroad.starcreator_webservice.entity.ud.Faction;
import com.brickroad.starcreator_webservice.entity.ud.Person;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.brickroad.starcreator_webservice.utils.tarot.TarotSpread;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Prompt {

    private List<Person> characters;
    private Faction mainFaction;
    private Faction secondaryFaction;
    private StarSystem system;
    private Planet focusPlanet;
    private TarotSpread storySpread;

    public Prompt() {}

    public Planet getPlanet() {
        return focusPlanet;
    }

    public void setPlanet(Planet planet) {
        this.focusPlanet = planet;
    }

}

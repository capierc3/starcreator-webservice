package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.creator.PersonCreator;
import com.brickroad.starcreator_webservice.entity.ud.Person;
import com.brickroad.starcreator_webservice.repository.FactionRepo;
import com.brickroad.starcreator_webservice.repository.GovernmentTypeRepo;
import com.brickroad.starcreator_webservice.utils.prompts.Prompt;
import com.brickroad.starcreator_webservice.request.StarSystemRequest;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.tarot.TarotSpread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PromptService {

    @Autowired
    PersonCreator characterCreator;

    private final FactionRepo factionRepo;
    private final CreationService creationService;

    public PromptService(FactionRepo factionRepo, CreationService creationService, GovernmentTypeRepo governmentTypeRepo) {
        this.factionRepo = factionRepo;
        this.creationService = creationService;
    }

    public Prompt createPrompt() {

        Prompt prompt = new Prompt();

        List<Person> characters = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            characters.add(characterCreator.createPerson());
        }
        prompt.setCharacters(characters);
        prompt.setMainFaction(factionRepo.getRandomFaction());
        prompt.setSecondaryFaction(factionRepo.getRandomFaction());
        prompt.setSystem(creationService.createStarSystem(new StarSystemRequest()));
        if (!prompt.getSystem().getPlanets().isEmpty()) {
            int randPlanetIdx = RandomUtils.rollRange(0,prompt.getSystem().getPlanets().size() - 1);
            prompt.setPlanet(prompt.getSystem().getPlanets().stream().skip(randPlanetIdx).findFirst().orElse(null));
        }
        prompt.setStorySpread(new TarotSpread());

        return prompt;
    }
}

package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.repos.FactionRepo;
import com.brickroad.starcreator_webservice.repos.GovernmentTypeRepo;
import com.brickroad.starcreator_webservice.model.prompts.Prompt;
import com.brickroad.starcreator_webservice.request.StarSystemRequest;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.TarotSpread;
import org.springframework.stereotype.Service;

@Service
public class PromptService {

    private final FactionRepo factionRepo;
    private final CreationService creationService;

    public PromptService(FactionRepo factionRepo, CreationService creationService, GovernmentTypeRepo governmentTypeRepo) {
        this.factionRepo = factionRepo;
        this.creationService = creationService;
    }

    public Prompt createPrompt() {

        Prompt prompt = new Prompt();

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

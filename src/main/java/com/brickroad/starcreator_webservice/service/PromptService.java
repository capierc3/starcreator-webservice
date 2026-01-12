package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.database.repos.FactionRepo;
import com.brickroad.starcreator_webservice.database.repos.GovernmentTypeRepo;
import com.brickroad.starcreator_webservice.model.Prompts.Prompt;
import com.brickroad.starcreator_webservice.utils.TarotDeck;
import org.springframework.stereotype.Service;

@Service
public class PromptService {

    private final FactionRepo factionRepo;
    private final GovernmentTypeRepo governmentTypeRepo;
    private final CreationService creationService;

    public PromptService(FactionRepo factionRepo, CreationService creationService, GovernmentTypeRepo governmentTypeRepo) {
        this.factionRepo = factionRepo;
        this.creationService = creationService;
        this.governmentTypeRepo = governmentTypeRepo;
    }

    public Prompt createPrompt() {

        Prompt prompt = new Prompt();

        prompt.setMainFaction(factionRepo.getRandomFaction());
        prompt.setMainGovernment(governmentTypeRepo.findById(prompt.getMainFaction().getGovernmentTypeId()));
        prompt.setSecondaryFaction(factionRepo.getRandomFaction());
        prompt.setSecondaryGovernment(governmentTypeRepo.findById(prompt.getSecondaryFaction().getGovernmentTypeId()));
        prompt.setPlanet(creationService.createPlanet(null));

        TarotDeck deck = new TarotDeck();
        deck.shuffle();
        prompt.setHero(deck.drawCard(true));
        prompt.setVillain(deck.drawCard(false));
        prompt.setStatusQuo(deck.drawCard(false));
        prompt.setIncitingIncident(deck.drawCard(false));
        prompt.setRisingTension(deck.drawCard(false));
        prompt.setFalseResolution(deck.drawCard(false));
        prompt.setHiddenObstacle(deck.drawCard(false));
        prompt.setClimax(deck.drawCard(false));
        prompt.setResolutionConsequence(deck.drawCard(false));

        return prompt;
    }
}

package com.brickroad.starcreator_webservice.controller;

import com.brickroad.starcreator_webservice.model.factions.Faction;
import com.brickroad.starcreator_webservice.model.factions.FactionResponse;
import com.brickroad.starcreator_webservice.request.FactionRequest;
import com.brickroad.starcreator_webservice.service.FactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/faction")
public class FactionController {

    private final FactionService factionService;

    public FactionController(FactionService factionService) {
        this.factionService = factionService;
    }

    @Operation(summary = "Get Faction", description = "Gets a faction", tags = {"Star Creation"})
    @ApiResponse(responseCode = "200", description = "Faction Found", content = {@Content(mediaType = "application/json",schema = @Schema(implementation = Faction.class))})
    @GetMapping("/")
    public ResponseEntity<FactionResponse> getFaction(@RequestBody FactionRequest factionRequest) {
        if (isEmptyRequest(factionRequest)) {
            return createResponse(
                    factionService.getRandomFaction(),
                    null
            );
        } else if (!StringUtils.isEmpty(factionRequest.getName())) {
            return createResponse(
                    factionService.getFactionByName(factionRequest.getName()),
                    null
            );
        } else {
            return createResponse(
                    null,
                    factionService.getFactionByTypeAndAlignment(factionRequest.getType(), factionRequest.getAlignment(), factionRequest.getCount())
            );
        }
    }

    private boolean isEmptyRequest(FactionRequest factionRequest) {
        if(factionRequest == null) {
            return true;
        } else {
            return StringUtils.isEmpty(factionRequest.getName()) && StringUtils.isEmpty(factionRequest.getType()) && StringUtils.isEmpty(factionRequest.getAlignment());
        }
    }

    private ResponseEntity<FactionResponse> createResponse(Faction faction, List<Faction> factionList) {
        FactionResponse response = new FactionResponse();
        response.setFaction(faction);
        response.setFactions(factionList);
        return ResponseEntity.ok(response);
    }
}

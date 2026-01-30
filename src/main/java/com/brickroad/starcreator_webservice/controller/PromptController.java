package com.brickroad.starcreator_webservice.controller;

import com.brickroad.starcreator_webservice.utils.prompts.Prompt;
import com.brickroad.starcreator_webservice.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/prompt")
public class PromptController {

    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @Operation(summary = "Create Prompt", description = "Creates a story prompt", tags = {"Star Creation"})
    @ApiResponse(responseCode = "200", description = "Prompt Created", content = {@Content(mediaType = "application/json",schema = @Schema(implementation = Prompt.class))})
    @GetMapping("/")
    public ResponseEntity<Prompt> getPrompt() {
        return ResponseEntity.ok(promptService.createPrompt());
    }

}

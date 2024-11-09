package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.entity.Star;
import com.brickroad.starcreator_webservice.request.CreationRequest;
import org.springframework.stereotype.Service;

@Service
public class CreationService {

    public Star createStar() {
        return new Star("Sol","Yellow Dwarf");
    }

    public Star createStar(CreationRequest input) {
        return new Star(input.getName(),"Yellow Dwarf");
    }

}

package com.brickroad.starcreator_webservice.repos;

import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.model.stars.StarTypeRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlanetRepository extends JpaRepository<Planet, Integer> {

}

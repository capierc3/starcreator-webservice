package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ref.PlanetTypeRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanetTypeRefRepository extends JpaRepository<PlanetTypeRef, Long> {

    @Query("SELECT p FROM PlanetTypeRef p")
    List<PlanetTypeRef> findAllPlanetTypes();

    @Query("SELECT p FROM PlanetTypeRef p WHERE p.formationZone = :zone")
    List<PlanetTypeRef> findByFormationZone(String zone);

    @Query("SELECT p FROM PlanetTypeRef p WHERE p.habitable = true")
    List<PlanetTypeRef> findHabitableTypes();
}

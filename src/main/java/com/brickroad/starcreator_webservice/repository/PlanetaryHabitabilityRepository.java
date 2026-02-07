package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.PlanetaryHabitability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanetaryHabitabilityRepository extends JpaRepository<PlanetaryHabitability, Long> {

    Optional<PlanetaryHabitability> findByPlanetId(Long planetId);

    boolean existsByPlanetId(Long planetId);

    void deleteByPlanetId(Long planetId);
}

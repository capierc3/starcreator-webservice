package com.brickroad.starcreator_webservice.repos;

import com.brickroad.starcreator_webservice.model.planets.PlanetaryMagneticField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for planetary magnetic field data
 * 
 * @author Chase Pierce
 * @version 2.0
 */
@Repository
public interface PlanetaryMagneticFieldRepository extends JpaRepository<PlanetaryMagneticField, Long> {
    
    /**
     * Find magnetic field by planet ID
     */
    Optional<PlanetaryMagneticField> findByPlanetId(Long planetId);
    
    /**
     * Check if magnetic field exists for a planet
     */
    boolean existsByPlanetId(Long planetId);
    
    /**
     * Delete magnetic field by planet ID
     */
    void deleteByPlanetId(Long planetId);
}

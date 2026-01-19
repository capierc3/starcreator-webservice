package com.brickroad.starcreator_webservice.repos;

import com.brickroad.starcreator_webservice.model.atmospheres.AtmosphereClassification;
import com.brickroad.starcreator_webservice.model.atmospheres.AtmosphereTemplateRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AtmosphereTemplateRefRepository extends JpaRepository<AtmosphereTemplateRef, Long> {

    Optional<AtmosphereTemplateRef> findByClassification(AtmosphereClassification classification);

    @Query("SELECT t FROM AtmosphereTemplateRef t WHERE " +
           "(:temp IS NULL OR (t.minTemperatureK IS NULL OR :temp >= t.minTemperatureK) AND " +
           "                  (t.maxTemperatureK IS NULL OR :temp <= t.maxTemperatureK)) AND " +
           "(:mass IS NULL OR (t.minPlanetMassEarth IS NULL OR :mass >= t.minPlanetMassEarth) AND " +
           "                  (t.maxPlanetMassEarth IS NULL OR :mass <= t.maxPlanetMassEarth))")
    List<AtmosphereTemplateRef> findMatchingTemplates(@Param("temp") Double temperatureK, 
                                                       @Param("mass") Double planetMassEarth);

    @Query("SELECT t FROM AtmosphereTemplateRef t WHERE " +
           "t.classification IN ('JOVIAN', 'ICE_GIANT') AND " +
           ":mass >= t.minPlanetMassEarth")
    List<AtmosphereTemplateRef> findGasGiantTemplates(@Param("mass") Double planetMassEarth);

    @Query("SELECT t FROM AtmosphereTemplateRef t WHERE " +
           "t.classification IN ('EARTH_LIKE', 'VENUS_LIKE', 'MARS_LIKE', 'VOLCANIC') AND " +
           ":mass <= 10.0")
    List<AtmosphereTemplateRef> findTerrestrialTemplates(@Param("mass") Double planetMassEarth);

    List<AtmosphereTemplateRef> findByIsBreathableTrue();

    @Query("SELECT t FROM AtmosphereTemplateRef t ORDER BY t.rarityWeight DESC")
    List<AtmosphereTemplateRef> findAllOrderedByRarity();
}

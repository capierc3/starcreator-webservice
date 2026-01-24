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
    List<AtmosphereTemplateRef> findMatchingTemplatesOLD(@Param("temp") Double temperatureK,
                                                       @Param("mass") Double planetMassEarth);

    @Query(value = "SELECT t.* FROM ref.atmosphere_template t " +
            "JOIN ref.planet_atmosphere_compatibility pac ON t.classification = pac.atmosphere_classification " +
            "WHERE pac.planet_type = :planetType AND " +
            "(:temp IS NULL OR (t.min_temperature_k IS NULL OR :temp >= t.min_temperature_k) AND " +
            "                  (t.max_temperature_k IS NULL OR :temp <= t.max_temperature_k)) AND " +
            "(:mass IS NULL OR (t.min_planet_mass_earth IS NULL OR :mass >= t.min_planet_mass_earth) AND " +
            "                  (t.max_planet_mass_earth IS NULL OR :mass <= t.max_planet_mass_earth)) " +
            "ORDER BY pac.preference_weight DESC",
            nativeQuery = true)
    List<AtmosphereTemplateRef> findMatchingTemplates(@Param("planetType") String planetType,
                                                      @Param("temp") Double temperatureK,
                                                      @Param("mass") Double planetMassEarth);

    @Query(value = "SELECT t.* FROM ref.atmosphere_template t " +
            "JOIN ref.planet_atmosphere_compatibility pac ON t.classification = pac.atmosphere_classification " +
            "WHERE pac.planet_type = :planetType AND " +
            "(:temp IS NULL OR (t.min_temperature_k IS NULL OR :temp >= t.min_temperature_k) AND " +
            "                  (t.max_temperature_k IS NULL OR :temp <= t.max_temperature_k))" +
            "ORDER BY pac.preference_weight DESC",
            nativeQuery = true)
    List<AtmosphereTemplateRef> findMatchingTemplates(@Param("planetType") String planetType,
                                                      @Param("temp") Double temperatureK);

    @Query(value = "SELECT t.* FROM ref.atmosphere_template t " +
            "JOIN ref.planet_atmosphere_compatibility pac ON t.classification = pac.atmosphere_classification " +
            "WHERE pac.planet_type = :planetType " +
            "ORDER BY pac.preference_weight DESC",
            nativeQuery = true)
    List<AtmosphereTemplateRef> findMatchingTemplates(@Param("planetType") String planetType);

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

package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ref.CloudCompositionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CloudCompositionTemplateRepository extends JpaRepository<CloudCompositionTemplate, Integer> {

    List<CloudCompositionTemplate> findByAtmosphereClassification(String atmosphereClassification);

    @Query("SELECT c FROM CloudCompositionTemplate c WHERE c.atmosphereClassification = :classification " +
           "AND c.minTemperatureK <= :temp AND c.maxTemperatureK >= :temp ORDER BY c.weight DESC")
    List<CloudCompositionTemplate> findByClassificationAndTemperature(String classification, double temp);
}

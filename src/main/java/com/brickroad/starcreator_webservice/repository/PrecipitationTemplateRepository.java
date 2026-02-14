package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ref.PrecipitationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrecipitationTemplateRepository extends JpaRepository<PrecipitationTemplate, Integer> {

    List<PrecipitationTemplate> findByCloudSubstance(String cloudSubstance);

    @Query("SELECT p FROM PrecipitationTemplate p WHERE p.cloudSubstance = :substance " +
           "AND p.surfaceTempMinK <= :surfaceTemp AND p.surfaceTempMaxK >= :surfaceTemp")
    List<PrecipitationTemplate> findBySubstanceAndSurfaceTemp(String substance, double surfaceTemp);
}

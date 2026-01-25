package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ref.GeologicalTemplateRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeologicalTemplateRepository extends JpaRepository<GeologicalTemplateRef, Integer> {

    @Query("SELECT g FROM GeologicalTemplateRef g WHERE g.activityLevel = :activityLevel")
    List<GeologicalTemplateRef> findByActivityLevel(@Param("activityLevel") String activityLevel);

    @Query("SELECT g FROM GeologicalTemplateRef g WHERE " +
            ":activityScore >= g.minActivityScore AND :activityScore <= g.maxActivityScore")
    List<GeologicalTemplateRef> findByActivityScoreRange(@Param("activityScore") Double activityScore);

    @Query("SELECT g FROM GeologicalTemplateRef g WHERE " +
            "(:planetType IS NULL OR g.planetTypes LIKE CONCAT('%', :planetType, '%')) AND " +
            "(:activityScore >= g.minActivityScore AND :activityScore <= g.maxActivityScore)")
    List<GeologicalTemplateRef> findByPlanetTypeAndActivityScore(
            @Param("planetType") String planetType,
            @Param("activityScore") Double activityScore
    );
}
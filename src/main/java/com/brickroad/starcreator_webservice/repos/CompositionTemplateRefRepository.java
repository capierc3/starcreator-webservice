package com.brickroad.starcreator_webservice.repos;

import com.brickroad.starcreator_webservice.model.compositions.CompositionClassification;
import com.brickroad.starcreator_webservice.model.compositions.CompositionTemplateRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompositionTemplateRefRepository extends JpaRepository<CompositionTemplateRef, Integer> {
    
    /**
     * Find all templates ordered by rarity weight (descending)
     */
    @Query("SELECT c FROM CompositionTemplateRef c ORDER BY c.rarityWeight DESC")
    List<CompositionTemplateRef> findAllOrderedByRarity();
    
    /**
     * Find templates that match the given orbital distance
     */
    @Query("SELECT c FROM CompositionTemplateRef c WHERE " +
           "(c.minDistanceAu IS NULL OR :distance >= c.minDistanceAu) AND " +
           "(c.maxDistanceAu IS NULL OR :distance <= c.maxDistanceAu)")
    List<CompositionTemplateRef> findMatchingTemplates(@Param("distance") double distance);
    
    /**
     * Find a specific template by classification
     */
    Optional<CompositionTemplateRef> findByClassification(CompositionClassification classification);
    
    /**
     * Find templates by planet type (contains match on comma-separated list)
     */
    @Query("SELECT c FROM CompositionTemplateRef c WHERE " +
           "c.planetTypes IS NULL OR " +
           "LOWER(c.planetTypes) LIKE LOWER(CONCAT('%', :planetType, '%'))")
    List<CompositionTemplateRef> findByPlanetType(@Param("planetType") String planetType);
}

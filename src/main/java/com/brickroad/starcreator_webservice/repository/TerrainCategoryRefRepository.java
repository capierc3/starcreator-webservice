package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ref.TerrainCategoryRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TerrainCategoryRefRepository extends JpaRepository<TerrainCategoryRef, Integer> {

    Optional<TerrainCategoryRef> findByCategory(String category);

    @Query("SELECT t FROM TerrainCategoryRef t WHERE t.isMajorTerrain = true ORDER BY t.baseWeight DESC")
    List<TerrainCategoryRef> findMajorCategories();

    @Query("SELECT t FROM TerrainCategoryRef t WHERE t.isRare = true")
    List<TerrainCategoryRef> findRareCategories();

    @Query("SELECT t FROM TerrainCategoryRef t ORDER BY t.baseWeight DESC")
    List<TerrainCategoryRef> findAllOrderedByWeight();
}
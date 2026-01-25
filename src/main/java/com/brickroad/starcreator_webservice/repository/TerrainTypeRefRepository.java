package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ref.TerrainTypeRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TerrainTypeRefRepository extends JpaRepository<TerrainTypeRef, Integer> {

    Optional<TerrainTypeRef> findByName(String name);

    List<TerrainTypeRef> findByCategory(String category);

    @Query("SELECT t FROM TerrainTypeRef t WHERE t.category = :category ORDER BY t.rarityWeight DESC")
    List<TerrainTypeRef> findByCategoryOrderedByRarity(@Param("category") String category);

    @Query("SELECT t FROM TerrainTypeRef t WHERE " +
            "(:surfaceTemp IS NULL OR t.minTemperatureK IS NULL OR :surfaceTemp >= t.minTemperatureK) AND " +
            "(:surfaceTemp IS NULL OR t.maxTemperatureK IS NULL OR :surfaceTemp <= t.maxTemperatureK) AND " +
            "(t.requiresWater = false OR :hasWater = true) AND " +
            "(t.requiresAtmosphere = false OR :hasAtmosphere = true)")
    List<TerrainTypeRef> findViableTerrainTypes(
            @Param("surfaceTemp") Double surfaceTemp,
            @Param("hasWater") Boolean hasWater,
            @Param("hasAtmosphere") Boolean hasAtmosphere
    );

    @Query("SELECT t FROM TerrainTypeRef t WHERE t.isVolcanic = true")
    List<TerrainTypeRef> findVolcanicTerrains();

    @Query("SELECT t FROM TerrainTypeRef t WHERE t.isFrozen = true")
    List<TerrainTypeRef> findFrozenTerrains();

    @Query("SELECT t FROM TerrainTypeRef t WHERE t.isAquatic = true")
    List<TerrainTypeRef> findAquaticTerrains();
}
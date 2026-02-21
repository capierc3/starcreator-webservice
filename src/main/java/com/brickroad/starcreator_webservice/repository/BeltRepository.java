package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.OrbitalBand;
import com.brickroad.starcreator_webservice.enums.BandCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeltRepository extends JpaRepository<OrbitalBand, Long> {

    @Query("SELECT b FROM OrbitalBand b WHERE b.starSystem.id = :systemId")
    List<OrbitalBand> findByStarSystemId(@Param("systemId") Long starSystemId);

    @Query("SELECT b FROM OrbitalBand b WHERE b.starSystem.id = :systemId AND b.beltType.code = :typeCode")
    List<OrbitalBand> findByStarSystemIdAndTypeCode(@Param("systemId") Long starSystemId, @Param("typeCode") String typeCode);

    @Query("SELECT b FROM OrbitalBand b WHERE b.starSystem.id = :systemId AND b.bandCategory = :category")
    List<OrbitalBand> findByStarSystemIdAndCategory(@Param("systemId") Long starSystemId, @Param("category") BandCategory category);

    @Query("SELECT b FROM OrbitalBand b WHERE b.planet.id = :planetId AND b.bandCategory = :category")
    List<OrbitalBand> findByPlanetIdAndCategory(@Param("planetId") Long planetId, @Param("category") BandCategory category);
}

package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.Asteroid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsteroidRepository extends JpaRepository<Asteroid, Long> {

    @Query("SELECT a FROM Asteroid a WHERE a.band.id = :bandId ORDER BY a.physicalProperties.earthMass DESC")
    List<Asteroid> findByBandIdOrderByMassDesc(@Param("bandId") Long bandId);

    @Query("SELECT a FROM Asteroid a WHERE a.band.id = :bandId ORDER BY a.physicalProperties.earthMass DESC")
    List<Asteroid> findNotableByBandId(@Param("bandId") Long bandId);
}

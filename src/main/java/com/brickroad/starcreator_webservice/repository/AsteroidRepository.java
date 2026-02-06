package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.Asteroid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsteroidRepository extends JpaRepository<Asteroid, Long> {

    @Query("SELECT a FROM Asteroid a WHERE a.belt.id = :beltId ORDER BY a.mass DESC")
    List<Asteroid> findByBeltIdOrderByMassDesc(@Param("beltId") Long beltId);

    @Query("SELECT a FROM Asteroid a WHERE a.belt.id = :beltId AND a.isNotable = true ORDER BY a.mass DESC")
    List<Asteroid> findNotableByBeltId(@Param("beltId") Long beltId);
}

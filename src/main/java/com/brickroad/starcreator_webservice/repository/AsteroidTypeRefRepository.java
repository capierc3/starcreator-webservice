package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ref.AsteroidTypeRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsteroidTypeRefRepository extends JpaRepository<AsteroidTypeRef, Integer> {

    @Query("SELECT a FROM AsteroidTypeRef a")
    List<AsteroidTypeRef> findAllAsteroidTypes();

    Optional<AsteroidTypeRef> findByCode(String code);

    Optional<AsteroidTypeRef> findByName(String name);

    @Query("SELECT a FROM AsteroidTypeRef a WHERE a.beltAffinity = :affinity ORDER BY a.relativeAbundance DESC")
    List<AsteroidTypeRef> findByBeltAffinity(@Param("affinity") String beltAffinity);

    @Query("SELECT a FROM AsteroidTypeRef a WHERE a.beltAffinity IN :affinities ORDER BY a.relativeAbundance DESC")
    List<AsteroidTypeRef> findByBeltAffinities(@Param("affinities") List<String> beltAffinities);
}

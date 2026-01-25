package com.brickroad.starcreator_webservice.repos;

import com.brickroad.starcreator_webservice.model.moons.MoonTypeRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MoonTypeRefRepository extends JpaRepository<MoonTypeRef, Long> {

    @Query("SELECT m FROM MoonTypeRef m")
    List<MoonTypeRef> findAllMoonTypes();

    @Query("SELECT m FROM MoonTypeRef m WHERE m.moonType = :moonType")
    Optional<MoonTypeRef> findByMoonType(String moonType);

    @Query("SELECT m FROM MoonTypeRef m ORDER BY m.massDistributionPriority ASC")
    List<MoonTypeRef> findAllOrderedByPriority();
}

package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ref.StarTypeRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StarTypeRefRepository extends JpaRepository<StarTypeRef, Integer> {

    @Query(value = "SELECT * FROM ref.star_type", nativeQuery = true)
    List<StarTypeRef> findAllStarTypes();

    Optional<StarTypeRef> findByName(String name);
}

package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ref.NameOriginRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NameOriginRefRepository extends JpaRepository<NameOriginRef, Long> {

    Optional<NameOriginRef> findByName(String name);

    @Query("SELECT o FROM NameOriginRef o ORDER BY o.name")
    List<NameOriginRef> findAllOrdered();
}

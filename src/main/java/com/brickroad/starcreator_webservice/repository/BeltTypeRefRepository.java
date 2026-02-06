package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ref.BeltTypeRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeltTypeRefRepository extends JpaRepository<BeltTypeRef, Integer> {

    @Query("SELECT b FROM BeltTypeRef b")
    List<BeltTypeRef> findAllBeltTypes();

    Optional<BeltTypeRef> findByCode(String code);

    Optional<BeltTypeRef> findByName(String name);
}

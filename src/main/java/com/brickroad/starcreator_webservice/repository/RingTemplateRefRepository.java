package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ref.RingTemplateRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RingTemplateRefRepository extends JpaRepository<RingTemplateRef, Integer> {
}

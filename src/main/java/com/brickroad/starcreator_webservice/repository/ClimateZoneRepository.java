package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.ClimateZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClimateZoneRepository extends JpaRepository<ClimateZone, Long> {

    List<ClimateZone> findByWeatherId(Long weatherId);

    void deleteByWeatherId(Long weatherId);
}

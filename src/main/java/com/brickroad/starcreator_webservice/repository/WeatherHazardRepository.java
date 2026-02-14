package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.WeatherHazard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherHazardRepository extends JpaRepository<WeatherHazard, Long> {

    List<WeatherHazard> findByWeatherId(Long weatherId);

    void deleteByWeatherId(Long weatherId);
}

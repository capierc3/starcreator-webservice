package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.ExtremeWeatherEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtremeWeatherEventRepository extends JpaRepository<ExtremeWeatherEvent, Long> {

    List<ExtremeWeatherEvent> findByWeatherId(Long weatherId);

    void deleteByWeatherId(Long weatherId);
}

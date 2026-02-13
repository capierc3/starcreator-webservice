package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.CloudLayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CloudLayerRepository extends JpaRepository<CloudLayer, Long> {

    List<CloudLayer> findByWeatherIdOrderByLayerOrder(Long weatherId);

    void deleteByWeatherId(Long weatherId);
}

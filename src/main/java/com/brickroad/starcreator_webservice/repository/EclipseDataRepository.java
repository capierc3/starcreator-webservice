package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.EclipseData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EclipseDataRepository extends JpaRepository<EclipseData, Long> {

    List<EclipseData> findByWeatherId(Long weatherId);

    void deleteByWeatherId(Long weatherId);
}

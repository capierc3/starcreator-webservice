package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.PrecipitationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrecipitationTypeRepository extends JpaRepository<PrecipitationType, Long> {

    List<PrecipitationType> findByWeatherId(Long weatherId);

    void deleteByWeatherId(Long weatherId);
}

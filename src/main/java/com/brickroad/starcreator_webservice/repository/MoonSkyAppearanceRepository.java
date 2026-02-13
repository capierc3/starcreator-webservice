package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.MoonSkyAppearance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoonSkyAppearanceRepository extends JpaRepository<MoonSkyAppearance, Long> {

    List<MoonSkyAppearance> findByWeatherId(Long weatherId);

    void deleteByWeatherId(Long weatherId);
}

package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.Atmosphere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtmosphereRepository extends JpaRepository<Atmosphere, Long> {
}
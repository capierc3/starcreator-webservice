package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.PlanetaryWeather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanetaryWeatherRepository extends JpaRepository<PlanetaryWeather, Long> {

    Optional<PlanetaryWeather> findByPlanetId(Long planetId);

    Optional<PlanetaryWeather> findByMoonId(Long moonId);

    boolean existsByPlanetId(Long planetId);

    void deleteByPlanetId(Long planetId);
}

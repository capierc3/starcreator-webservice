package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.Faction;
import com.brickroad.starcreator_webservice.entity.ud.FactionPresence;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactionPresenceRepository extends JpaRepository<FactionPresence, Long> {

    List<FactionPresence> findBySystem(StarSystem system);

    List<FactionPresence> findByFaction(Faction faction);

    Optional<FactionPresence> findByFactionAndSystem(Faction faction, StarSystem system);

    List<FactionPresence> findBySystemAndIsControllingTrue(StarSystem system);
}
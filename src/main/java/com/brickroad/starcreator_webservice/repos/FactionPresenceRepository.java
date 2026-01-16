package com.brickroad.starcreator_webservice.repos;

import com.brickroad.starcreator_webservice.model.factions.Faction;
import com.brickroad.starcreator_webservice.model.factions.FactionPresence;
import com.brickroad.starcreator_webservice.model.starSystems.StarSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactionPresenceRepository extends JpaRepository<FactionPresence, Long> {

    List<FactionPresence> findBySystem(StarSystem system);

    List<FactionPresence> findByFaction(Faction faction);

    Optional<FactionPresence> findByFactionAndSystem(Faction faction, StarSystem system);

    List<FactionPresence> findBySystemAndIsControllingTrue(StarSystem system);
}
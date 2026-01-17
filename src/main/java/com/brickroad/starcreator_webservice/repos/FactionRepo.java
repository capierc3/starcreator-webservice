package com.brickroad.starcreator_webservice.repos;

import com.brickroad.starcreator_webservice.model.factions.Faction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactionRepo extends JpaRepository<Faction, Long> {
    Faction findById(long id);

    Faction findByName(String name);

    @Query(value = "SELECT * FROM ud.factions ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Faction getRandomFaction();

    @Query(value = "SELECT * FROM ud.factions WHERE type = :type ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Faction> findAllByType(@Param("type") String Type, @Param("limit") int count);

    @Query(value = "SELECT * FROM ud.factions WHERE alignment = :alignment ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Faction> findAllByAlignment(@Param("alignment") String alignment, @Param("limit") int count);

    @Query(value = "SELECT * FROM ud.factions WHERE type = :type AND alignment = :alignment ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Faction> findByTypeAndAlignment(@Param("type") String type, @Param("alignment") String alignment,@Param("limit") int count);
}

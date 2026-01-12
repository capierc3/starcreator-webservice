package com.brickroad.starcreator_webservice.database.repos;

import com.brickroad.starcreator_webservice.model.factions.GovernmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GovernmentTypeRepo extends JpaRepository<GovernmentType, Long> {

    GovernmentType findById(int id);

}

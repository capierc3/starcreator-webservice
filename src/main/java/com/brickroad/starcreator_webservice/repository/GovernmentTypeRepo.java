package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ref.GovernmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GovernmentTypeRepo extends JpaRepository<GovernmentType, Long> {

    GovernmentType findById(int id);

}

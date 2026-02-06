package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ud.Belt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeltRepository extends JpaRepository<Belt, Long> {

    @Query("SELECT b FROM Belt b WHERE b.starSystem.id = :systemId")
    List<Belt> findByStarSystemId(@Param("systemId") Long starSystemId);

    @Query("SELECT b FROM Belt b WHERE b.starSystem.id = :systemId AND b.beltType.code = :typeCode")
    List<Belt> findByStarSystemIdAndTypeCode(@Param("systemId") Long starSystemId, @Param("typeCode") String typeCode);
}

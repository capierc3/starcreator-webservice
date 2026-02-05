package com.brickroad.starcreator_webservice.repository;

import com.brickroad.starcreator_webservice.entity.ref.NameRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NameRefRepository extends JpaRepository<NameRef, Long> {

    /**
     * Find all first names
     */
    @Query("SELECT n FROM NameRef n WHERE n.isFirst = true")
    List<NameRef> findAllFirstNames();

    /**
     * Find all last names
     */
    @Query("SELECT n FROM NameRef n WHERE n.isLast = true")
    List<NameRef> findAllLastNames();

    /**
     * Find first names by gender (includes unisex)
     */
    @Query("SELECT n FROM NameRef n WHERE n.isFirst = true AND (n.gender = :gender OR n.gender = 'unisex')")
    List<NameRef> findFirstNamesByGender(@Param("gender") String gender);

    /**
     * Find last names (all are unisex by convention)
     */
    @Query("SELECT n FROM NameRef n WHERE n.isLast = true")
    List<NameRef> findLastNames();

    /**
     * Find first names by origin (via mapping table)
     */
    @Query(value = """
        SELECT n.* FROM ref.name_ref n
        JOIN ref.name_origin_mapping nom ON n.id = nom.name_id
        JOIN ref.name_origin_ref o ON nom.origin_id = o.id
        WHERE n.is_first = true AND o.name = :originName
        """, nativeQuery = true)
    List<NameRef> findFirstNamesByOrigin(@Param("originName") String originName);

    /**
     * Find last names by origin (via mapping table)
     */
    @Query(value = """
        SELECT n.* FROM ref.name_ref n
        JOIN ref.name_origin_mapping nom ON n.id = nom.name_id
        JOIN ref.name_origin_ref o ON nom.origin_id = o.id
        WHERE n.is_last = true AND o.name = :originName
        """, nativeQuery = true)
    List<NameRef> findLastNamesByOrigin(@Param("originName") String originName);

    /**
     * Find first names by gender and origin
     */
    @Query(value = """
        SELECT n.* FROM ref.name_ref n
        JOIN ref.name_origin_mapping nom ON n.id = nom.name_id
        JOIN ref.name_origin_ref o ON nom.origin_id = o.id
        WHERE n.is_first = true 
        AND (n.gender = :gender OR n.gender = 'unisex')
        AND o.name = :originName
        """, nativeQuery = true)
    List<NameRef> findFirstNamesByGenderAndOrigin(@Param("gender") String gender, @Param("originName") String originName);

    /**
     * Find first names by region (via origin -> region mapping)
     */
    @Query(value = """
        SELECT DISTINCT n.* FROM ref.name_ref n
        JOIN ref.name_origin_mapping nom ON n.id = nom.name_id
        JOIN ref.origin_region_mapping orm ON nom.origin_id = orm.origin_id
        JOIN ref.name_region_ref r ON orm.region_id = r.id
        WHERE n.is_first = true AND r.name = :regionName
        """, nativeQuery = true)
    List<NameRef> findFirstNamesByRegion(@Param("regionName") String regionName);

    /**
     * Find last names by region
     */
    @Query(value = """
        SELECT DISTINCT n.* FROM ref.name_ref n
        JOIN ref.name_origin_mapping nom ON n.id = nom.name_id
        JOIN ref.origin_region_mapping orm ON nom.origin_id = orm.origin_id
        JOIN ref.name_region_ref r ON orm.region_id = r.id
        WHERE n.is_last = true AND r.name = :regionName
        """, nativeQuery = true)
    List<NameRef> findLastNamesByRegion(@Param("regionName") String regionName);
}

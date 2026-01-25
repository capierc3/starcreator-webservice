package com.brickroad.starcreator_webservice.entity.ref;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "star_type", schema = "ref")
public class StarTypeRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String spectralClass;
    private Double minMass;
    private Double maxMass;
    private Double massRadiusExponent;
    private Double radiusMultiplierMin;
    private Double radiusMultiplierMax;
    private Integer rarityWeight;
    private String description;

}

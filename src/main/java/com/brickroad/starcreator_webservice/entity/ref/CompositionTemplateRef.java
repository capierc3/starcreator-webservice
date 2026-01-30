package com.brickroad.starcreator_webservice.entity.ref;

import com.brickroad.starcreator_webservice.enums.CompositionClassification;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "composition_template", schema = "ref")
public class CompositionTemplateRef {

    // Getters and setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CompositionClassification classification;

    @Column(name = "planet_types", length = 500)
    private String planetTypes;

    @Column(name = "min_surface_temp_k")
    private Double minSurfaceTempK;

    @Column(name = "max_surface_temp_k")
    private Double maxSurfaceTempK;

    @Column(name = "rarity_weight")
    private Integer rarityWeight = 100;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CompositionTemplateComponentRef> components = new ArrayList<>();

    public boolean matches(String planetType, double surfaceTempK) {
        double tempToleranceK = 5.0;
        boolean typeMatch = planetTypes == null ||
                planetTypes.toLowerCase().contains(planetType.toLowerCase());

        boolean tempMatch = (minSurfaceTempK == null || surfaceTempK >= (minSurfaceTempK - tempToleranceK)) &&
                (maxSurfaceTempK == null || surfaceTempK <= (maxSurfaceTempK + tempToleranceK));

        return typeMatch && tempMatch;
    }

    public List<CompositionTemplateComponentRef> getComponentsForLayer(String layerType) {
        return components.stream()
                .filter(c -> layerType.equals(c.getLayerType()))
                .toList();
    }
}

package com.brickroad.starcreator_webservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "celestial_body", schema = "ud")
@Inheritance(strategy = InheritanceType.JOINED)
public class CelestialBody {

    @Id
    private Long id;

    private String name;
    private double mass;
    private double radius;
    private double circumference;

    @ManyToOne
    @JoinColumn(name = "system_id")
    private com.brickroad.starcreator_webservice.model.starSystems.StarSystem system;

    private Double distanceFromStar;

    private Integer orbitalOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getCircumference() {
        return circumference;
    }

    public void setCircumference(double circumference) {
        this.circumference = circumference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Double getDistanceFromStar() {
        return distanceFromStar;
    }

    public void setDistanceFromStar(Double distanceFromStar) {
        this.distanceFromStar = distanceFromStar;
    }

    public com.brickroad.starcreator_webservice.model.starSystems.StarSystem getSystem() {
        return system;
    }

    public void setSystem(com.brickroad.starcreator_webservice.model.starSystems.StarSystem system) {
        this.system = system;
    }

    public Integer getOrbitalOrder() {
        return orbitalOrder;
    }

    public void setOrbitalOrder(Integer orbitalOrder) {
        this.orbitalOrder = orbitalOrder;
    }
}

package com.brickroad.starcreator_webservice.entity.ref;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "name_ref", schema = "ref")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "is_first", nullable = false)
    private Boolean isFirst = false;

    @Column(name = "is_last", nullable = false)
    private Boolean isLast = false;

    @Column(name = "gender", length = 20, nullable = false)
    private String gender = "unisex";

    @Column(name = "popularity", nullable = false)
    private Integer popularity = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        modifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}

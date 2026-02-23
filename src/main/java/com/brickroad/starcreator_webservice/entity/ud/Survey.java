package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "survey", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A survey campaign organizing sectors and star systems under a common designation code")
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Human-readable name of the survey", example = "Star Creator Survey")
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    @Schema(description = "Three-letter designation code used in system naming", example = "SCS")
    private String code;

    @Schema(description = "Description of the survey's purpose", example = "Basic system mapping survey")
    private String description;

    @Column(name = "user_id")
    @Schema(description = "ID of the user who created the survey")
    private Long userId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @JsonIgnore
    private LocalDateTime modifiedAt;
}

package com.pokemon.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad Pokemon para la persistencia en base de datos
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@Entity
@Table(name = "pokemon", indexes = {
    @Index(name = "idx_pokemon_name", columnList = "name"),
    @Index(name = "idx_pokemon_external_id", columnList = "external_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    @NotNull(message = "El ID externo no puede ser nulo")
    @Positive(message = "El ID externo debe ser positivo")
    private Integer externalId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Column(name = "height")
    @Positive(message = "La altura debe ser positiva")
    private Integer height;

    @Column(name = "weight")
    @Positive(message = "El peso debe ser positivo")
    private Integer weight;

    @Column(name = "base_experience")
    @Positive(message = "La experiencia base debe ser positiva")
    private Integer baseExperience;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "pokemon_types", joinColumns = @JoinColumn(name = "pokemon_id"))
    @Column(name = "type_name")
    private List<String> types;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "pokemon_abilities", joinColumns = @JoinColumn(name = "pokemon_id"))
    @Column(name = "ability_name")
    private List<String> abilities;

    @Column(name = "sprite_url", length = 500)
    @Size(max = 500, message = "La URL del sprite no puede exceder 500 caracteres")
    private String spriteUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
} 
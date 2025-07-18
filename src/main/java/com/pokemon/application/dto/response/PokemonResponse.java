package com.pokemon.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para Pokémon
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con información del Pokémon")
public class PokemonResponse {

    @Schema(description = "ID interno del Pokémon", example = "1")
    private Long id;

    @Schema(description = "ID externo del Pokémon en la PokeAPI", example = "25")
    private Integer externalId;

    @Schema(description = "Nombre del Pokémon", example = "pikachu")
    private String name;

    @Schema(description = "Altura del Pokémon en decímetros", example = "4")
    private Integer height;

    @Schema(description = "Peso del Pokémon en hectogramos", example = "60")
    private Integer weight;

    @Schema(description = "Experiencia base del Pokémon", example = "112")
    private Integer baseExperience;

    @Schema(description = "Tipos del Pokémon", example = "[\"electric\"]")
    private List<String> types;

    @Schema(description = "Habilidades del Pokémon", example = "[\"static\", \"lightning-rod\"]")
    private List<String> abilities;

    @Schema(description = "URL del sprite frontal del Pokémon")
    private String spriteUrl;

    @Schema(description = "Fecha y hora de creación")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha y hora de última actualización")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
} 
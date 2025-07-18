package com.pokemon.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de creación de Pokémon
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para crear un nuevo Pokémon")
public class PokemonCreateRequest {

    @NotBlank(message = "El nombre del Pokémon es obligatorio")
    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "El nombre solo puede contener letras, números y guiones")
    @Schema(
        description = "Nombre del Pokémon a buscar en la PokeAPI",
        example = "pikachu",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;
} 
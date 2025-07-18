package com.pokemon.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemon.application.dto.request.PokemonCreateRequest;
import com.pokemon.application.dto.response.PokemonResponse;
import com.pokemon.application.service.PokemonService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para PokemonController
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@WebMvcTest(PokemonController.class)
@DisplayName("Pokemon Controller Integration Tests")
class PokemonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PokemonService pokemonService;

    @Test
    @DisplayName("POST /pokemon - Debe crear Pokemon exitosamente")
    void shouldCreatePokemonSuccessfully() throws Exception {
        // Given
        PokemonCreateRequest request = PokemonCreateRequest.builder()
                .name("pikachu")
                .build();

        PokemonResponse response = PokemonResponse.builder()
                .id(1L)
                .externalId(25)
                .name("pikachu")
                .height(4)
                .weight(60)
                .baseExperience(112)
                .types(List.of("electric"))
                .abilities(List.of("static"))
                .spriteUrl("sprite-url")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(pokemonService.createPokemon(any(PokemonCreateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/pokemon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pokémon creado exitosamente"))
                .andExpect(jsonPath("$.data.name").value("pikachu"))
                .andExpect(jsonPath("$.data.externalId").value(25))
                .andExpect(jsonPath("$.data.types[0]").value("electric"));
    }

    @Test
    @DisplayName("POST /pokemon - Debe retornar error de validación con nombre vacío")
    void shouldReturnValidationErrorWithEmptyName() throws Exception {
        // Given
        PokemonCreateRequest request = PokemonCreateRequest.builder()
                .name("")
                .build();

        // When & Then
        mockMvc.perform(post("/pokemon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    @DisplayName("GET /pokemon - Debe listar Pokemon con paginación")
    void shouldListPokemonWithPagination() throws Exception {
        // Given
        PokemonResponse response = PokemonResponse.builder()
                .id(1L)
                .externalId(25)
                .name("pikachu")
                .height(4)
                .weight(60)
                .baseExperience(112)
                .types(List.of("electric"))
                .abilities(List.of("static"))
                .spriteUrl("sprite-url")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Page<PokemonResponse> page = new PageImpl<>(List.of(response));
        when(pokemonService.getAllPokemon(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/pokemon")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lista de Pokémon obtenida exitosamente"))
                .andExpect(jsonPath("$.data.content[0].name").value("pikachu"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /pokemon/{id} - Debe obtener Pokemon por ID")
    void shouldGetPokemonById() throws Exception {
        // Given
        Long pokemonId = 1L;
        PokemonResponse response = PokemonResponse.builder()
                .id(pokemonId)
                .externalId(25)
                .name("pikachu")
                .height(4)
                .weight(60)
                .baseExperience(112)
                .types(List.of("electric"))
                .abilities(List.of("static"))
                .spriteUrl("sprite-url")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(pokemonService.getPokemonById(pokemonId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/pokemon/{id}", pokemonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(pokemonId))
                .andExpect(jsonPath("$.data.name").value("pikachu"));
    }

    @Test
    @DisplayName("GET /pokemon/{id} - Debe retornar 404 cuando Pokemon no existe")
    void shouldReturn404WhenPokemonNotFound() throws Exception {
        // Given
        Long pokemonId = 999L;
        when(pokemonService.getPokemonById(pokemonId))
                .thenThrow(new PokemonService.PokemonNotFoundException("Pokemon no encontrado"));

        // When & Then
        mockMvc.perform(get("/pokemon/{id}", pokemonId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("POKEMON_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /pokemon/name/{name} - Debe obtener Pokemon por nombre")
    void shouldGetPokemonByName() throws Exception {
        // Given
        String pokemonName = "pikachu";
        PokemonResponse response = PokemonResponse.builder()
                .id(1L)
                .externalId(25)
                .name(pokemonName)
                .height(4)
                .weight(60)
                .baseExperience(112)
                .types(List.of("electric"))
                .abilities(List.of("static"))
                .spriteUrl("sprite-url")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(pokemonService.getPokemonByName(pokemonName)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/pokemon/name/{name}", pokemonName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value(pokemonName));
    }

    @Test
    @DisplayName("GET /pokemon/search - Debe buscar Pokemon por nombre")
    void shouldSearchPokemonByName() throws Exception {
        // Given
        String query = "pika";
        PokemonResponse response = PokemonResponse.builder()
                .id(1L)
                .externalId(25)
                .name("pikachu")
                .height(4)
                .weight(60)
                .baseExperience(112)
                .types(List.of("electric"))
                .abilities(List.of("static"))
                .spriteUrl("sprite-url")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Page<PokemonResponse> page = new PageImpl<>(List.of(response));
        when(pokemonService.searchPokemonByName(eq(query), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/pokemon/search")
                .param("query", query)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("pikachu"));
    }

    @Test
    @DisplayName("DELETE /pokemon/{id} - Debe eliminar Pokemon exitosamente")
    void shouldDeletePokemonSuccessfully() throws Exception {
        // Given
        Long pokemonId = 1L;

        // When & Then
        mockMvc.perform(delete("/pokemon/{id}", pokemonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pokémon eliminado exitosamente"));
    }

    @Test
    @DisplayName("GET /pokemon/stats - Debe obtener estadísticas")
    void shouldGetStatistics() throws Exception {
        // Given
        PokemonService.PokemonStatsResponse stats = PokemonService.PokemonStatsResponse.builder()
                .totalPokemon(10L)
                .build();

        when(pokemonService.getStatistics()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/pokemon/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPokemon").value(10))
                .andExpect(jsonPath("$.data.total_pokemon").value(10));

    }
} 
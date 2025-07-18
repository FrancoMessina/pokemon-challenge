package com.pokemon.application.service;

import com.pokemon.application.dto.request.PokemonCreateRequest;
import com.pokemon.application.dto.response.PokemonResponse;
import com.pokemon.application.mapper.PokemonMapper;
import com.pokemon.domain.entity.Pokemon;
import com.pokemon.domain.repository.PokemonRepository;
import com.pokemon.infrastructure.external.dto.PokeApiResponse;
import com.pokemon.infrastructure.external.service.PokeApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PokemonService
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pokemon Service Tests")
class PokemonServiceTest {

    @Mock
    private PokemonRepository pokemonRepository;

    @Mock
    private PokeApiService pokeApiService;

    @Mock
    private PokemonMapper pokemonMapper;

    @InjectMocks
    private PokemonService pokemonService;

    private PokemonCreateRequest createRequest;
    private PokeApiResponse pokeApiResponse;
    private Pokemon pokemon;
    private PokemonResponse pokemonResponse;

    @BeforeEach
    void setUp() {
        createRequest = PokemonCreateRequest.builder()
                .name("pikachu")
                .build();

        pokeApiResponse = PokeApiResponse.builder()
                .id(25)
                .name("pikachu")
                .height(4)
                .weight(60)
                .baseExperience(112)
                .types(List.of(
                    new PokeApiResponse.TypeSlot(1, new PokeApiResponse.Type("electric", "url"))
                ))
                .abilities(List.of(
                    new PokeApiResponse.AbilitySlot(false, 1, new PokeApiResponse.Ability("static", "url"))
                ))
                .sprites(new PokeApiResponse.Sprites("sprite-url", null, null, null))
                .build();

        pokemon = Pokemon.builder()
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

        pokemonResponse = PokemonResponse.builder()
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
    }

    @Test
    @DisplayName("Debe crear un Pokemon exitosamente")
    void shouldCreatePokemonSuccessfully() {
        // Given
        when(pokemonRepository.existsByNameIgnoreCase("pikachu")).thenReturn(false);
        when(pokeApiService.getPokemonByName("pikachu")).thenReturn(pokeApiResponse);
        when(pokemonMapper.fromPokeApiResponse(pokeApiResponse)).thenReturn(pokemon);
        when(pokemonRepository.save(pokemon)).thenReturn(pokemon);
        when(pokemonMapper.toResponse(pokemon)).thenReturn(pokemonResponse);

        // When
        PokemonResponse result = pokemonService.createPokemon(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("pikachu");
        assertThat(result.getExternalId()).isEqualTo(25);

        verify(pokemonRepository).existsByNameIgnoreCase("pikachu");
        verify(pokeApiService).getPokemonByName("pikachu");
        verify(pokemonMapper).fromPokeApiResponse(pokeApiResponse);
        verify(pokemonRepository).save(pokemon);
        verify(pokemonMapper).toResponse(pokemon);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Pokemon ya existe")
    void shouldThrowExceptionWhenPokemonAlreadyExists() {
        // Given
        when(pokemonRepository.existsByNameIgnoreCase("pikachu")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> pokemonService.createPokemon(createRequest))
                .isInstanceOf(PokemonService.PokemonAlreadyExistsException.class)
                .hasMessageContaining("ya existe en la base de datos");

        verify(pokemonRepository).existsByNameIgnoreCase("pikachu");
        verify(pokeApiService, never()).getPokemonByName(anyString());
        verify(pokemonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el Pokemon no existe en PokeAPI")
    void shouldThrowExceptionWhenPokemonNotFoundInPokeAPI() {
        // Given
        when(pokemonRepository.existsByNameIgnoreCase("nonexistent")).thenReturn(false);
        when(pokeApiService.getPokemonByName("nonexistent"))
                .thenThrow(new PokeApiService.PokemonNotFoundException("Pokemon no encontrado"));

        PokemonCreateRequest request = PokemonCreateRequest.builder()
                .name("nonexistent")
                .build();

        // When & Then
        assertThatThrownBy(() -> pokemonService.createPokemon(request))
                .isInstanceOf(PokemonService.PokemonNotFoundException.class)
                .hasMessageContaining("no encontrado en PokeAPI");

        verify(pokemonRepository).existsByNameIgnoreCase("nonexistent");
        verify(pokeApiService).getPokemonByName("nonexistent");
        verify(pokemonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener todos los Pokemon con paginación")
    void shouldGetAllPokemonWithPagination() {
        // Given
        Pageable pageable = Pageable.ofSize(10);
        Page<Pokemon> pokemonPage = new PageImpl<>(List.of(pokemon), pageable, 1);
        List<PokemonResponse> responses = List.of(pokemonResponse);

        when(pokemonRepository.findAll(pageable)).thenReturn(pokemonPage);
        when(pokemonMapper.toResponseList(anyList())).thenReturn(responses);

        // When
        Page<PokemonResponse> result = pokemonService.getAllPokemon(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("pikachu");
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(pokemonRepository).findAll(pageable);
        verify(pokemonMapper).toResponseList(anyList());
    }

    @Test
    @DisplayName("Debe obtener Pokemon por ID")
    void shouldGetPokemonById() {
        // Given
        Long pokemonId = 1L;
        when(pokemonRepository.findById(pokemonId)).thenReturn(Optional.of(pokemon));
        when(pokemonMapper.toResponse(pokemon)).thenReturn(pokemonResponse);

        // When
        PokemonResponse result = pokemonService.getPokemonById(pokemonId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(pokemonId);
        assertThat(result.getName()).isEqualTo("pikachu");

        verify(pokemonRepository).findById(pokemonId);
        verify(pokemonMapper).toResponse(pokemon);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando Pokemon no existe por ID")
    void shouldThrowExceptionWhenPokemonNotFoundById() {
        // Given
        Long pokemonId = 999L;
        when(pokemonRepository.findById(pokemonId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> pokemonService.getPokemonById(pokemonId))
                .isInstanceOf(PokemonService.PokemonNotFoundException.class)
                .hasMessageContaining("no encontrado");

        verify(pokemonRepository).findById(pokemonId);
        verify(pokemonMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Debe obtener Pokemon por nombre")
    void shouldGetPokemonByName() {
        // Given
        String pokemonName = "pikachu";
        when(pokemonRepository.findByNameIgnoreCase(pokemonName)).thenReturn(Optional.of(pokemon));
        when(pokemonMapper.toResponse(pokemon)).thenReturn(pokemonResponse);

        // When
        PokemonResponse result = pokemonService.getPokemonByName(pokemonName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(pokemonName);

        verify(pokemonRepository).findByNameIgnoreCase(pokemonName);
        verify(pokemonMapper).toResponse(pokemon);
    }

    @Test
    @DisplayName("Debe eliminar Pokemon exitosamente")
    void shouldDeletePokemonSuccessfully() {
        // Given
        Long pokemonId = 1L;
        when(pokemonRepository.existsById(pokemonId)).thenReturn(true);

        // When
        pokemonService.deletePokemon(pokemonId);

        // Then
        verify(pokemonRepository).existsById(pokemonId);
        verify(pokemonRepository).deleteById(pokemonId);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar Pokemon inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentPokemon() {
        // Given
        Long pokemonId = 999L;
        when(pokemonRepository.existsById(pokemonId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> pokemonService.deletePokemon(pokemonId))
                .isInstanceOf(PokemonService.PokemonNotFoundException.class)
                .hasMessageContaining("no encontrado");

        verify(pokemonRepository).existsById(pokemonId);
        verify(pokemonRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Debe obtener estadísticas de Pokemon")
    void shouldGetPokemonStatistics() {
        // Given
        when(pokemonRepository.count()).thenReturn(10L);

        // When
        PokemonService.PokemonStatsResponse result = pokemonService.getStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalPokemon()).isEqualTo(10L);

        verify(pokemonRepository).count();
    }
} 
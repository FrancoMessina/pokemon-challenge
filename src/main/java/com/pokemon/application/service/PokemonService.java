package com.pokemon.application.service;

import com.pokemon.application.dto.request.PokemonCreateRequest;
import com.pokemon.application.dto.response.PokemonResponse;
import com.pokemon.application.mapper.PokemonMapper;
import com.pokemon.domain.entity.Pokemon;
import com.pokemon.domain.repository.PokemonRepository;
import com.pokemon.infrastructure.external.dto.PokeApiResponse;
import com.pokemon.infrastructure.external.service.PokeApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio principal para la gestión de Pokémon
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PokemonService {

    private final PokemonRepository pokemonRepository;
    private final PokeApiService pokeApiService;
    private final PokemonMapper pokemonMapper;

    /**
     * Crea un nuevo Pokémon obteniendo la información desde PokeAPI
     * 
     * @param request solicitud de creación
     * @return respuesta con la información del Pokémon creado
     * @throws PokemonAlreadyExistsException si el Pokémon ya existe
     * @throws PokemonNotFoundException si el Pokémon no existe en PokeAPI
     */
    @Transactional
    @CacheEvict(value = {"pokemonListCache", "pokemonSearchCache"}, allEntries = true)
    public PokemonResponse createPokemon(PokemonCreateRequest request) {
        String pokemonName = request.getName().toLowerCase().trim();
        
        log.info("Iniciando creación de Pokémon: {}", pokemonName);

        // Verificar si ya existe en nuestra base de datos
        if (pokemonRepository.existsByNameIgnoreCase(pokemonName)) {
            log.warn("Intento de crear Pokémon duplicado: {}", pokemonName);
            throw new PokemonAlreadyExistsException("El Pokémon '" + pokemonName + "' ya existe en la base de datos");
        }

        // Obtener información desde PokeAPI
        PokeApiResponse pokeApiResponse;
        try {
            pokeApiResponse = pokeApiService.getPokemonByName(pokemonName);
        } catch (PokeApiService.PokemonNotFoundException e) {
            log.error("Pokémon '{}' no encontrado en PokeAPI", pokemonName);
            throw new PokemonNotFoundException("Pokémon '" + pokemonName + "' no encontrado en PokeAPI", e);
        } catch (PokeApiService.ExternalApiException e) {
            log.error("Error al consultar PokeAPI para '{}'", pokemonName, e);
            throw new ExternalServiceException("Error al consultar información del Pokémon desde PokeAPI", e);
        }

        // Convertir y guardar
        Pokemon pokemon = pokemonMapper.fromPokeApiResponse(pokeApiResponse);
        Pokemon savedPokemon = pokemonRepository.save(pokemon);
        
        log.info("Pokémon '{}' creado exitosamente con ID: {}", pokemonName, savedPokemon.getId());
        
        return pokemonMapper.toResponse(savedPokemon);
    }

    /**
     * Obtiene todos los Pokémon con paginación
     * Los resultados se cachean por 10 minutos
     * 
     * @param pageable configuración de paginación
     * @return página de Pokémon
     */
    @Cacheable(value = "pokemonListCache", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    public Page<PokemonResponse> getAllPokemon(Pageable pageable) {
        log.debug("Obteniendo lista de Pokémon - Página: {}, Tamaño: {}", 
                 pageable.getPageNumber(), pageable.getPageSize());

        Page<Pokemon> pokemonPage = pokemonRepository.findAll(pageable);
        List<PokemonResponse> responses = pokemonMapper.toResponseList(pokemonPage.getContent());
        
        log.info("Se encontraron {} Pokémon en la página {} de {}", 
                responses.size(), pokemonPage.getNumber() + 1, pokemonPage.getTotalPages());

        return new PageImpl<>(responses, pageable, pokemonPage.getTotalElements());
    }

    /**
     * Busca un Pokémon por su ID
     * 
     * @param id ID del Pokémon
     * @return información del Pokémon
     * @throws PokemonNotFoundException si no se encuentra
     */
    @Cacheable(value = "pokemonCache", key = "'id_' + #id")
    public PokemonResponse getPokemonById(Long id) {
        log.debug("Buscando Pokémon por ID: {}", id);

        Pokemon pokemon = pokemonRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pokémon con ID {} no encontrado", id);
                    return new PokemonNotFoundException("Pokémon con ID " + id + " no encontrado");
                });

        log.debug("Pokémon encontrado: {}", pokemon.getName());
        return pokemonMapper.toResponse(pokemon);
    }

    /**
     * Busca un Pokémon por su nombre
     * 
     * @param name nombre del Pokémon
     * @return información del Pokémon
     * @throws PokemonNotFoundException si no se encuentra
     */
    @Cacheable(value = "pokemonCache", key = "'name_' + #name.toLowerCase()")
    public PokemonResponse getPokemonByName(String name) {
        log.debug("Buscando Pokémon por nombre: {}", name);

        Pokemon pokemon = pokemonRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> {
                    log.warn("Pokémon con nombre '{}' no encontrado", name);
                    return new PokemonNotFoundException("Pokémon '" + name + "' no encontrado");
                });

        log.debug("Pokémon encontrado: {}", pokemon.getName());
        return pokemonMapper.toResponse(pokemon);
    }

    /**
     * Busca Pokémon por tipo
     * 
     * @param type tipo de Pokémon
     * @param pageable configuración de paginación
     * @return página de Pokémon del tipo especificado
     */
    @Cacheable(value = "pokemonSearchCache", key = "'type_' + #type.toLowerCase() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PokemonResponse> getPokemonByType(String type, Pageable pageable) {
        log.debug("Buscando Pokémon por tipo: {}", type);

        Page<Pokemon> pokemonPage = pokemonRepository.findByTypesContainingIgnoreCase(type, pageable);
        List<PokemonResponse> responses = pokemonMapper.toResponseList(pokemonPage.getContent());

        log.info("Se encontraron {} Pokémon de tipo '{}' en la página {} de {}", 
                responses.size(), type, pokemonPage.getNumber() + 1, pokemonPage.getTotalPages());

        return new PageImpl<>(responses, pageable, pokemonPage.getTotalElements());
    }

    /**
     * Busca Pokémon por nombre que contenga el texto dado
     * 
     * @param name parte del nombre a buscar
     * @param pageable configuración de paginación
     * @return página de Pokémon que contengan el texto en su nombre
     */
    @Cacheable(value = "pokemonSearchCache", key = "'search_' + #name.toLowerCase() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PokemonResponse> searchPokemonByName(String name, Pageable pageable) {
        log.debug("Buscando Pokémon que contengan: {}", name);

        Page<Pokemon> pokemonPage = pokemonRepository.findByNameContainingIgnoreCase(name, pageable);
        List<PokemonResponse> responses = pokemonMapper.toResponseList(pokemonPage.getContent());

        log.info("Se encontraron {} Pokémon que contienen '{}' en la página {} de {}", 
                responses.size(), name, pokemonPage.getNumber() + 1, pokemonPage.getTotalPages());

        return new PageImpl<>(responses, pageable, pokemonPage.getTotalElements());
    }

    /**
     * Elimina un Pokémon por su ID
     * 
     * @param id ID del Pokémon a eliminar
     * @throws PokemonNotFoundException si no se encuentra
     */
    @Transactional
    @CacheEvict(value = {"pokemonCache", "pokemonListCache", "pokemonSearchCache"}, allEntries = true)
    public void deletePokemon(Long id) {
        log.info("Eliminando Pokémon con ID: {}", id);

        if (!pokemonRepository.existsById(id)) {
            log.warn("Intento de eliminar Pokémon inexistente con ID: {}", id);
            throw new PokemonNotFoundException("Pokémon con ID " + id + " no encontrado");
        }

        pokemonRepository.deleteById(id);
        log.info("Pokémon con ID {} eliminado exitosamente", id);
    }

    /**
     * Obtiene estadísticas básicas de Pokémon
     * 
     * @return información estadística
     */
    @Cacheable(value = "pokemonStatsCache", key = "'stats'")
    public PokemonStatsResponse getStatistics() {
        long totalPokemon = pokemonRepository.count();
        
        // Aquí podrías agregar más estadísticas como tipos más comunes, etc.
        return PokemonStatsResponse.builder()
                .totalPokemon(totalPokemon)
                .build();
    }

    // Excepciones personalizadas
    public static class PokemonNotFoundException extends RuntimeException {
        public PokemonNotFoundException(String message) {
            super(message);
        }
        
        public PokemonNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class PokemonAlreadyExistsException extends RuntimeException {
        public PokemonAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class ExternalServiceException extends RuntimeException {
        public ExternalServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // DTO para estadísticas
    public static class PokemonStatsResponse {
        private final Long totalPokemon;

        private PokemonStatsResponse(Builder builder) {
            this.totalPokemon = builder.totalPokemon;
        }

        public Long getTotalPokemon() {
            return totalPokemon;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long totalPokemon;

            public Builder totalPokemon(Long totalPokemon) {
                this.totalPokemon = totalPokemon;
                return this;
            }

            public PokemonStatsResponse build() {
                return new PokemonStatsResponse(this);
            }
        }
    }
} 
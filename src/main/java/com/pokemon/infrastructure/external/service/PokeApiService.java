package com.pokemon.infrastructure.external.service;

import com.pokemon.infrastructure.external.dto.PokeApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Servicio para interactuar con la PokeAPI externa
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PokeApiService {

    private final WebClient webClient;

    @Value("${pokemon.api.base-url}")
    private String baseUrl;

    @Value("${pokemon.api.timeout}")
    private Duration timeout;

    /**
     * Obtiene información de un Pokémon desde la PokeAPI
     * Los resultados se cachean por 5 minutos para reducir llamadas externas
     * 
     * @param name nombre del Pokémon
     * @return información del Pokémon desde la API externa
     * @throws PokemonNotFoundException si el Pokémon no existe
     * @throws ExternalApiException si hay error en la comunicación
     */
    @Cacheable(value = "pokemonCache", key = "#name.toLowerCase()")
    public PokeApiResponse getPokemonByName(String name) {
        log.info("Buscando Pokémon '{}' en PokeAPI", name);
        
        try {
            return webClient
                .get()
                .uri(baseUrl + "/pokemon/{name}", name.toLowerCase())
                .retrieve()
                .bodyToMono(PokeApiResponse.class)
                .timeout(timeout)
                .doOnSuccess(response -> log.info("Pokémon '{}' encontrado exitosamente", name))
                .doOnError(error -> log.error("Error al buscar Pokémon '{}': {}", name, error.getMessage()))
                .onErrorMap(WebClientResponseException.NotFound.class, 
                    ex -> new PokemonNotFoundException("Pokémon '" + name + "' no encontrado en PokeAPI"))
                .onErrorMap(WebClientResponseException.class,
                    ex -> new ExternalApiException("Error al comunicarse con PokeAPI: " + ex.getMessage()))
                .onErrorMap(Exception.class,
                    ex -> new ExternalApiException("Error inesperado al consultar PokeAPI: " + ex.getMessage()))
                .block();
                
        } catch (Exception e) {
            log.error("Error al obtener Pokémon '{}' desde PokeAPI", name, e);
            throw e;
        }
    }

    /**
     * Verifica si un Pokémon existe en la PokeAPI
     * 
     * @param name nombre del Pokémon
     * @return true si existe, false en caso contrario
     */
    @Cacheable(value = "pokemonExistsCache", key = "#name.toLowerCase()")
    public boolean pokemonExists(String name) {
        log.debug("Verificando si Pokémon '{}' existe en PokeAPI", name);
        
        try {
            return webClient
                .head()
                .uri(baseUrl + "/pokemon/{name}", name.toLowerCase())
                .retrieve()
                .toBodilessEntity()
                .timeout(timeout)
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorReturn(WebClientResponseException.NotFound.class, false)
                .onErrorReturn(false)
                .block();
                
        } catch (Exception e) {
            log.warn("Error al verificar existencia de Pokémon '{}': {}", name, e.getMessage());
            return false;
        }
    }

    /**
     * Excepción personalizada para Pokémon no encontrado
     */
    public static class PokemonNotFoundException extends RuntimeException {
        public PokemonNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Excepción personalizada para errores de API externa
     */
    public static class ExternalApiException extends RuntimeException {
        public ExternalApiException(String message) {
            super(message);
        }
        
        public ExternalApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 
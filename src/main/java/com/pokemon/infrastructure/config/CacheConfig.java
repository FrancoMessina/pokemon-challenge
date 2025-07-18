package com.pokemon.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuración de cache con Caffeine
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    /**
     * Configuración del cache manager con diferentes configuraciones por cache
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Cache para datos de Pokemon desde PokeAPI (larga duración)
        cacheManager.registerCustomCache("pokemonCache", 
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(Duration.ofMinutes(30))
                        .recordStats()
                        .build());

        // Cache para verificación de existencia en PokeAPI (duración media)
        cacheManager.registerCustomCache("pokemonExistsCache", 
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(Duration.ofMinutes(15))
                        .recordStats()
                        .build());

        // Cache para listas de Pokemon (corta duración)
        cacheManager.registerCustomCache("pokemonListCache", 
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .recordStats()
                        .build());

        // Cache para búsquedas de Pokemon (corta duración)
        cacheManager.registerCustomCache("pokemonSearchCache", 
                Caffeine.newBuilder()
                        .maximumSize(200)
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .recordStats()
                        .build());

        // Cache para estadísticas (muy corta duración)
        cacheManager.registerCustomCache("pokemonStatsCache", 
                Caffeine.newBuilder()
                        .maximumSize(10)
                        .expireAfterWrite(Duration.ofMinutes(2))
                        .recordStats()
                        .build());

        log.info("Cache manager configurado con Caffeine");
        return cacheManager;
    }
} 
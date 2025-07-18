package com.pokemon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Clase principal de la aplicación Pokémon API
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class PokemonApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokemonApiApplication.class, args);
    }
} 
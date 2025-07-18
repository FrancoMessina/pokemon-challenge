package com.pokemon.domain.repository;

import com.pokemon.domain.entity.Pokemon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Pokemon
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Long> {

    /**
     * Busca un Pokémon por su nombre (case-insensitive)
     * 
     * @param name nombre del Pokémon
     * @return Optional con el Pokémon encontrado
     */
    Optional<Pokemon> findByNameIgnoreCase(String name);

    /**
     * Busca un Pokémon por su ID externo
     * 
     * @param externalId ID del Pokémon en la PokeAPI
     * @return Optional con el Pokémon encontrado
     */
    Optional<Pokemon> findByExternalId(Integer externalId);

    /**
     * Verifica si existe un Pokémon con el nombre dado
     * 
     * @param name nombre del Pokémon
     * @return true si existe, false en caso contrario
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Verifica si existe un Pokémon con el ID externo dado
     * 
     * @param externalId ID del Pokémon en la PokeAPI
     * @return true si existe, false en caso contrario
     */
    boolean existsByExternalId(Integer externalId);

    /**
     * Busca Pokémon por tipo
     * 
     * @param type tipo de Pokémon
     * @param pageable configuración de paginación
     * @return página de Pokémon del tipo especificado
     */
    @Query("SELECT p FROM Pokemon p JOIN p.types t WHERE LOWER(t) = LOWER(:type)")
    Page<Pokemon> findByTypesContainingIgnoreCase(@Param("type") String type, Pageable pageable);

    /**
     * Busca Pokémon por habilidad
     * 
     * @param ability habilidad del Pokémon
     * @param pageable configuración de paginación
     * @return página de Pokémon con la habilidad especificada
     */
    @Query("SELECT p FROM Pokemon p JOIN p.abilities a WHERE LOWER(a) = LOWER(:ability)")
    Page<Pokemon> findByAbilitiesContainingIgnoreCase(@Param("ability") String ability, Pageable pageable);

    /**
     * Busca Pokémon por nombre que contenga el texto dado
     * 
     * @param name parte del nombre a buscar
     * @param pageable configuración de paginación
     * @return página de Pokémon que contengan el texto en su nombre
     */
    Page<Pokemon> findByNameContainingIgnoreCase(String name, Pageable pageable);
} 
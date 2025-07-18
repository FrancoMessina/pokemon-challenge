package com.pokemon.application.mapper;

import com.pokemon.application.dto.response.PokemonResponse;
import com.pokemon.domain.entity.Pokemon;
import com.pokemon.infrastructure.external.dto.PokeApiResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades y DTOs de Pokémon
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface PokemonMapper {

    /**
     * Convierte una entidad Pokemon a DTO de respuesta
     * 
     * @param pokemon entidad Pokemon
     * @return DTO de respuesta
     */
    PokemonResponse toResponse(Pokemon pokemon);

    /**
     * Convierte una lista de entidades Pokemon a lista de DTOs de respuesta
     * 
     * @param pokemon lista de entidades Pokemon
     * @return lista de DTOs de respuesta
     */
    List<PokemonResponse> toResponseList(List<Pokemon> pokemon);

    /**
     * Convierte un DTO de PokeAPI a entidad Pokemon
     * 
     * @param pokeApiResponse respuesta de la PokeAPI
     * @return entidad Pokemon
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", source = "id")
    @Mapping(target = "types", source = "types", qualifiedByName = "mapTypes")
    @Mapping(target = "abilities", source = "abilities", qualifiedByName = "mapAbilities")
    @Mapping(target = "spriteUrl", source = "sprites.frontDefault")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Pokemon fromPokeApiResponse(PokeApiResponse pokeApiResponse);

    /**
     * Mapea los tipos de Pokémon desde la respuesta de PokeAPI
     * 
     * @param types lista de slots de tipos
     * @return lista de nombres de tipos
     */
    @Named("mapTypes")
    default List<String> mapTypes(List<PokeApiResponse.TypeSlot> types) {
        if (types == null) {
            return List.of();
        }
        
        return types.stream()
                .filter(typeSlot -> typeSlot.getType() != null)
                .map(typeSlot -> typeSlot.getType().getName())
                .collect(Collectors.toList());
    }

    /**
     * Mapea las habilidades de Pokémon desde la respuesta de PokeAPI
     * 
     * @param abilities lista de slots de habilidades
     * @return lista de nombres de habilidades
     */
    @Named("mapAbilities")
    default List<String> mapAbilities(List<PokeApiResponse.AbilitySlot> abilities) {
        if (abilities == null) {
            return List.of();
        }
        
        return abilities.stream()
                .filter(abilitySlot -> abilitySlot.getAbility() != null)
                .map(abilitySlot -> abilitySlot.getAbility().getName())
                .collect(Collectors.toList());
    }
} 
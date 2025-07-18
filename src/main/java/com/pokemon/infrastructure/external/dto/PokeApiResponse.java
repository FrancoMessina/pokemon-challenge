package com.pokemon.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para mapear la respuesta de la PokeAPI externa
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PokeApiResponse {

    private Integer id;
    private String name;
    private Integer height;
    private Integer weight;
    
    @JsonProperty("base_experience")
    private Integer baseExperience;
    
    private List<TypeSlot> types;
    private List<AbilitySlot> abilities;
    private Sprites sprites;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TypeSlot {
        private Integer slot;
        private Type type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Type {
        private String name;
        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AbilitySlot {
        private Boolean isHidden;
        private Integer slot;
        private Ability ability;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Ability {
        private String name;
        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sprites {
        @JsonProperty("front_default")
        private String frontDefault;
        
        @JsonProperty("front_shiny")
        private String frontShiny;
        
        @JsonProperty("back_default")
        private String backDefault;
        
        @JsonProperty("back_shiny")
        private String backShiny;
    }
} 
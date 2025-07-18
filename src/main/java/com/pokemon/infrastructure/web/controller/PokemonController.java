package com.pokemon.infrastructure.web.controller;

import com.pokemon.application.dto.request.PokemonCreateRequest;
import com.pokemon.application.dto.response.PokemonResponse;
import com.pokemon.application.service.PokemonService;
import com.pokemon.infrastructure.web.dto.ApiResponse;
import com.pokemon.infrastructure.web.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de Pokémon
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/pokemon")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Pokemon API", description = "Endpoints para gestión de Pokémon")
public class PokemonController {

    private final PokemonService pokemonService;

    /**
     * Crea un nuevo Pokémon obteniendo información desde PokeAPI
     */
    @PostMapping
    @Operation(
        summary = "Crear nuevo Pokémon",
        description = "Crea un nuevo Pokémon obteniendo la información desde la PokeAPI externa"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Pokémon creado exitosamente",
            content = @Content(schema = @Schema(implementation = PokemonResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pokémon no encontrado en PokeAPI",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Pokémon ya existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "502",
            description = "Error en servicio externo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<PokemonResponse>> createPokemon(
            @Valid @RequestBody PokemonCreateRequest request) {
        
        log.info("POST /pokemon - Creando Pokémon: {}", request.getName());
        
        try {
            PokemonResponse response = pokemonService.createPokemon(request);
            
            ApiResponse<PokemonResponse> apiResponse = ApiResponse.<PokemonResponse>builder()
                    .success(true)
                    .message("Pokémon creado exitosamente")
                    .data(response)
                    .build();
            
            log.info("Pokémon '{}' creado exitosamente con ID: {}", 
                    response.getName(), response.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
            
        } catch (Exception e) {
            log.error("Error al crear Pokémon '{}'", request.getName(), e);
            throw e; // El @ControllerAdvice manejará la excepción
        }
    }

    /**
     * Obtiene todos los Pokémon con paginación y ordenamiento
     */
    @GetMapping
    @Operation(
        summary = "Listar Pokémon",
        description = "Obtiene una lista paginada de todos los Pokémon guardados en la base de datos"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de Pokémon obtenida exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Parámetros de paginación inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Page<PokemonResponse>>> getAllPokemon(
            @Parameter(description = "Número de página (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) int size,
            
            @Parameter(description = "Campo de ordenamiento", example = "name")
            @RequestParam(defaultValue = "id") String sortBy,
            
            @Parameter(description = "Dirección de ordenamiento", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("GET /pokemon - Página: {}, Tamaño: {}, Ordenar por: {} {}", 
                page, size, sortBy, sortDir);

        // Validar dirección de ordenamiento
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PokemonResponse> pokemonPage = pokemonService.getAllPokemon(pageable);

        ApiResponse<Page<PokemonResponse>> apiResponse = ApiResponse.<Page<PokemonResponse>>builder()
                .success(true)
                .message("Lista de Pokémon obtenida exitosamente")
                .data(pokemonPage)
                .build();

        log.info("Devolviendo {} Pokémon de {} total", 
                pokemonPage.getNumberOfElements(), pokemonPage.getTotalElements());

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Busca un Pokémon por su ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener Pokémon por ID",
        description = "Busca un Pokémon específico por su ID interno"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pokémon encontrado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pokémon no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<PokemonResponse>> getPokemonById(
            @Parameter(description = "ID del Pokémon", example = "1")
            @PathVariable @Positive Long id) {
        
        log.info("GET /pokemon/{} - Buscando Pokémon por ID", id);

        PokemonResponse pokemon = pokemonService.getPokemonById(id);
        
        ApiResponse<PokemonResponse> apiResponse = ApiResponse.<PokemonResponse>builder()
                .success(true)
                .message("Pokémon encontrado exitosamente")
                .data(pokemon)
                .build();

        log.info("Pokémon encontrado: {}", pokemon.getName());
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Busca un Pokémon por su nombre
     */
    @GetMapping("/name/{name}")
    @Operation(
        summary = "Obtener Pokémon por nombre",
        description = "Busca un Pokémon específico por su nombre"
    )
    public ResponseEntity<ApiResponse<PokemonResponse>> getPokemonByName(
            @Parameter(description = "Nombre del Pokémon", example = "pikachu")
            @PathVariable String name) {
        
        log.info("GET /pokemon/name/{} - Buscando Pokémon por nombre", name);

        PokemonResponse pokemon = pokemonService.getPokemonByName(name);
        
        ApiResponse<PokemonResponse> apiResponse = ApiResponse.<PokemonResponse>builder()
                .success(true)
                .message("Pokémon encontrado exitosamente")
                .data(pokemon)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Busca Pokémon por tipo
     */
    @GetMapping("/type/{type}")
    @Operation(
        summary = "Buscar Pokémon por tipo",
        description = "Obtiene todos los Pokémon de un tipo específico"
    )
    public ResponseEntity<ApiResponse<Page<PokemonResponse>>> getPokemonByType(
            @Parameter(description = "Tipo de Pokémon", example = "electric")
            @PathVariable String type,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        log.info("GET /pokemon/type/{} - Buscando Pokémon por tipo", type);

        Pageable pageable = PageRequest.of(page, size);
        Page<PokemonResponse> pokemonPage = pokemonService.getPokemonByType(type, pageable);

        ApiResponse<Page<PokemonResponse>> apiResponse = ApiResponse.<Page<PokemonResponse>>builder()
                .success(true)
                .message("Pokémon encontrados por tipo exitosamente")
                .data(pokemonPage)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Busca Pokémon por nombre (búsqueda parcial)
     */
    @GetMapping("/search")
    @Operation(
        summary = "Buscar Pokémon por nombre",
        description = "Busca Pokémon que contengan el texto especificado en su nombre"
    )
    public ResponseEntity<ApiResponse<Page<PokemonResponse>>> searchPokemon(
            @Parameter(description = "Texto a buscar en el nombre", example = "chu")
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        log.info("GET /pokemon/search?query={} - Buscando Pokémon", query);

        Pageable pageable = PageRequest.of(page, size);
        Page<PokemonResponse> pokemonPage = pokemonService.searchPokemonByName(query, pageable);

        ApiResponse<Page<PokemonResponse>> apiResponse = ApiResponse.<Page<PokemonResponse>>builder()
                .success(true)
                .message("Búsqueda completada exitosamente")
                .data(pokemonPage)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Elimina un Pokémon por su ID
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar Pokémon",
        description = "Elimina un Pokémon de la base de datos"
    )
    public ResponseEntity<ApiResponse<Void>> deletePokemon(
            @Parameter(description = "ID del Pokémon a eliminar", example = "1")
            @PathVariable @Positive Long id) {
        
        log.info("DELETE /pokemon/{} - Eliminando Pokémon", id);

        pokemonService.deletePokemon(id);
        
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .message("Pokémon eliminado exitosamente")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Obtiene estadísticas de Pokémon
     */
    @GetMapping("/stats")
    @Operation(
        summary = "Obtener estadísticas",
        description = "Obtiene estadísticas generales de los Pokémon en la base de datos"
    )
    public ResponseEntity<ApiResponse<PokemonService.PokemonStatsResponse>> getStatistics() {
        log.info("GET /pokemon/stats - Obteniendo estadísticas");

        PokemonService.PokemonStatsResponse stats = pokemonService.getStatistics();
        
        ApiResponse<PokemonService.PokemonStatsResponse> apiResponse = 
                ApiResponse.<PokemonService.PokemonStatsResponse>builder()
                .success(true)
                .message("Estadísticas obtenidas exitosamente")
                .data(stats)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
} 
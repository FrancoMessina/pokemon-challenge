package com.pokemon.infrastructure.web.exception;

import com.pokemon.application.service.PokemonService;
import com.pokemon.infrastructure.external.service.PokeApiService;
import com.pokemon.infrastructure.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para la API
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación de @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        log.warn("Error de validación en {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                fieldErrors.put("general", error.getDefaultMessage());
            }
        });

        ErrorResponse errorResponse = ErrorResponse.validationError(
                "Error de validación en los datos de entrada", fieldErrors);
        errorResponse.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja errores de validación de @Validated en parámetros
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        log.warn("Error de validación de parámetros en {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    ConstraintViolation::getMessage,
                    (existing, replacement) -> existing
                ));

        ErrorResponse errorResponse = ErrorResponse.validationError(
                "Error de validación en los parámetros", fieldErrors);
        errorResponse.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja errores de tipo de parámetro incorrecto
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        log.warn("Error de tipo de parámetro en {}: {}", request.getRequestURI(), ex.getMessage());

        String message = String.format("El parámetro '%s' debe ser de tipo %s", 
                ex.getName(), ex.getRequiredType().getSimpleName());

        ErrorResponse errorResponse = ErrorResponse.of(message, "TYPE_MISMATCH");
        errorResponse.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja Pokémon no encontrado
     */
    @ExceptionHandler(PokemonService.PokemonNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePokemonNotFound(
            PokemonService.PokemonNotFoundException ex, HttpServletRequest request) {
        
        log.info("Pokémon no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ex.getMessage(), "POKEMON_NOT_FOUND");
        errorResponse.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja Pokémon ya existente
     */
    @ExceptionHandler(PokemonService.PokemonAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePokemonAlreadyExists(
            PokemonService.PokemonAlreadyExistsException ex, HttpServletRequest request) {
        
        log.warn("Intento de crear Pokémon duplicado en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ex.getMessage(), "POKEMON_ALREADY_EXISTS");
        errorResponse.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Maneja errores de PokeAPI (Pokémon no encontrado en servicio externo)
     */
    @ExceptionHandler(PokeApiService.PokemonNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExternalPokemonNotFound(
            PokeApiService.PokemonNotFoundException ex, HttpServletRequest request) {
        
        log.warn("Pokémon no encontrado en PokeAPI en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ex.getMessage(), "EXTERNAL_POKEMON_NOT_FOUND");
        errorResponse.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja errores de servicio externo
     */
    @ExceptionHandler({
        PokeApiService.ExternalApiException.class,
        PokemonService.ExternalServiceException.class
    })
    public ResponseEntity<ErrorResponse> handleExternalServiceError(
            Exception ex, HttpServletRequest request) {
        
        log.error("Error en servicio externo en {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                "Error temporal en servicio externo. Intente nuevamente más tarde.", 
                "EXTERNAL_SERVICE_ERROR",
                ex.getMessage()
        );
        errorResponse.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    /**
     * Maneja errores de integridad de datos
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        log.error("Error de integridad de datos en {}: {}", request.getRequestURI(), ex.getMessage());

        String message = "Error de integridad de datos. Verifique que los datos no estén duplicados.";
        if (ex.getMessage() != null && ex.getMessage().contains("duplicate key")) {
            message = "Ya existe un registro con estos datos.";
        }

        ErrorResponse errorResponse = ErrorResponse.of(message, "DATA_INTEGRITY_ERROR");
        errorResponse.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Maneja errores inesperados
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(
            Exception ex, HttpServletRequest request) {
        
        log.error("Error inesperado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                "Error interno del servidor. Contacte al administrador si el problema persiste.",
                "INTERNAL_SERVER_ERROR",
                ex.getClass().getSimpleName()
        );
        errorResponse.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
} 
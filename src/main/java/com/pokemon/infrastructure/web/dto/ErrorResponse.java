package com.pokemon.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO para respuestas de error
 * 
 * @author Pokemon API Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de error de la API")
public class ErrorResponse {

    @Schema(description = "Indica que ocurrió un error", example = "false")
    @Builder.Default
    private boolean success = false;

    @Schema(description = "Mensaje principal del error", example = "Error de validación")
    private String message;

    @Schema(description = "Código de error específico", example = "VALIDATION_ERROR")
    private String errorCode;

    @Schema(description = "Detalles adicionales del error")
    private String details;

    @Schema(description = "Errores de validación por campo")
    private Map<String, String> fieldErrors;

    @Schema(description = "Lista de errores adicionales")
    private List<String> errors;

    @Schema(description = "Timestamp del error")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Path de la request que causó el error", example = "/api/v1/pokemon")
    private String path;

    /**
     * Crea una respuesta de error simple
     */
    public static ErrorResponse of(String message) {
        return ErrorResponse.builder()
                .message(message)
                .build();
    }

    /**
     * Crea una respuesta de error con código
     */
    public static ErrorResponse of(String message, String errorCode) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .build();
    }

    /**
     * Crea una respuesta de error con detalles
     */
    public static ErrorResponse of(String message, String errorCode, String details) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .details(details)
                .build();
    }

    /**
     * Crea una respuesta de error de validación
     */
    public static ErrorResponse validationError(String message, Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode("VALIDATION_ERROR")
                .fieldErrors(fieldErrors)
                .build();
    }
} 
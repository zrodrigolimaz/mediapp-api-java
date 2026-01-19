package com.mediapp.api.dto.auth;

import com.mediapp.api.entity.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "DTO de workspace")
public record WorkspaceDto(
    @Schema(description = "ID do workspace", example = "123e4567-e89b-12d3-a456-426614174001")
    UUID id,
    
    @Schema(description = "Nome do workspace", example = "Clínica Dr. João")
    String name,
    
    @Schema(description = "Tipo de documento", example = "CPF")
    DocumentType documentType,
    
    @Schema(description = "Número do documento", example = "REG1737123456789-12")
    String documentNumber,
    
    @Schema(description = "ID do proprietário do workspace", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID ownerId,
    
    @Schema(description = "Data de criação")
    Instant createdAt,
    
    @Schema(description = "Data de atualização")
    Instant updatedAt
) {}


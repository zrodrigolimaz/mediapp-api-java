package com.mediapp.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mediapp.api.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta do registro, contendo dados do usuário, workspace e token JWT.
 */
@Schema(description = "DTO de resposta do registro de usuário")
public record AuthResponseDTO(
    @Schema(description = "ID do usuário", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,
    
    @Schema(description = "Nome completo do usuário", example = "Dr. João Silva")
    String fullName,
    
    @Schema(description = "Email do usuário", example = "joao@example.com")
    String email,
    
    @Schema(description = "Papel do usuário", example = "ADMIN")
    UserRole role,
    
    @Schema(description = "Data de criação")
    Instant createdAt,
    
    @Schema(description = "Data de atualização")
    Instant updatedAt,
    
    @Schema(description = "Workspace associado ao usuário")
    WorkspaceDto workspace,
    
    @Schema(description = "Token JWT para autenticação", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @JsonProperty("access_token")
    String accessToken
) {}


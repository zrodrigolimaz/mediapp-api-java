package com.mediapp.api.dto.auth;

import com.mediapp.api.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "DTO para dados do perfil do usuário autenticado")
public record ProfileDto(
    @Schema(description = "ID do usuário", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,
    
    @Schema(description = "Nome completo do usuário", example = "Dr. João Silva")
    String fullName,
    
    @Schema(description = "Email do usuário", example = "joao.silva@example.com")
    String email,
    
    @Schema(description = "Papel do usuário no workspace", example = "ADMIN")
    UserRole role,
    
    @Schema(description = "CRM do médico (opcional)", example = "123456")
    String crm,
    
    @Schema(description = "URL da assinatura digital (opcional)")
    String digitalSignatureUrl,
    
    @Schema(description = "Dados do workspace do usuário")
    WorkspaceDto workspace,
    
    @Schema(description = "Data de criação do usuário")
    Instant createdAt,
    
    @Schema(description = "Data de última atualização do usuário")
    Instant updatedAt
) {}


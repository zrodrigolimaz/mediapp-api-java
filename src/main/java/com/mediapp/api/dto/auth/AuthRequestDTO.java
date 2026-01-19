package com.mediapp.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de requisição para o fluxo de registro.
 * Usado no endpoint POST /api/auth/register.
 */
@Schema(description = "DTO de requisição para registro de usuário")
public record AuthRequestDTO(

    @Schema(description = "Nome completo do usuário", example = "Dr. João Silva", required = true)
    @NotBlank(message = "O nome completo é obrigatório.")
    @Size(min = 2, message = "O nome deve ter pelo menos 2 caracteres.")
    String fullName,

    @Schema(description = "Email do usuário", example = "joao@example.com", required = true)
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Por favor, insira um e-mail válido.")
    String email,

    @Schema(description = "Senha do usuário (mínimo 6 caracteres)", example = "123456", required = true)
    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
    String password,

    @Schema(description = "Nome do workspace/clínica", example = "Clínica Dr. João", required = true)
    @NotBlank(message = "O nome do workspace é obrigatório.")
    @Size(min = 2, message = "O nome do workspace deve ter pelo menos 2 caracteres.")
    String workspaceName
) {}


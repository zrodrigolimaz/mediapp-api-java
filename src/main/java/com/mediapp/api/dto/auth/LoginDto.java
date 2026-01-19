package com.mediapp.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO de requisição para login")
public record LoginDto(

    @Schema(description = "Email do usuário", example = "joao@example.com", required = true)
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Por favor, insira um e-mail válido.")
    String email,

    @Schema(description = "Senha do usuário", example = "123456", required = true)
    @NotBlank(message = "A senha é obrigatória.")
    String password
) {}


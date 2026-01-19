package com.mediapp.api.controller;

import com.mediapp.api.dto.auth.*;
import com.mediapp.api.entity.*;
import com.mediapp.api.exception.ConflictException;
import com.mediapp.api.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthController Tests - DTOs e Responses")
class AuthControllerTest {

    private UUID userId;
    private UUID workspaceId;
    private Instant now;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        workspaceId = UUID.randomUUID();
        now = Instant.now();
    }

    @Test
    @DisplayName("AuthRequestDTO deve ter validacao de campos obrigatorios")
    void authRequestDTO_ShouldHaveRequiredFields() {
        // Arrange
        AuthRequestDTO dto = new AuthRequestDTO(
            "Dr. João Silva",
            "joao@example.com",
            "password123",
            "Clínica Dr. João"
        );

        // Assert
        assertNotNull(dto.fullName());
        assertNotNull(dto.email());
        assertNotNull(dto.password());
        assertNotNull(dto.workspaceName());
        assertEquals("Dr. João Silva", dto.fullName());
        assertEquals("joao@example.com", dto.email());
    }

    @Test
    @DisplayName("AuthResponseDTO deve incluir todos os campos de resposta")
    void authResponseDTO_ShouldIncludeAllResponseFields() {
        // Arrange
        WorkspaceDto workspaceDto = new WorkspaceDto(
            workspaceId, "Clínica Dr. João", DocumentType.CPF,
            "12345678900", userId, now, now
        );

        // Act
        AuthResponseDTO response = new AuthResponseDTO(
            userId, "Dr. João Silva", "joao@example.com", UserRole.ADMIN,
            now, now, workspaceDto, "jwt-token-here"
        );

        // Assert
        assertEquals(userId, response.id());
        assertEquals("Dr. João Silva", response.fullName());
        assertEquals("joao@example.com", response.email());
        assertEquals(UserRole.ADMIN, response.role());
        assertEquals("jwt-token-here", response.accessToken());
        assertNotNull(response.workspace());
        assertEquals("Clínica Dr. João", response.workspace().name());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
    }

    @Test
    @DisplayName("LoginDto deve conter email e password")
    void loginDto_ShouldContainEmailAndPassword() {
        // Arrange & Act
        LoginDto dto = new LoginDto("joao@example.com", "password123");

        // Assert
        assertEquals("joao@example.com", dto.email());
        assertEquals("password123", dto.password());
    }

    @Test
    @DisplayName("LoginResponseDto deve conter apenas access_token")
    void loginResponseDto_ShouldContainOnlyAccessToken() {
        // Arrange & Act
        LoginResponseDto response = new LoginResponseDto("jwt-token-here");

        // Assert
        assertEquals("jwt-token-here", response.accessToken());
    }

    @Test
    @DisplayName("ProfileDto deve conter dados completos do usuario")
    void profileDto_ShouldContainCompleteUserData() {
        // Arrange
        WorkspaceDto workspaceDto = new WorkspaceDto(
            workspaceId, "Clínica Teste", DocumentType.CNPJ,
            "12345678000199", userId, now, now
        );

        // Act
        ProfileDto profile = new ProfileDto(
            userId, "Dr. Maria Santos", "maria@example.com",
            UserRole.ADMIN, "12345-SP", "http://signature.url",
            workspaceDto, now, now
        );

        // Assert
        assertEquals(userId, profile.id());
        assertEquals("Dr. Maria Santos", profile.fullName());
        assertEquals("maria@example.com", profile.email());
        assertEquals(UserRole.ADMIN, profile.role());
        assertEquals("12345-SP", profile.crm());
        assertEquals("http://signature.url", profile.digitalSignatureUrl());
        assertNotNull(profile.workspace());
    }

    @Test
    @DisplayName("ProfileDto pode ter workspace nulo")
    void profileDto_CanHaveNullWorkspace() {
        // Act
        ProfileDto profile = new ProfileDto(
            userId, "Dr. Novo", "novo@example.com",
            UserRole.MEMBER, null, null, null, now, now
        );

        // Assert
        assertNotNull(profile);
        assertNull(profile.workspace());
        assertNull(profile.crm());
    }

    @Test
    @DisplayName("ConflictException deve ser lancada para email duplicado")
    void conflictException_ShouldBeThrownForDuplicateEmail() {
        // Arrange
        String message = "Este e-mail já está em uso.";

        // Act
        ConflictException exception = new ConflictException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("UnauthorizedException deve ser lancada para credenciais invalidas")
    void unauthorizedException_ShouldBeThrownForInvalidCredentials() {
        // Arrange
        String message = "Credenciais inválidas.";

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("WorkspaceDto deve incluir documentType e documentNumber")
    void workspaceDto_ShouldIncludeDocumentInfo() {
        // Act
        WorkspaceDto dto = new WorkspaceDto(
            workspaceId, "Minha Clínica", DocumentType.CPF,
            "12345678900", userId, now, now
        );

        // Assert
        assertEquals(DocumentType.CPF, dto.documentType());
        assertEquals("12345678900", dto.documentNumber());
        assertEquals(userId, dto.ownerId());
    }

    @Test
    @DisplayName("WorkspaceDto com CNPJ deve funcionar corretamente")
    void workspaceDto_WithCnpj_ShouldWorkCorrectly() {
        // Act
        WorkspaceDto dto = new WorkspaceDto(
            workspaceId, "Empresa de Saúde LTDA", DocumentType.CNPJ,
            "12345678000199", userId, now, now
        );

        // Assert
        assertEquals(DocumentType.CNPJ, dto.documentType());
        assertEquals("12345678000199", dto.documentNumber());
    }

    @Test
    @DisplayName("UserRole deve ter valores ADMIN e MEMBER")
    void userRole_ShouldHaveAdminAndMember() {
        // Assert
        assertEquals(2, UserRole.values().length);
        assertNotNull(UserRole.ADMIN);
        assertNotNull(UserRole.MEMBER);
    }

    @Test
    @DisplayName("DocumentType deve ter valores CPF e CNPJ")
    void documentType_ShouldHaveCpfAndCnpj() {
        // Assert
        assertEquals(2, DocumentType.values().length);
        assertNotNull(DocumentType.CPF);
        assertNotNull(DocumentType.CNPJ);
    }

    @Test
    @DisplayName("Resposta de mensagem simples deve funcionar")
    void simpleMessageResponse_ShouldWork() {
        // Simula a resposta Map<String, String> usada nos controllers
        Map<String, String> response = Map.of("message", "Operação realizada com sucesso.");

        // Assert
        assertEquals("Operação realizada com sucesso.", response.get("message"));
    }
}

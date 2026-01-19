package com.mediapp.api.service;

import com.mediapp.api.dto.auth.*;
import com.mediapp.api.entity.*;
import com.mediapp.api.exception.ConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthService Tests - DTOs e Entities")
class AuthServiceTest {

    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setRole(UserRole.ADMIN);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        
        Workspace workspace = new Workspace();
        workspace.setId(UUID.randomUUID());
        workspace.setName("Test Workspace");
        workspace.setDocumentType(DocumentType.CPF);
        workspace.setDocumentNumber("12345678900");
        workspace.setCreatedAt(Instant.now());
        workspace.setUpdatedAt(Instant.now());
        
        user.setWorkspace(workspace);
        return user;
    }

    @Test
    @DisplayName("ProfileDto deve conter todos os campos do usuário")
    void profileDto_ShouldContainAllUserFields() {
        // Arrange
        User user = createTestUser();
        
        ProfileDto profile = new ProfileDto(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getCrm(),
            user.getDigitalSignatureUrl(),
            new WorkspaceDto(
                user.getWorkspace().getId(),
                user.getWorkspace().getName(),
                user.getWorkspace().getDocumentType(),
                user.getWorkspace().getDocumentNumber(),
                user.getId(),
                user.getWorkspace().getCreatedAt(),
                user.getWorkspace().getUpdatedAt()
            ),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );

        // Assert
        assertNotNull(profile);
        assertEquals(user.getId(), profile.id());
        assertEquals("Test User", profile.fullName());
        assertEquals("test@example.com", profile.email());
        assertEquals(UserRole.ADMIN, profile.role());
        assertNotNull(profile.workspace());
        assertEquals("Test Workspace", profile.workspace().name());
    }

    @Test
    @DisplayName("ProfileDto sem workspace deve ter workspace nulo")
    void profileDto_WithoutWorkspace_ShouldHaveNullWorkspace() {
        // Arrange
        User user = createTestUser();
        user.setWorkspace(null);

        ProfileDto profile = new ProfileDto(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getCrm(),
            user.getDigitalSignatureUrl(),
            null,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );

        // Assert
        assertNotNull(profile);
        assertNull(profile.workspace());
    }

    @Test
    @DisplayName("AuthResponseDTO deve conter todos os campos corretamente")
    void authResponseDTO_ShouldContainAllFields() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        Instant now = Instant.now();
        
        WorkspaceDto workspaceDto = new WorkspaceDto(
            workspaceId, "Test Workspace", DocumentType.CPF, 
            "12345678900", userId, now, now
        );

        // Act
        AuthResponseDTO response = new AuthResponseDTO(
            userId, "Test User", "test@example.com", UserRole.ADMIN,
            now, now, workspaceDto, "jwt-token"
        );

        // Assert
        assertEquals(userId, response.id());
        assertEquals("Test User", response.fullName());
        assertEquals("test@example.com", response.email());
        assertEquals(UserRole.ADMIN, response.role());
        assertEquals("jwt-token", response.accessToken());
        assertNotNull(response.workspace());
        assertEquals(workspaceId, response.workspace().id());
    }

    @Test
    @DisplayName("LoginResponseDto deve conter access token")
    void loginResponseDto_ShouldContainAccessToken() {
        // Arrange & Act
        LoginResponseDto response = new LoginResponseDto("test-jwt-token");

        // Assert
        assertEquals("test-jwt-token", response.accessToken());
    }

    @Test
    @DisplayName("WorkspaceDto deve conter todos os campos")
    void workspaceDto_ShouldContainAllFields() {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant now = Instant.now();

        // Act
        WorkspaceDto dto = new WorkspaceDto(
            workspaceId, "My Workspace", DocumentType.CNPJ,
            "12345678000199", ownerId, now, now
        );

        // Assert
        assertEquals(workspaceId, dto.id());
        assertEquals("My Workspace", dto.name());
        assertEquals(DocumentType.CNPJ, dto.documentType());
        assertEquals("12345678000199", dto.documentNumber());
        assertEquals(ownerId, dto.ownerId());
    }

    @Test
    @DisplayName("LoginDto deve conter email e password")
    void loginDto_ShouldContainEmailAndPassword() {
        // Arrange & Act
        LoginDto dto = new LoginDto("test@example.com", "password123");

        // Assert
        assertEquals("test@example.com", dto.email());
        assertEquals("password123", dto.password());
    }

    @Test
    @DisplayName("AuthRequestDTO deve conter todos os campos de registro")
    void authRequestDTO_ShouldContainAllRegistrationFields() {
        // Arrange & Act
        AuthRequestDTO dto = new AuthRequestDTO(
            "Dr. João Silva",
            "joao@example.com",
            "password123",
            "Clínica Dr. João"
        );

        // Assert
        assertEquals("Dr. João Silva", dto.fullName());
        assertEquals("joao@example.com", dto.email());
        assertEquals("password123", dto.password());
        assertEquals("Clínica Dr. João", dto.workspaceName());
    }

    @Test
    @DisplayName("User entity deve ter todos os campos corretamente")
    void userEntity_ShouldHaveAllFieldsCorrectly() {
        // Arrange & Act
        User user = createTestUser();

        // Assert
        assertNotNull(user.getId());
        assertEquals("Test User", user.getFullName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedPassword", user.getPasswordHash());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertNotNull(user.getWorkspace());
    }

    @Test
    @DisplayName("UserRole enum deve ter valores ADMIN e MEMBER")
    void userRoleEnum_ShouldHaveAdminAndMember() {
        // Assert
        assertEquals(2, UserRole.values().length);
        assertNotNull(UserRole.ADMIN);
        assertNotNull(UserRole.MEMBER);
    }

    @Test
    @DisplayName("DocumentType enum deve ter CPF e CNPJ")
    void documentTypeEnum_ShouldHaveCpfAndCnpj() {
        // Assert
        assertEquals(2, DocumentType.values().length);
        assertNotNull(DocumentType.CPF);
        assertNotNull(DocumentType.CNPJ);
    }

    @Test
    @DisplayName("ConflictException deve ter mensagem correta")
    void conflictException_ShouldHaveCorrectMessage() {
        // Arrange
        String message = "Este e-mail já está em uso.";
        
        // Act
        ConflictException exception = new ConflictException(message);
        
        // Assert
        assertEquals(message, exception.getMessage());
    }
}

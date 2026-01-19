package com.mediapp.api.security;

import com.mediapp.api.entity.User;
import com.mediapp.api.entity.UserRole;
import com.mediapp.api.entity.Workspace;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private SecretKey secretKey;
    private static final String SECRET = "test-secret-key-for-testing-purposes-only-must-be-at-least-32-characters-long";
    private static final long EXPIRATION_MS = 3600000L; // 1 hora

    @BeforeEach
    void setUp() {
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        jwtUtil = new JwtUtil(secretKey, EXPIRATION_MS);
    }

    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setRole(UserRole.ADMIN);
        
        Workspace workspace = new Workspace();
        workspace.setId(UUID.randomUUID());
        workspace.setName("Test Workspace");
        user.setWorkspace(workspace);
        
        return user;
    }

    @Test
    @DisplayName("generateToken deve retornar um token JWT válido")
    void generateToken_ShouldReturnValidToken() {
        // Arrange
        User user = createTestUser();

        // Act
        String token = jwtUtil.generateToken(user);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tem 3 partes separadas por ponto
    }

    @Test
    @DisplayName("generateToken deve incluir claims corretos")
    void generateToken_ShouldIncludeCorrectClaims() {
        // Arrange
        User user = createTestUser();

        // Act
        String token = jwtUtil.generateToken(user);

        // Assert
        var claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertEquals(user.getId().toString(), claims.getSubject());
        assertEquals(user.getEmail(), claims.get("email"));
        assertEquals(user.getWorkspace().getId().toString(), claims.get("workspaceId"));
        assertEquals(user.getRole().name(), claims.get("role"));
    }

    @Test
    @DisplayName("generateToken sem workspace deve funcionar")
    void generateToken_WithoutWorkspace_ShouldWork() {
        // Arrange
        User user = createTestUser();
        user.setWorkspace(null);

        // Act
        String token = jwtUtil.generateToken(user);

        // Assert
        assertNotNull(token);
        
        var claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertNull(claims.get("workspaceId"));
    }

    @Test
    @DisplayName("validateToken com token válido deve retornar true")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        User user = createTestUser();
        String token = jwtUtil.generateToken(user);

        // Act
        boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("validateToken com token inválido deve retornar false")
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken com token nulo deve retornar false")
    void validateToken_WithNullToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken com token vazio deve retornar false")
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken com token de outra chave deve retornar false")
    void validateToken_WithDifferentKey_ShouldReturnFalse() {
        // Arrange
        SecretKey differentKey = Keys.hmacShaKeyFor(
            "different-secret-key-for-testing-purposes-only-32-chars".getBytes(StandardCharsets.UTF_8)
        );
        JwtUtil differentJwtUtil = new JwtUtil(differentKey, EXPIRATION_MS);
        
        User user = createTestUser();
        String tokenFromDifferentKey = differentJwtUtil.generateToken(user);

        // Act
        boolean isValid = jwtUtil.validateToken(tokenFromDifferentKey);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("extractUserId deve retornar o UUID correto do usuário")
    void extractUserId_ShouldReturnCorrectUserId() {
        // Arrange
        User user = createTestUser();
        String token = jwtUtil.generateToken(user);

        // Act
        UUID extractedUserId = jwtUtil.extractUserId(token);

        // Assert
        assertEquals(user.getId(), extractedUserId);
    }

    @Test
    @DisplayName("extractUserId com token inválido deve lançar exceção")
    void extractUserId_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractUserId(invalidToken));
    }
}


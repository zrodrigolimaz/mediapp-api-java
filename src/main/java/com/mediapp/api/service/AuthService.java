package com.mediapp.api.service;

import com.mediapp.api.dto.auth.AuthRequestDTO;
import com.mediapp.api.dto.auth.AuthResponseDTO;
import com.mediapp.api.dto.auth.LoginDto;
import com.mediapp.api.dto.auth.LoginResponseDto;
import com.mediapp.api.dto.auth.ProfileDto;
import com.mediapp.api.dto.auth.WorkspaceDto;
import com.mediapp.api.entity.DocumentType;
import com.mediapp.api.entity.User;
import com.mediapp.api.entity.UserRole;
import com.mediapp.api.entity.Workspace;
import com.mediapp.api.exception.ConflictException;
import com.mediapp.api.exception.UnauthorizedException;
import com.mediapp.api.repository.UserRepository;
import com.mediapp.api.repository.WorkspaceRepository;
import com.mediapp.api.security.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DataSource dataSource;
    
    @PersistenceContext
    private EntityManager entityManager;

    public AuthService(UserRepository userRepository, WorkspaceRepository workspaceRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponseDTO register(AuthRequestDTO dto) {
        // Verificar se o email já existe usando query nativa para evitar problemas de tipo
        String checkEmailSql = "SELECT COUNT(*) FROM users WHERE email = ?";
        boolean emailExists = false;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkEmailSql)) {
            stmt.setString(1, dto.email());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    emailExists = true;
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao verificar email: " + e.getMessage(), e);
        }
        
        if (emailExists) {
            throw new ConflictException("Este e-mail já está em uso.");
        }

        String passwordHash = passwordEncoder.encode(dto.password());
        String documentNumber = "REG" + System.currentTimeMillis() + String.format("%02d", new Random().nextInt(100));

        // Criar usuário usando DataSource diretamente para controle total
        UUID userId = UUID.randomUUID();
        java.time.Instant now = java.time.Instant.now();
        java.sql.Timestamp nowTimestamp = java.sql.Timestamp.from(now);
        
        String insertUserSql = "INSERT INTO users (id, workspace_id, full_name, email, password_hash, crm, role, digital_signature_url, password_reset_token, password_reset_expires, created_at, updated_at) " +
            "VALUES (CAST(? AS uuid), NULL, ?, ?, ?, NULL, CAST(? AS users_role_enum), NULL, NULL, NULL, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertUserSql)) {
            stmt.setString(1, userId.toString());
            stmt.setString(2, dto.fullName());
            stmt.setString(3, dto.email());
            stmt.setString(4, passwordHash);
            stmt.setString(5, "ADMIN");
            stmt.setTimestamp(6, nowTimestamp);
            stmt.setTimestamp(7, nowTimestamp);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Nenhuma linha foi inserida na tabela users");
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao criar usuário: " + e.getMessage(), e);
        }
        
        // Buscar o usuário recém-criado usando query nativa para evitar problemas de tipo do Hibernate
        String selectUserSql = "SELECT id, workspace_id, full_name, email, password_hash, crm, role, digital_signature_url, password_reset_token, password_reset_expires, created_at, updated_at FROM users WHERE email = ?";
        User user = null;
        try (Connection conn2 = dataSource.getConnection();
             PreparedStatement stmt = conn2.prepareStatement(selectUserSql)) {
            stmt.setString(1, dto.email());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(UUID.fromString(rs.getString("id")));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setCrm(rs.getString("crm"));
                    String roleStr = rs.getString("role");
                    user.setRole(roleStr != null ? UserRole.valueOf(roleStr) : UserRole.MEMBER);
                    user.setDigitalSignatureUrl(rs.getString("digital_signature_url"));
                    user.setPasswordResetToken(rs.getString("password_reset_token"));
                    if (rs.getTimestamp("password_reset_expires") != null) {
                        user.setPasswordResetExpires(rs.getTimestamp("password_reset_expires").toInstant());
                    }
                    user.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    user.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário: " + e.getMessage(), e);
        }
        
        if (user == null) {
            throw new RuntimeException("Erro ao buscar usuário recém-criado");
        }

        // Criar workspace usando DataSource diretamente
        UUID workspaceId = UUID.randomUUID();
        String insertWorkspaceSql = "INSERT INTO workspaces (id, name, document_type, document_number, owner_id, created_at, updated_at) " +
            "VALUES (CAST(? AS uuid), ?, CAST(? AS workspaces_document_type_enum), ?, CAST(? AS uuid), ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertWorkspaceSql)) {
            stmt.setString(1, workspaceId.toString());
            stmt.setString(2, dto.workspaceName());
            stmt.setString(3, "CPF");
            stmt.setString(4, documentNumber);
            stmt.setString(5, user.getId().toString());
            stmt.setTimestamp(6, nowTimestamp);
            stmt.setTimestamp(7, nowTimestamp);
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao criar workspace: " + e.getMessage(), e);
        }
        
        // Buscar o workspace recém-criado usando query nativa para evitar problemas de tipo do Hibernate
        String selectWorkspaceSql = "SELECT id, name, document_type, document_number, owner_id, created_at, updated_at FROM workspaces WHERE id = CAST(? AS uuid)";
        Workspace workspace = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectWorkspaceSql)) {
            stmt.setString(1, workspaceId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    workspace = new Workspace();
                    workspace.setId(UUID.fromString(rs.getString("id")));
                    workspace.setName(rs.getString("name"));
                    String documentTypeStr = rs.getString("document_type");
                    workspace.setDocumentType(documentTypeStr != null ? DocumentType.valueOf(documentTypeStr) : DocumentType.CPF);
                    workspace.setDocumentNumber(rs.getString("document_number"));
                    // owner será setado depois, não precisamos buscar aqui
                    workspace.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    workspace.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao buscar workspace: " + e.getMessage(), e);
        }
        
        if (workspace == null) {
            throw new RuntimeException("Erro ao buscar workspace recém-criado");
        }
        
        // Atualizar o usuário com workspace_id usando query nativa
        String updateUserSql = "UPDATE users SET workspace_id = CAST(? AS uuid), updated_at = ? WHERE id = CAST(? AS uuid)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateUserSql)) {
            stmt.setString(1, workspaceId.toString());
            stmt.setTimestamp(2, java.sql.Timestamp.from(java.time.Instant.now()));
            stmt.setString(3, user.getId().toString());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Nenhuma linha foi atualizada na tabela users");
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao atualizar usuário: " + e.getMessage(), e);
        }
        
        // Atualizar o objeto user localmente para retornar
        user.setWorkspace(workspace);

        WorkspaceDto workspaceDto = new WorkspaceDto(
            workspace.getId(),
            workspace.getName(),
            workspace.getDocumentType(),
            workspace.getDocumentNumber(),
            user.getId(),
            workspace.getCreatedAt(),
            workspace.getUpdatedAt()
        );

        String accessToken = jwtUtil.generateToken(user);

        return new AuthResponseDTO(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            workspaceDto,
            accessToken
        );
    }

    public LoginResponseDto login(LoginDto dto) {
        // Buscar usuário e workspace usando query nativa para evitar problemas de tipo do Hibernate
        String selectUserWithWorkspaceSql = "SELECT u.id, u.workspace_id, u.full_name, u.email, u.password_hash, u.crm, u.role, " +
            "u.digital_signature_url, u.password_reset_token, u.password_reset_expires, u.created_at, u.updated_at, " +
            "w.id as workspace_id_col, w.name as workspace_name, w.document_type, w.document_number, w.owner_id, " +
            "w.created_at as workspace_created_at, w.updated_at as workspace_updated_at " +
            "FROM users u LEFT JOIN workspaces w ON u.workspace_id = w.id WHERE u.email = ?";
        
        User user = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectUserWithWorkspaceSql)) {
            stmt.setString(1, dto.email());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(UUID.fromString(rs.getString("id")));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setCrm(rs.getString("crm"));
                    String roleStr = rs.getString("role");
                    user.setRole(roleStr != null ? UserRole.valueOf(roleStr) : UserRole.MEMBER);
                    user.setDigitalSignatureUrl(rs.getString("digital_signature_url"));
                    user.setPasswordResetToken(rs.getString("password_reset_token"));
                    if (rs.getTimestamp("password_reset_expires") != null) {
                        user.setPasswordResetExpires(rs.getTimestamp("password_reset_expires").toInstant());
                    }
                    user.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    user.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                    
                    // Carregar workspace se existir
                    String workspaceIdStr = rs.getString("workspace_id_col");
                    if (workspaceIdStr != null) {
                        Workspace workspace = new Workspace();
                        workspace.setId(UUID.fromString(workspaceIdStr));
                        workspace.setName(rs.getString("workspace_name"));
                        String documentTypeStr = rs.getString("document_type");
                        workspace.setDocumentType(documentTypeStr != null ? DocumentType.valueOf(documentTypeStr) : DocumentType.CPF);
                        workspace.setDocumentNumber(rs.getString("document_number"));
                        workspace.setCreatedAt(rs.getTimestamp("workspace_created_at").toInstant());
                        workspace.setUpdatedAt(rs.getTimestamp("workspace_updated_at").toInstant());
                        user.setWorkspace(workspace);
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário: " + e.getMessage(), e);
        }
        
        if (user == null) {
            throw new UnauthorizedException("Credenciais inválidas.");
        }

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciais inválidas.");
        }

        String token = jwtUtil.generateToken(user);
        return new LoginResponseDto(token);
    }
    
    public ProfileDto getProfile(User user) {
        // Sempre buscar workspace usando query nativa para evitar problemas de lazy loading
        Workspace workspace = null;
        if (user.getId() != null) {
            String selectWorkspaceSql = "SELECT w.id, w.name, w.document_type, w.document_number, w.owner_id, w.created_at, w.updated_at " +
                "FROM workspaces w INNER JOIN users u ON w.id = u.workspace_id WHERE u.id = CAST(? AS uuid)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(selectWorkspaceSql)) {
                stmt.setString(1, user.getId().toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        workspace = new Workspace();
                        workspace.setId(UUID.fromString(rs.getString("id")));
                        workspace.setName(rs.getString("name"));
                        String documentTypeStr = rs.getString("document_type");
                        workspace.setDocumentType(documentTypeStr != null ? DocumentType.valueOf(documentTypeStr) : DocumentType.CPF);
                        workspace.setDocumentNumber(rs.getString("document_number"));
                        workspace.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                        workspace.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                    }
                }
            } catch (java.sql.SQLException e) {
                throw new RuntimeException("Erro ao buscar workspace: " + e.getMessage(), e);
            }
        }
        
        WorkspaceDto workspaceDto = null;
        if (workspace != null) {
            workspaceDto = new WorkspaceDto(
                workspace.getId(),
                workspace.getName(),
                workspace.getDocumentType(),
                workspace.getDocumentNumber(),
                user.getId(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt()
            );
        }
        
        return new ProfileDto(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getCrm(),
            user.getDigitalSignatureUrl(),
            workspaceDto,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}


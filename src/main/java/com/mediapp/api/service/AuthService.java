package com.mediapp.api.service;

import com.mediapp.api.dto.auth.AuthRequestDTO;
import com.mediapp.api.dto.auth.AuthResponseDTO;
import com.mediapp.api.dto.auth.LoginDto;
import com.mediapp.api.dto.auth.LoginResponseDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, WorkspaceRepository workspaceRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponseDTO register(AuthRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("Este e-mail já está em uso.");
        }

        String passwordHash = passwordEncoder.encode(dto.password());
        String documentNumber = "REG" + System.currentTimeMillis() + String.format("%02d", new Random().nextInt(100));

        User user = new User();
        user.setFullName(dto.fullName());
        user.setEmail(dto.email());
        user.setPasswordHash(passwordHash);
        user.setRole(UserRole.ADMIN);
        user.setWorkspace(null);
        user = userRepository.saveAndFlush(user);

        Workspace workspace = new Workspace();
        workspace.setName(dto.workspaceName());
        workspace.setDocumentType(DocumentType.CPF);
        workspace.setDocumentNumber(documentNumber);
        workspace.setOwner(user);
        workspace = workspaceRepository.save(workspace);

        user.setWorkspace(workspace);
        user = userRepository.save(user);

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
        User user = userRepository.findByEmailWithWorkspace(dto.email())
            .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas."));

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciais inválidas.");
        }

        String token = jwtUtil.generateToken(user);
        return new LoginResponseDto(token);
    }
}


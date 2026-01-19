package com.mediapp.api.controller;

import com.mediapp.api.dto.auth.AuthRequestDTO;
import com.mediapp.api.dto.auth.AuthResponseDTO;
import com.mediapp.api.dto.auth.LoginDto;
import com.mediapp.api.dto.auth.LoginResponseDto;
import com.mediapp.api.dto.auth.ProfileDto;
import com.mediapp.api.entity.User;
import com.mediapp.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints de autenticação e registro")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Registrar novo usuário",
        description = "Cria um novo usuário com workspace. Retorna dados do usuário e token JWT."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Email já está em uso")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody AuthRequestDTO dto) {
        AuthResponseDTO response = authService.register(dto);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(
        summary = "Fazer login",
        description = "Autentica usuário e retorna token JWT"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginDto dto) {
        LoginResponseDto response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obter perfil do usuário",
        description = "Retorna dados do usuário autenticado com workspace"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponse(responseCode = "200", description = "Dados do usuário")
    @GetMapping("/profile")
    public ResponseEntity<ProfileDto> profile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        ProfileDto profile = authService.getProfile(user);
        return ResponseEntity.ok(profile);
    }
}


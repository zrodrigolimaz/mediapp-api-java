package com.mediapp.api.controller;

import com.mediapp.api.dto.patient.CreatePatientDto;
import com.mediapp.api.dto.patient.UpdatePatientDto;
import com.mediapp.api.entity.Patient;
import com.mediapp.api.entity.User;
import com.mediapp.api.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Patients", description = "Endpoints para gerenciamento de pacientes")
@RestController
@RequestMapping("/api/patients")
@SecurityRequirement(name = "Bearer Authentication")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @Operation(
        summary = "Criar novo paciente",
        description = "Cria um novo paciente no workspace do usuário autenticado. CPF deve ser único dentro do workspace."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Paciente criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "CPF já cadastrado neste consultório"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou ausente")
    })
    @PostMapping
    public ResponseEntity<Patient> create(@Valid @RequestBody CreatePatientDto dto) {
        User user = getCurrentUser();
        Patient patient = patientService.create(dto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(patient);
    }

    @Operation(
        summary = "Listar pacientes",
        description = "Retorna todos os pacientes ativos do workspace do usuário autenticado, ordenados por nome."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de pacientes retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou ausente")
    })
    @GetMapping
    public ResponseEntity<List<Patient>> findAll() {
        User user = getCurrentUser();
        List<Patient> patients = patientService.findAll(user);
        return ResponseEntity.ok(patients);
    }

    @Operation(
        summary = "Buscar paciente por ID",
        description = "Retorna os dados de um paciente específico do workspace do usuário autenticado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paciente encontrado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou ausente")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Patient> findOne(
        @Parameter(description = "UUID do paciente", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id
    ) {
        User user = getCurrentUser();
        Patient patient = patientService.findOne(id, user);
        return ResponseEntity.ok(patient);
    }

    @Operation(
        summary = "Atualizar paciente",
        description = "Atualiza parcialmente os dados de um paciente do workspace do usuário autenticado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paciente atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado"),
        @ApiResponse(responseCode = "409", description = "CPF já cadastrado neste consultório"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou ausente")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Patient> update(
        @Parameter(description = "UUID do paciente", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id,
        @Valid @RequestBody UpdatePatientDto dto
    ) {
        User user = getCurrentUser();
        Patient patient = patientService.update(id, dto, user);
        return ResponseEntity.ok(patient);
    }

    @Operation(
        summary = "Remover paciente",
        description = "Remove um paciente do workspace do usuário autenticado (soft delete - marca como inativo)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paciente removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Paciente não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido ou ausente")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> remove(
        @Parameter(description = "UUID do paciente", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id
    ) {
        User user = getCurrentUser();
        patientService.remove(id, user);
        return ResponseEntity.ok(Map.of("message", "Paciente removido com sucesso."));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }
}


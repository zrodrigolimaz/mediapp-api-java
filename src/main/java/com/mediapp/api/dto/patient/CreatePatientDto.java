package com.mediapp.api.dto.patient;

import com.mediapp.api.entity.SexType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "DTO para criação de paciente")
public record CreatePatientDto(

    @Schema(description = "Nome completo do paciente", example = "João Silva Santos", required = true)
    @NotBlank(message = "O nome completo é obrigatório.")
    @Size(min = 1, message = "O nome completo deve ter pelo menos 1 caractere.")
    String fullName,

    @Schema(description = "CPF do paciente (formato XXX.XXX.XXX-XX ou 11 dígitos)", example = "123.456.789-00", required = true)
    @NotBlank(message = "O CPF é obrigatório.")
    @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$|^\\d{11}$", message = "CPF deve estar no formato XXX.XXX.XXX-XX ou conter 11 dígitos.")
    String taxId,

    @Schema(description = "Data de nascimento do paciente", example = "1990-05-15", required = true)
    @NotBlank(message = "A data de nascimento é obrigatória.")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "A data de nascimento deve estar no formato YYYY-MM-DD.")
    String birthDate,

    @Schema(description = "Telefone de contato principal", example = "(11) 98765-4321", required = true)
    @NotBlank(message = "O telefone de contato é obrigatório.")
    String contactPhone,

    @Schema(description = "RG do paciente", example = "12.345.678-9", required = false)
    String identityDocument,

    @Schema(description = "Sexo do paciente", example = "MALE", required = false)
    SexType sex,

    @Schema(description = "Telefone de contato secundário", example = "(11) 91234-5678", required = false)
    String secondaryContactPhone,

    @Schema(description = "E-mail do paciente", example = "joao.silva@email.com", required = false)
    @Email(message = "Por favor, insira um e-mail válido.")
    String email,

    @Schema(description = "CEP do endereço", example = "01234-567", required = false)
    String zipCode,

    @Schema(description = "Rua do endereço", example = "Rua das Flores", required = false)
    String addressStreet,

    @Schema(description = "Número do endereço", example = "123", required = false)
    String addressNumber,

    @Schema(description = "Complemento do endereço", example = "Apto 45", required = false)
    String addressComplement,

    @Schema(description = "Bairro do endereço", example = "Centro", required = false)
    String addressNeighborhood,

    @Schema(description = "Cidade do endereço", example = "São Paulo", required = false)
    String addressCity,

    @Schema(description = "Estado do endereço (sigla)", example = "SP", required = false)
    String addressState,

    @Schema(description = "Nome completo do responsável", example = "Maria Silva Santos", required = false)
    String guardianFullName,

    @Schema(description = "CPF do responsável", example = "987.654.321-00", required = false)
    String guardianTaxId,

    @Schema(description = "Telefone de contato do responsável", example = "(11) 99876-5432", required = false)
    String guardianContactPhone,

    @Schema(description = "Plano de saúde", example = "Unimed", required = false)
    String healthInsurance,

    @Schema(description = "Número da carteirinha do plano", example = "123456789", required = false)
    String insuranceCardNumber,

    @Schema(description = "Alergias conhecidas", example = "Alergia a penicilina, alergia a frutos do mar", required = false)
    String allergies,

    @Schema(description = "Fototipo de Fitzpatrick (1-6)", example = "3", minimum = "1", maximum = "6", required = false)
    @Min(value = 1, message = "O fototipo deve ser entre 1 e 6.")
    @Max(value = 6, message = "O fototipo deve ser entre 1 e 6.")
    Integer fitzpatrickPhototype,

    @Schema(description = "Observações gerais sobre o paciente", example = "Paciente com histórico de hipertensão controlada", required = false)
    String generalObservations
) {}


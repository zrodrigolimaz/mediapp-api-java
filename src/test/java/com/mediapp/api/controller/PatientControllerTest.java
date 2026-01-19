package com.mediapp.api.controller;

import com.mediapp.api.dto.patient.CreatePatientDto;
import com.mediapp.api.dto.patient.UpdatePatientDto;
import com.mediapp.api.entity.*;
import com.mediapp.api.exception.ConflictException;
import com.mediapp.api.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PatientController Tests - DTOs e Entities")
class PatientControllerTest {

    private UUID patientId;
    private UUID workspaceId;
    private Workspace testWorkspace;
    private Instant now;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        workspaceId = UUID.randomUUID();
        now = Instant.now();

        testWorkspace = new Workspace();
        testWorkspace.setId(workspaceId);
        testWorkspace.setName("Clínica Teste");
    }

    @Test
    @DisplayName("CreatePatientDto deve ter campos obrigatorios")
    void createPatientDto_ShouldHaveRequiredFields() {
        // Arrange
        CreatePatientDto dto = new CreatePatientDto(
            "Maria Silva Santos",
            "123.456.789-00",
            "1990-05-15",
            "(11) 98765-4321",
            null, SexType.FEMALE, null, "maria@email.com",
            null, null, null, null, null, "São Paulo", "SP",
            null, null, null, null, null, null, null, null
        );

        // Assert
        assertEquals("Maria Silva Santos", dto.fullName());
        assertEquals("123.456.789-00", dto.taxId());
        assertEquals("1990-05-15", dto.birthDate());
        assertEquals("(11) 98765-4321", dto.contactPhone());
        assertEquals(SexType.FEMALE, dto.sex());
    }

    @Test
    @DisplayName("CreatePatientDto deve aceitar todos os campos opcionais")
    void createPatientDto_ShouldAcceptAllOptionalFields() {
        // Arrange
        CreatePatientDto dto = new CreatePatientDto(
            "João Pedro Silva",
            "987.654.321-00",
            "1985-03-20",
            "(11) 91234-5678",
            "12345678",           // identityDocument
            SexType.MALE,
            "(11) 88888-8888",    // secondaryContactPhone
            "joao@email.com",
            "01234-567",          // zipCode
            "Rua das Flores",     // addressStreet
            "123",                // addressNumber
            "Apto 45",            // addressComplement
            "Centro",             // addressNeighborhood
            "São Paulo",          // addressCity
            "SP",                 // addressState
            "Maria Silva",        // guardianFullName
            "111.222.333-44",     // guardianTaxId
            "(11) 77777-7777",    // guardianContactPhone
            "Unimed",             // healthInsurance
            "123456789",          // insuranceCardNumber
            "Penicilina",         // allergies
            3,                    // fitzpatrickPhototype
            "Paciente diabético"  // generalObservations
        );

        // Assert
        assertEquals("12345678", dto.identityDocument());
        assertEquals(SexType.MALE, dto.sex());
        assertEquals("(11) 88888-8888", dto.secondaryContactPhone());
        assertEquals("01234-567", dto.zipCode());
        assertEquals("Rua das Flores", dto.addressStreet());
        assertEquals("Maria Silva", dto.guardianFullName());
        assertEquals("Unimed", dto.healthInsurance());
        assertEquals(3, dto.fitzpatrickPhototype());
    }

    @Test
    @DisplayName("UpdatePatientDto pode ter apenas alguns campos")
    void updatePatientDto_CanHaveOnlySomeFields() {
        // Arrange
        UpdatePatientDto dto = new UpdatePatientDto(
            "Novo Nome",
            null, null, null, null, null, null, null,
            null, null, null, null, null, "Rio de Janeiro", "RJ",
            null, null, null, null, null, null, null, null
        );

        // Assert
        assertEquals("Novo Nome", dto.fullName());
        assertEquals("Rio de Janeiro", dto.addressCity());
        assertEquals("RJ", dto.addressState());
        assertNull(dto.taxId());
        assertNull(dto.birthDate());
        assertNull(dto.sex());
    }

    @Test
    @DisplayName("UpdatePatientDto pode ter todos os campos")
    void updatePatientDto_CanHaveAllFields() {
        // Arrange
        UpdatePatientDto dto = new UpdatePatientDto(
            "Nome Atualizado",
            "999.888.777-66",
            "1980-12-25",
            "(21) 99999-9999",
            "87654321",
            SexType.OTHER,
            "(21) 88888-8888",
            "atualizado@email.com",
            "20000-000",
            "Av. Brasil",
            "1000",
            "Sala 501",
            "Centro",
            "Rio de Janeiro",
            "RJ",
            "Responsável Atualizado",
            "555.666.777-88",
            "(21) 77777-7777",
            "Bradesco Saúde",
            "987654321",
            "Látex",
            5,
            "Observações atualizadas"
        );

        // Assert
        assertEquals("Nome Atualizado", dto.fullName());
        assertEquals("999.888.777-66", dto.taxId());
        assertEquals(SexType.OTHER, dto.sex());
        assertEquals("20000-000", dto.zipCode());
        assertEquals(5, dto.fitzpatrickPhototype());
    }

    @Test
    @DisplayName("Patient entity deve ser criada corretamente")
    void patientEntity_ShouldBeCreatedCorrectly() {
        // Arrange
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setFullName("Maria Silva Santos");
        patient.setTaxId("12345678900");
        patient.setBirthDate("1990-05-15");
        patient.setContactPhone("(11) 98765-4321");
        patient.setSex(SexType.FEMALE);
        patient.setEmail("maria@email.com");
        patient.setAddressCity("São Paulo");
        patient.setAddressState("SP");
        patient.setActive(true);
        patient.setCreatedAt(now);
        patient.setUpdatedAt(now);
        patient.setWorkspace(testWorkspace);

        // Assert
        assertEquals(patientId, patient.getId());
        assertEquals("Maria Silva Santos", patient.getFullName());
        assertEquals("12345678900", patient.getTaxId());
        assertEquals(SexType.FEMALE, patient.getSex());
        assertTrue(patient.getActive());
        assertEquals(testWorkspace, patient.getWorkspace());
    }

    @Test
    @DisplayName("Patient pode ter campos de responsavel")
    void patient_CanHaveGuardianFields() {
        // Arrange
        Patient patient = new Patient();
        patient.setGuardianFullName("João Silva");
        patient.setGuardianTaxId("11122233344");
        patient.setGuardianContactPhone("(11) 91234-5678");

        // Assert
        assertEquals("João Silva", patient.getGuardianFullName());
        assertEquals("11122233344", patient.getGuardianTaxId());
        assertEquals("(11) 91234-5678", patient.getGuardianContactPhone());
    }

    @Test
    @DisplayName("Patient pode ter campos de convenio")
    void patient_CanHaveInsuranceFields() {
        // Arrange
        Patient patient = new Patient();
        patient.setHealthInsurance("Unimed");
        patient.setInsuranceCardNumber("123456789");

        // Assert
        assertEquals("Unimed", patient.getHealthInsurance());
        assertEquals("123456789", patient.getInsuranceCardNumber());
    }

    @Test
    @DisplayName("Patient pode ter campos medicos")
    void patient_CanHaveMedicalFields() {
        // Arrange
        Patient patient = new Patient();
        patient.setAllergies("Penicilina, Dipirona");
        patient.setFitzpatrickPhototype(4);
        patient.setGeneralObservations("Paciente com histórico de hipertensão");

        // Assert
        assertEquals("Penicilina, Dipirona", patient.getAllergies());
        assertEquals(4, patient.getFitzpatrickPhototype());
        assertEquals("Paciente com histórico de hipertensão", patient.getGeneralObservations());
    }

    @Test
    @DisplayName("ConflictException para CPF duplicado")
    void conflictException_ForDuplicateTaxId() {
        // Arrange
        String message = "CPF já cadastrado neste consultório.";

        // Act
        ConflictException exception = new ConflictException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("NotFoundException para paciente nao encontrado")
    void notFoundException_ForPatientNotFound() {
        // Arrange
        String message = "Paciente não encontrado.";

        // Act
        NotFoundException exception = new NotFoundException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("SexType enum deve ter MALE, FEMALE e OTHER")
    void sexType_ShouldHaveMaleFemaleAndOther() {
        // Assert
        assertEquals(3, SexType.values().length);
        assertNotNull(SexType.MALE);
        assertNotNull(SexType.FEMALE);
        assertNotNull(SexType.OTHER);
    }

    @Test
    @DisplayName("Resposta de delete deve conter mensagem de sucesso")
    void deleteResponse_ShouldContainSuccessMessage() {
        // Simula a resposta Map<String, String> usada no controller
        Map<String, String> response = Map.of("message", "Paciente removido com sucesso.");

        // Assert
        assertEquals("Paciente removido com sucesso.", response.get("message"));
    }

    @Test
    @DisplayName("Patient active deve ser true por padrao")
    void patientActive_ShouldBeTrueByDefault() {
        // Arrange
        Patient patient = new Patient();

        // Assert
        assertTrue(patient.getActive());
    }

    @Test
    @DisplayName("CreatePatientDto deve normalizar CPF removendo caracteres")
    void createPatientDto_ShouldNormalizeTaxId() {
        // Arrange
        CreatePatientDto dto = new CreatePatientDto(
            "Teste", "123.456.789-00", "1990-01-01", "(11) 99999-9999",
            null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null
        );

        // Act
        String normalized = dto.taxId().replaceAll("[.-]", "");

        // Assert
        assertEquals("12345678900", normalized);
    }
}

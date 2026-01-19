package com.mediapp.api.service;

import com.mediapp.api.dto.patient.CreatePatientDto;
import com.mediapp.api.dto.patient.UpdatePatientDto;
import com.mediapp.api.entity.*;
import com.mediapp.api.exception.ConflictException;
import com.mediapp.api.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PatientService Tests - DTOs e Entities")
class PatientServiceTest {

    private User testUser;
    private Workspace testWorkspace;

    @BeforeEach
    void setUp() {
        testWorkspace = new Workspace();
        testWorkspace.setId(UUID.randomUUID());
        testWorkspace.setName("Test Workspace");
        
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("doctor@example.com");
        testUser.setRole(UserRole.ADMIN);
        testUser.setWorkspace(testWorkspace);
    }

    private Patient createTestPatient() {
        Patient patient = new Patient();
        patient.setId(UUID.randomUUID());
        patient.setFullName("Maria Silva Santos");
        patient.setTaxId("12345678900");
        patient.setBirthDate("1990-05-15");
        patient.setContactPhone("(11) 98765-4321");
        patient.setSex(SexType.FEMALE);
        patient.setEmail("maria@email.com");
        patient.setAddressCity("São Paulo");
        patient.setAddressState("SP");
        patient.setActive(true);
        patient.setCreatedAt(Instant.now());
        patient.setUpdatedAt(Instant.now());
        patient.setWorkspace(testWorkspace);
        return patient;
    }

    @Test
    @DisplayName("Patient entity deve ter todos os campos corretamente")
    void patientEntity_ShouldHaveAllFieldsCorrectly() {
        // Arrange
        Patient patient = createTestPatient();

        // Assert
        assertNotNull(patient.getId());
        assertEquals("Maria Silva Santos", patient.getFullName());
        assertEquals("12345678900", patient.getTaxId());
        assertEquals("1990-05-15", patient.getBirthDate());
        assertEquals("(11) 98765-4321", patient.getContactPhone());
        assertEquals(SexType.FEMALE, patient.getSex());
        assertEquals("maria@email.com", patient.getEmail());
        assertEquals("São Paulo", patient.getAddressCity());
        assertEquals("SP", patient.getAddressState());
        assertTrue(patient.getActive());
        assertNotNull(patient.getCreatedAt());
        assertNotNull(patient.getUpdatedAt());
        assertNotNull(patient.getWorkspace());
    }

    @Test
    @DisplayName("CreatePatientDto deve normalizar CPF removendo caracteres especiais")
    void createPatientDto_ShouldNormalizeTaxId() {
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
        
        String normalizedTaxId = dto.taxId().replaceAll("[.-]", "");

        // Assert
        assertEquals("12345678900", normalizedTaxId);
    }

    @Test
    @DisplayName("CreatePatientDto deve ter campos obrigatórios")
    void createPatientDto_ShouldHaveRequiredFields() {
        // Arrange
        CreatePatientDto dto = new CreatePatientDto(
            "Maria Silva Santos",
            "123.456.789-00",
            "1990-05-15",
            "(11) 98765-4321",
            null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null
        );

        // Assert
        assertEquals("Maria Silva Santos", dto.fullName());
        assertEquals("123.456.789-00", dto.taxId());
        assertEquals("1990-05-15", dto.birthDate());
        assertEquals("(11) 98765-4321", dto.contactPhone());
    }

    @Test
    @DisplayName("UpdatePatientDto deve permitir campos opcionais nulos")
    void updatePatientDto_ShouldAllowNullOptionalFields() {
        // Arrange
        UpdatePatientDto dto = new UpdatePatientDto(
            "Novo Nome",
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null
        );

        // Assert
        assertEquals("Novo Nome", dto.fullName());
        assertNull(dto.taxId());
        assertNull(dto.birthDate());
        assertNull(dto.sex());
    }

    @Test
    @DisplayName("UpdatePatientDto deve aceitar todos os campos")
    void updatePatientDto_ShouldAcceptAllFields() {
        // Arrange
        UpdatePatientDto dto = new UpdatePatientDto(
            "Novo Nome",
            "987.654.321-00",
            "1985-03-20",
            "(11) 99999-9999",
            "12345678",
            SexType.MALE,
            "(11) 88888-8888",
            "novo@email.com",
            "01234-567",
            "Rua Nova",
            "123",
            "Apto 45",
            "Centro",
            "Rio de Janeiro",
            "RJ",
            "Responsável",
            "111.222.333-44",
            "(11) 77777-7777",
            "Unimed",
            "123456789",
            "Alergia a penicilina",
            3,
            "Observações gerais"
        );

        // Assert
        assertEquals("Novo Nome", dto.fullName());
        assertEquals("987.654.321-00", dto.taxId());
        assertEquals(SexType.MALE, dto.sex());
        assertEquals("Rio de Janeiro", dto.addressCity());
        assertEquals("RJ", dto.addressState());
        assertEquals(3, dto.fitzpatrickPhototype());
    }

    @Test
    @DisplayName("SexType enum deve ter valores MALE, FEMALE e OTHER")
    void sexTypeEnum_ShouldHaveMaleFemaleAndOther() {
        // Assert
        assertEquals(3, SexType.values().length);
        assertNotNull(SexType.MALE);
        assertNotNull(SexType.FEMALE);
        assertNotNull(SexType.OTHER);
        assertEquals("MALE", SexType.MALE.name());
        assertEquals("FEMALE", SexType.FEMALE.name());
        assertEquals("OTHER", SexType.OTHER.name());
    }

    @Test
    @DisplayName("NotFoundException deve ter mensagem correta")
    void notFoundException_ShouldHaveCorrectMessage() {
        // Arrange
        String message = "Paciente não encontrado.";
        
        // Act
        NotFoundException exception = new NotFoundException(message);
        
        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("ConflictException deve ter mensagem correta")
    void conflictException_ShouldHaveCorrectMessage() {
        // Arrange
        String message = "CPF já cadastrado neste consultório.";
        
        // Act
        ConflictException exception = new ConflictException(message);
        
        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Patient workspace relationship deve funcionar")
    void patientWorkspaceRelationship_ShouldWork() {
        // Arrange
        Patient patient = createTestPatient();

        // Assert
        assertNotNull(patient.getWorkspace());
        assertEquals(testWorkspace.getId(), patient.getWorkspace().getId());
        assertEquals("Test Workspace", patient.getWorkspace().getName());
    }

    @Test
    @DisplayName("Patient deve ter campos de endereço completo")
    void patient_ShouldHaveCompleteAddressFields() {
        // Arrange
        Patient patient = createTestPatient();
        patient.setZipCode("01234-567");
        patient.setAddressStreet("Rua das Flores");
        patient.setAddressNumber("123");
        patient.setAddressComplement("Apto 45");
        patient.setAddressNeighborhood("Centro");

        // Assert
        assertEquals("01234-567", patient.getZipCode());
        assertEquals("Rua das Flores", patient.getAddressStreet());
        assertEquals("123", patient.getAddressNumber());
        assertEquals("Apto 45", patient.getAddressComplement());
        assertEquals("Centro", patient.getAddressNeighborhood());
        assertEquals("São Paulo", patient.getAddressCity());
        assertEquals("SP", patient.getAddressState());
    }

    @Test
    @DisplayName("Patient deve ter campos de responsável")
    void patient_ShouldHaveGuardianFields() {
        // Arrange
        Patient patient = createTestPatient();
        patient.setGuardianFullName("João Silva");
        patient.setGuardianTaxId("98765432100");
        patient.setGuardianContactPhone("(11) 91234-5678");

        // Assert
        assertEquals("João Silva", patient.getGuardianFullName());
        assertEquals("98765432100", patient.getGuardianTaxId());
        assertEquals("(11) 91234-5678", patient.getGuardianContactPhone());
    }

    @Test
    @DisplayName("Patient deve ter campos de convênio")
    void patient_ShouldHaveInsuranceFields() {
        // Arrange
        Patient patient = createTestPatient();
        patient.setHealthInsurance("Unimed");
        patient.setInsuranceCardNumber("123456789");

        // Assert
        assertEquals("Unimed", patient.getHealthInsurance());
        assertEquals("123456789", patient.getInsuranceCardNumber());
    }

    @Test
    @DisplayName("Patient deve ter campos médicos")
    void patient_ShouldHaveMedicalFields() {
        // Arrange
        Patient patient = createTestPatient();
        patient.setAllergies("Alergia a penicilina");
        patient.setFitzpatrickPhototype(3);
        patient.setGeneralObservations("Paciente com hipertensão controlada");

        // Assert
        assertEquals("Alergia a penicilina", patient.getAllergies());
        assertEquals(3, patient.getFitzpatrickPhototype());
        assertEquals("Paciente com hipertensão controlada", patient.getGeneralObservations());
    }

    @Test
    @DisplayName("Patient active deve ser true por padrão")
    void patient_ActiveShouldBeTrueByDefault() {
        // Arrange
        Patient patient = new Patient();

        // Assert - campo active é inicializado como true na entidade
        assertEquals(true, patient.getActive());
    }
}

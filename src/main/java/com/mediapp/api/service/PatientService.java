package com.mediapp.api.service;

import com.mediapp.api.dto.patient.CreatePatientDto;
import com.mediapp.api.dto.patient.UpdatePatientDto;
import com.mediapp.api.entity.Patient;
import com.mediapp.api.entity.SexType;
import com.mediapp.api.entity.User;
import com.mediapp.api.entity.Workspace;
import com.mediapp.api.exception.ConflictException;
import com.mediapp.api.exception.NotFoundException;
import com.mediapp.api.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    @Autowired
    private DataSource dataSource;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public Patient create(CreatePatientDto dto, User user) {
        // Normalizar CPF removendo pontos e traços
        String normalizedTaxId = dto.taxId().replaceAll("[.-]", "");

        // Verificar se já existe paciente ativo com mesmo CPF no workspace
        if (patientRepository.existsByTaxIdAndWorkspaceIdAndActiveTrue(normalizedTaxId, user.getWorkspace().getId())) {
            throw new ConflictException("CPF já cadastrado neste consultório.");
        }

        // Sempre usar query nativa para evitar problema de enum com Hibernate
        return createWithNativeQuery(dto, user, normalizedTaxId);
    }

    private Patient createWithNativeQuery(CreatePatientDto dto, User user, String normalizedTaxId) {
        UUID patientId = UUID.randomUUID();
        Instant now = Instant.now();
        java.sql.Timestamp nowTimestamp = java.sql.Timestamp.from(now);
        UUID workspaceId = user.getWorkspace().getId();

        // Construir query SQL dinâmica baseada nos campos presentes
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO patients (id, workspace_id, full_name, tax_id, birth_date, contact_phone, is_active, created_at, updated_at");
        
        List<String> fields = new java.util.ArrayList<>();
        List<Object> values = new java.util.ArrayList<>();
        
        fields.add("id"); values.add(patientId.toString());
        fields.add("workspace_id"); values.add(workspaceId.toString());
        fields.add("full_name"); values.add(dto.fullName());
        fields.add("tax_id"); values.add(normalizedTaxId);
        fields.add("birth_date"); values.add(dto.birthDate());
        fields.add("contact_phone"); values.add(dto.contactPhone());
        fields.add("is_active"); values.add(true);
        fields.add("created_at"); values.add(nowTimestamp);
        fields.add("updated_at"); values.add(nowTimestamp);

        // Adicionar sex apenas se presente
        if (dto.sex() != null) {
            sqlBuilder.append(", sex");
            fields.add("sex");
            values.add(dto.sex().name());
        }

        if (dto.identityDocument() != null) {
            sqlBuilder.append(", identity_document");
            fields.add("identity_document");
            values.add(dto.identityDocument());
        }
        if (dto.secondaryContactPhone() != null) {
            sqlBuilder.append(", secondary_contact_phone");
            fields.add("secondary_contact_phone");
            values.add(dto.secondaryContactPhone());
        }
        if (dto.email() != null) {
            sqlBuilder.append(", email");
            fields.add("email");
            values.add(dto.email());
        }
        if (dto.zipCode() != null) {
            sqlBuilder.append(", zip_code");
            fields.add("zip_code");
            values.add(dto.zipCode());
        }
        if (dto.addressStreet() != null) {
            sqlBuilder.append(", address_street");
            fields.add("address_street");
            values.add(dto.addressStreet());
        }
        if (dto.addressNumber() != null) {
            sqlBuilder.append(", address_number");
            fields.add("address_number");
            values.add(dto.addressNumber());
        }
        if (dto.addressComplement() != null) {
            sqlBuilder.append(", address_complement");
            fields.add("address_complement");
            values.add(dto.addressComplement());
        }
        if (dto.addressNeighborhood() != null) {
            sqlBuilder.append(", address_neighborhood");
            fields.add("address_neighborhood");
            values.add(dto.addressNeighborhood());
        }
        if (dto.addressCity() != null) {
            sqlBuilder.append(", address_city");
            fields.add("address_city");
            values.add(dto.addressCity());
        }
        if (dto.addressState() != null) {
            sqlBuilder.append(", address_state");
            fields.add("address_state");
            values.add(dto.addressState());
        }
        if (dto.guardianFullName() != null) {
            sqlBuilder.append(", guardian_full_name");
            fields.add("guardian_full_name");
            values.add(dto.guardianFullName());
        }
        if (dto.guardianTaxId() != null) {
            sqlBuilder.append(", guardian_tax_id");
            fields.add("guardian_tax_id");
            values.add(dto.guardianTaxId());
        }
        if (dto.guardianContactPhone() != null) {
            sqlBuilder.append(", guardian_contact_phone");
            fields.add("guardian_contact_phone");
            values.add(dto.guardianContactPhone());
        }
        if (dto.healthInsurance() != null) {
            sqlBuilder.append(", health_insurance");
            fields.add("health_insurance");
            values.add(dto.healthInsurance());
        }
        if (dto.insuranceCardNumber() != null) {
            sqlBuilder.append(", insurance_card_number");
            fields.add("insurance_card_number");
            values.add(dto.insuranceCardNumber());
        }
        if (dto.allergies() != null) {
            sqlBuilder.append(", allergies");
            fields.add("allergies");
            values.add(dto.allergies());
        }
        if (dto.fitzpatrickPhototype() != null) {
            sqlBuilder.append(", fitzpatrick_phototype");
            fields.add("fitzpatrick_phototype");
            values.add(dto.fitzpatrickPhototype());
        }
        if (dto.generalObservations() != null) {
            sqlBuilder.append(", general_observations");
            fields.add("general_observations");
            values.add(dto.generalObservations());
        }

        sqlBuilder.append(") VALUES (");
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) sqlBuilder.append(", ");
            String fieldName = fields.get(i);
            if ("id".equals(fieldName) || "workspace_id".equals(fieldName)) {
                sqlBuilder.append("CAST(? AS uuid)");
            } else if ("sex".equals(fieldName)) {
                sqlBuilder.append("CAST(? AS patients_sex_enum)");
            } else {
                sqlBuilder.append("?");
            }
        }
        sqlBuilder.append(")");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            int paramIndex = 1;
            for (Object value : values) {
                String fieldName = fields.get(paramIndex - 1);
                if ("id".equals(fieldName) || "workspace_id".equals(fieldName)) {
                    stmt.setString(paramIndex, value.toString());
                } else if ("sex".equals(fieldName)) {
                    stmt.setString(paramIndex, value.toString());
                } else if ("created_at".equals(fieldName) || "updated_at".equals(fieldName)) {
                    stmt.setTimestamp(paramIndex, (java.sql.Timestamp) value);
                } else if ("is_active".equals(fieldName)) {
                    stmt.setBoolean(paramIndex, (Boolean) value);
                } else if ("fitzpatrick_phototype".equals(fieldName)) {
                    stmt.setInt(paramIndex, (Integer) value);
                } else {
                    stmt.setString(paramIndex, value != null ? value.toString() : null);
                }
                paramIndex++;
            }
            
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao criar paciente: " + e.getMessage(), e);
        }

        // Buscar o paciente recém-criado
        return findPatientById(patientId);
    }
    
    private Patient findPatientById(UUID id) {
        String sql = "SELECT p.id, p.workspace_id, p.full_name, p.tax_id, p.identity_document, p.birth_date, " +
            "p.sex, p.contact_phone, p.secondary_contact_phone, p.email, p.zip_code, p.address_street, p.address_number, " +
            "p.address_complement, p.address_neighborhood, p.address_city, p.address_state, p.guardian_full_name, " +
            "p.guardian_tax_id, p.guardian_contact_phone, p.health_insurance, p.insurance_card_number, p.allergies, " +
            "p.fitzpatrick_phototype, p.general_observations, p.is_active, p.created_at, p.updated_at, " +
            "w.id as workspace_id_col, w.name as workspace_name " +
            "FROM patients p LEFT JOIN workspaces w ON p.workspace_id = w.id WHERE p.id = CAST(? AS uuid)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao buscar paciente: " + e.getMessage(), e);
        }
        
        throw new NotFoundException("Paciente não encontrado.");
    }

    public List<Patient> findAll(User user) {
        // Usar query nativa para evitar problemas de serialização com proxy Hibernate
        String sql = "SELECT p.id, p.workspace_id, p.full_name, p.tax_id, p.identity_document, p.birth_date, " +
            "p.sex, p.contact_phone, p.secondary_contact_phone, p.email, p.zip_code, p.address_street, p.address_number, " +
            "p.address_complement, p.address_neighborhood, p.address_city, p.address_state, p.guardian_full_name, " +
            "p.guardian_tax_id, p.guardian_contact_phone, p.health_insurance, p.insurance_card_number, p.allergies, " +
            "p.fitzpatrick_phototype, p.general_observations, p.is_active, p.created_at, p.updated_at, " +
            "w.id as workspace_id_col, w.name as workspace_name " +
            "FROM patients p LEFT JOIN workspaces w ON p.workspace_id = w.id " +
            "WHERE p.workspace_id = CAST(? AS uuid) AND p.is_active = true ORDER BY p.full_name ASC";
        
        List<Patient> patients = new java.util.ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getWorkspace().getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao listar pacientes: " + e.getMessage(), e);
        }
        
        return patients;
    }

    public Patient findOne(UUID id, User user) {
        // Usar query nativa para evitar problemas de serialização com proxy Hibernate
        String sql = "SELECT p.id, p.workspace_id, p.full_name, p.tax_id, p.identity_document, p.birth_date, " +
            "p.sex, p.contact_phone, p.secondary_contact_phone, p.email, p.zip_code, p.address_street, p.address_number, " +
            "p.address_complement, p.address_neighborhood, p.address_city, p.address_state, p.guardian_full_name, " +
            "p.guardian_tax_id, p.guardian_contact_phone, p.health_insurance, p.insurance_card_number, p.allergies, " +
            "p.fitzpatrick_phototype, p.general_observations, p.is_active, p.created_at, p.updated_at, " +
            "w.id as workspace_id_col, w.name as workspace_name " +
            "FROM patients p LEFT JOIN workspaces w ON p.workspace_id = w.id " +
            "WHERE p.id = CAST(? AS uuid) AND p.workspace_id = CAST(? AS uuid) AND p.is_active = true";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            stmt.setString(2, user.getWorkspace().getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao buscar paciente: " + e.getMessage(), e);
        }
        
        throw new NotFoundException("Paciente não encontrado.");
    }
    
    private Patient mapResultSetToPatient(ResultSet rs) throws java.sql.SQLException {
        Patient patient = new Patient();
        patient.setId(UUID.fromString(rs.getString("id")));
        patient.setFullName(rs.getString("full_name"));
        patient.setTaxId(rs.getString("tax_id"));
        patient.setIdentityDocument(rs.getString("identity_document"));
        patient.setBirthDate(rs.getString("birth_date"));
        String sexStr = rs.getString("sex");
        if (sexStr != null) {
            patient.setSex(SexType.valueOf(sexStr));
        }
        patient.setContactPhone(rs.getString("contact_phone"));
        patient.setSecondaryContactPhone(rs.getString("secondary_contact_phone"));
        patient.setEmail(rs.getString("email"));
        patient.setZipCode(rs.getString("zip_code"));
        patient.setAddressStreet(rs.getString("address_street"));
        patient.setAddressNumber(rs.getString("address_number"));
        patient.setAddressComplement(rs.getString("address_complement"));
        patient.setAddressNeighborhood(rs.getString("address_neighborhood"));
        patient.setAddressCity(rs.getString("address_city"));
        patient.setAddressState(rs.getString("address_state"));
        patient.setGuardianFullName(rs.getString("guardian_full_name"));
        patient.setGuardianTaxId(rs.getString("guardian_tax_id"));
        patient.setGuardianContactPhone(rs.getString("guardian_contact_phone"));
        patient.setHealthInsurance(rs.getString("health_insurance"));
        patient.setInsuranceCardNumber(rs.getString("insurance_card_number"));
        patient.setAllergies(rs.getString("allergies"));
        Integer fitzpatrick = rs.getObject("fitzpatrick_phototype", Integer.class);
        patient.setFitzpatrickPhototype(fitzpatrick);
        patient.setGeneralObservations(rs.getString("general_observations"));
        patient.setActive(rs.getBoolean("is_active"));
        patient.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        patient.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        
        // Carregar workspace
        String workspaceIdStr = rs.getString("workspace_id_col");
        if (workspaceIdStr != null) {
            Workspace workspace = new Workspace();
            workspace.setId(UUID.fromString(workspaceIdStr));
            workspace.setName(rs.getString("workspace_name"));
            patient.setWorkspace(workspace);
        }
        
        return patient;
    }

    @Transactional
    public Patient update(UUID id, UpdatePatientDto dto, User user) {
        // Verificar existência e ownership
        findOne(id, user);

        // Se o CPF for alterado, verificar conflito
        if (dto.taxId() != null) {
            String normalizedTaxId = dto.taxId().replaceAll("[.-]", "");
            
            patientRepository.findByTaxIdAndWorkspaceIdAndActiveTrue(normalizedTaxId, user.getWorkspace().getId())
                .ifPresent(conflictingPatient -> {
                    if (!conflictingPatient.getId().equals(id)) {
                        throw new ConflictException("CPF já cadastrado neste consultório.");
                    }
                });
        }

        // Sempre usar query nativa para evitar problemas com enum PostgreSQL
        return updateWithNativeQuery(id, dto);
    }

    private Patient updateWithNativeQuery(UUID id, UpdatePatientDto dto) {
        Instant now = Instant.now();
        java.sql.Timestamp nowTimestamp = java.sql.Timestamp.from(now);

        // Construir query SQL dinâmica baseada nos campos presentes
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("UPDATE patients SET updated_at = ?");
        
        List<String> fields = new java.util.ArrayList<>();
        List<Object> values = new java.util.ArrayList<>();
        values.add(nowTimestamp);

        if (dto.fullName() != null) {
            sqlBuilder.append(", full_name = ?");
            fields.add("full_name");
            values.add(dto.fullName());
        }
        if (dto.taxId() != null) {
            String normalizedTaxId = dto.taxId().replaceAll("[.-]", "");
            sqlBuilder.append(", tax_id = ?");
            fields.add("tax_id");
            values.add(normalizedTaxId);
        }
        if (dto.birthDate() != null) {
            sqlBuilder.append(", birth_date = ?");
            fields.add("birth_date");
            values.add(dto.birthDate());
        }
        if (dto.contactPhone() != null) {
            sqlBuilder.append(", contact_phone = ?");
            fields.add("contact_phone");
            values.add(dto.contactPhone());
        }
        if (dto.identityDocument() != null) {
            sqlBuilder.append(", identity_document = ?");
            fields.add("identity_document");
            values.add(dto.identityDocument());
        }
        if (dto.sex() != null) {
            sqlBuilder.append(", sex = CAST(? AS patients_sex_enum)");
            fields.add("sex");
            values.add(dto.sex().name());
        }
        if (dto.secondaryContactPhone() != null) {
            sqlBuilder.append(", secondary_contact_phone = ?");
            fields.add("secondary_contact_phone");
            values.add(dto.secondaryContactPhone());
        }
        if (dto.email() != null) {
            sqlBuilder.append(", email = ?");
            fields.add("email");
            values.add(dto.email());
        }
        if (dto.zipCode() != null) {
            sqlBuilder.append(", zip_code = ?");
            fields.add("zip_code");
            values.add(dto.zipCode());
        }
        if (dto.addressStreet() != null) {
            sqlBuilder.append(", address_street = ?");
            fields.add("address_street");
            values.add(dto.addressStreet());
        }
        if (dto.addressNumber() != null) {
            sqlBuilder.append(", address_number = ?");
            fields.add("address_number");
            values.add(dto.addressNumber());
        }
        if (dto.addressComplement() != null) {
            sqlBuilder.append(", address_complement = ?");
            fields.add("address_complement");
            values.add(dto.addressComplement());
        }
        if (dto.addressNeighborhood() != null) {
            sqlBuilder.append(", address_neighborhood = ?");
            fields.add("address_neighborhood");
            values.add(dto.addressNeighborhood());
        }
        if (dto.addressCity() != null) {
            sqlBuilder.append(", address_city = ?");
            fields.add("address_city");
            values.add(dto.addressCity());
        }
        if (dto.addressState() != null) {
            sqlBuilder.append(", address_state = ?");
            fields.add("address_state");
            values.add(dto.addressState());
        }
        if (dto.guardianFullName() != null) {
            sqlBuilder.append(", guardian_full_name = ?");
            fields.add("guardian_full_name");
            values.add(dto.guardianFullName());
        }
        if (dto.guardianTaxId() != null) {
            sqlBuilder.append(", guardian_tax_id = ?");
            fields.add("guardian_tax_id");
            values.add(dto.guardianTaxId());
        }
        if (dto.guardianContactPhone() != null) {
            sqlBuilder.append(", guardian_contact_phone = ?");
            fields.add("guardian_contact_phone");
            values.add(dto.guardianContactPhone());
        }
        if (dto.healthInsurance() != null) {
            sqlBuilder.append(", health_insurance = ?");
            fields.add("health_insurance");
            values.add(dto.healthInsurance());
        }
        if (dto.insuranceCardNumber() != null) {
            sqlBuilder.append(", insurance_card_number = ?");
            fields.add("insurance_card_number");
            values.add(dto.insuranceCardNumber());
        }
        if (dto.allergies() != null) {
            sqlBuilder.append(", allergies = ?");
            fields.add("allergies");
            values.add(dto.allergies());
        }
        if (dto.fitzpatrickPhototype() != null) {
            sqlBuilder.append(", fitzpatrick_phototype = ?");
            fields.add("fitzpatrick_phototype");
            values.add(dto.fitzpatrickPhototype());
        }
        if (dto.generalObservations() != null) {
            sqlBuilder.append(", general_observations = ?");
            fields.add("general_observations");
            values.add(dto.generalObservations());
        }

        sqlBuilder.append(" WHERE id = CAST(? AS uuid)");
        values.add(id.toString());

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            int paramIndex = 1;
            for (Object value : values) {
                if (paramIndex == 1) {
                    // updated_at
                    stmt.setTimestamp(paramIndex, (java.sql.Timestamp) value);
                } else if (paramIndex == values.size()) {
                    // id (último parâmetro)
                    stmt.setString(paramIndex, value.toString());
                } else {
                    String fieldName = fields.get(paramIndex - 2); // -2 porque pulamos updated_at e id
                    if ("sex".equals(fieldName)) {
                        stmt.setString(paramIndex, value.toString());
                    } else if ("fitzpatrick_phototype".equals(fieldName)) {
                        stmt.setInt(paramIndex, (Integer) value);
                    } else {
                        stmt.setString(paramIndex, value != null ? value.toString() : null);
                    }
                }
                paramIndex++;
            }
            
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao atualizar paciente: " + e.getMessage(), e);
        }

        // Buscar o paciente atualizado
        return findPatientById(id);
    }

    @Transactional
    public void remove(UUID id, User user) {
        // Verificar existência e ownership
        findOne(id, user);
        
        // Soft delete usando query nativa para evitar problemas com enum PostgreSQL
        String sql = "UPDATE patients SET is_active = false, updated_at = ? WHERE id = CAST(? AS uuid)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, java.sql.Timestamp.from(Instant.now()));
            stmt.setString(2, id.toString());
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao remover paciente: " + e.getMessage(), e);
        }
    }
}


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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

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
        String normalizedTaxId = dto.taxId().replaceAll("[.-]", "");

        if (patientRepository.existsByTaxIdAndWorkspaceIdAndActiveTrue(normalizedTaxId, user.getWorkspace().getId())) {
            throw new ConflictException("CPF já cadastrado neste consultório.");
        }

        return createWithNativeQuery(dto, user, normalizedTaxId);
    }

    private Patient createWithNativeQuery(CreatePatientDto dto, User user, String normalizedTaxId) {
        UUID patientId = UUID.randomUUID();
        Instant now = Instant.now();
        java.sql.Timestamp nowTimestamp = java.sql.Timestamp.from(now);
        UUID workspaceId = user.getWorkspace().getId();

        // Campos obrigatórios
        List<FieldMapping> fields = new ArrayList<>();
        fields.add(new FieldMapping("id", patientId.toString(), FieldType.UUID));
        fields.add(new FieldMapping("workspace_id", workspaceId.toString(), FieldType.UUID));
        fields.add(new FieldMapping("full_name", dto.fullName(), FieldType.STRING));
        fields.add(new FieldMapping("tax_id", normalizedTaxId, FieldType.STRING));
        fields.add(new FieldMapping("birth_date", dto.birthDate(), FieldType.STRING));
        fields.add(new FieldMapping("contact_phone", dto.contactPhone(), FieldType.STRING));
        fields.add(new FieldMapping("is_active", true, FieldType.BOOLEAN));
        fields.add(new FieldMapping("created_at", nowTimestamp, FieldType.TIMESTAMP));
        fields.add(new FieldMapping("updated_at", nowTimestamp, FieldType.TIMESTAMP));

        // Campos opcionais
        addOptionalField(fields, "sex", dto.sex(), () -> dto.sex().name(), FieldType.ENUM);
        addOptionalField(fields, "identity_document", dto.identityDocument(), FieldType.STRING);
        addOptionalField(fields, "secondary_contact_phone", dto.secondaryContactPhone(), FieldType.STRING);
        addOptionalField(fields, "email", dto.email(), FieldType.STRING);
        addOptionalField(fields, "zip_code", dto.zipCode(), FieldType.STRING);
        addOptionalField(fields, "address_street", dto.addressStreet(), FieldType.STRING);
        addOptionalField(fields, "address_number", dto.addressNumber(), FieldType.STRING);
        addOptionalField(fields, "address_complement", dto.addressComplement(), FieldType.STRING);
        addOptionalField(fields, "address_neighborhood", dto.addressNeighborhood(), FieldType.STRING);
        addOptionalField(fields, "address_city", dto.addressCity(), FieldType.STRING);
        addOptionalField(fields, "address_state", dto.addressState(), FieldType.STRING);
        addOptionalField(fields, "guardian_full_name", dto.guardianFullName(), FieldType.STRING);
        addOptionalField(fields, "guardian_tax_id", dto.guardianTaxId(), FieldType.STRING);
        addOptionalField(fields, "guardian_contact_phone", dto.guardianContactPhone(), FieldType.STRING);
        addOptionalField(fields, "health_insurance", dto.healthInsurance(), FieldType.STRING);
        addOptionalField(fields, "insurance_card_number", dto.insuranceCardNumber(), FieldType.STRING);
        addOptionalField(fields, "allergies", dto.allergies(), FieldType.STRING);
        addOptionalField(fields, "fitzpatrick_phototype", dto.fitzpatrickPhototype(), FieldType.INTEGER);
        addOptionalField(fields, "general_observations", dto.generalObservations(), FieldType.STRING);

        String sql = buildInsertQuery(fields);
        executeInsert(sql, fields);

        return findPatientById(patientId);
    }

    private void addOptionalField(List<FieldMapping> fields, String fieldName, Object value, FieldType type) {
        if (value != null) {
            fields.add(new FieldMapping(fieldName, value, type));
        }
    }

    private <T> void addOptionalField(List<FieldMapping> fields, String fieldName, T value, Supplier<Object> valueMapper, FieldType type) {
        if (value != null) {
            fields.add(new FieldMapping(fieldName, valueMapper.get(), type));
        }
    }

    private String buildInsertQuery(List<FieldMapping> fields) {
        StringBuilder sql = new StringBuilder("INSERT INTO patients (");
        
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append(fields.get(i).getColumnName());
        }
        
        sql.append(") VALUES (");
        
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) sql.append(", ");
            FieldMapping field = fields.get(i);
            sql.append(field.getPlaceholder());
        }
        
        sql.append(")");
        return sql.toString();
    }

    private void executeInsert(String sql, List<FieldMapping> fields) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < fields.size(); i++) {
                fields.get(i).setParameter(stmt, i + 1);
            }
            
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao criar paciente: " + e.getMessage(), e);
        }
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
        String sql = "SELECT p.id, p.workspace_id, p.full_name, p.tax_id, p.identity_document, p.birth_date, " +
            "p.sex, p.contact_phone, p.secondary_contact_phone, p.email, p.zip_code, p.address_street, p.address_number, " +
            "p.address_complement, p.address_neighborhood, p.address_city, p.address_state, p.guardian_full_name, " +
            "p.guardian_tax_id, p.guardian_contact_phone, p.health_insurance, p.insurance_card_number, p.allergies, " +
            "p.fitzpatrick_phototype, p.general_observations, p.is_active, p.created_at, p.updated_at, " +
            "w.id as workspace_id_col, w.name as workspace_name " +
            "FROM patients p LEFT JOIN workspaces w ON p.workspace_id = w.id " +
            "WHERE p.workspace_id = CAST(? AS uuid) AND p.is_active = true ORDER BY p.full_name ASC";
        
        List<Patient> patients = new ArrayList<>();
        
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
        
        // Mapear campos string usando Map
        java.util.Map<String, java.util.function.Consumer<String>> stringFields = new java.util.HashMap<>();
        stringFields.put("id", value -> patient.setId(UUID.fromString(value)));
        stringFields.put("full_name", patient::setFullName);
        stringFields.put("tax_id", patient::setTaxId);
        stringFields.put("identity_document", patient::setIdentityDocument);
        stringFields.put("birth_date", patient::setBirthDate);
        stringFields.put("contact_phone", patient::setContactPhone);
        stringFields.put("secondary_contact_phone", patient::setSecondaryContactPhone);
        stringFields.put("email", patient::setEmail);
        stringFields.put("zip_code", patient::setZipCode);
        stringFields.put("address_street", patient::setAddressStreet);
        stringFields.put("address_number", patient::setAddressNumber);
        stringFields.put("address_complement", patient::setAddressComplement);
        stringFields.put("address_neighborhood", patient::setAddressNeighborhood);
        stringFields.put("address_city", patient::setAddressCity);
        stringFields.put("address_state", patient::setAddressState);
        stringFields.put("guardian_full_name", patient::setGuardianFullName);
        stringFields.put("guardian_tax_id", patient::setGuardianTaxId);
        stringFields.put("guardian_contact_phone", patient::setGuardianContactPhone);
        stringFields.put("health_insurance", patient::setHealthInsurance);
        stringFields.put("insurance_card_number", patient::setInsuranceCardNumber);
        stringFields.put("allergies", patient::setAllergies);
        stringFields.put("general_observations", patient::setGeneralObservations);
        
        stringFields.forEach((column, setter) -> {
            try {
                String value = rs.getString(column);
                if (value != null) {
                    setter.accept(value);
                }
            } catch (java.sql.SQLException e) {
                throw new RuntimeException("Erro ao mapear campo " + column, e);
            }
        });
        
        // Campos especiais
        String sexStr = rs.getString("sex");
        if (sexStr != null) {
            patient.setSex(SexType.valueOf(sexStr));
        }
        
        Integer fitzpatrick = rs.getObject("fitzpatrick_phototype", Integer.class);
        patient.setFitzpatrickPhototype(fitzpatrick);
        
        patient.setActive(rs.getBoolean("is_active"));
        
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            patient.setCreatedAt(createdAt.toInstant());
        }
        
        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            patient.setUpdatedAt(updatedAt.toInstant());
        }
        
        // Workspace
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
        findOne(id, user);

        if (dto.taxId() != null) {
            String normalizedTaxId = dto.taxId().replaceAll("[.-]", "");
            
            patientRepository.findByTaxIdAndWorkspaceIdAndActiveTrue(normalizedTaxId, user.getWorkspace().getId())
                .ifPresent(conflictingPatient -> {
                    if (!conflictingPatient.getId().equals(id)) {
                        throw new ConflictException("CPF já cadastrado neste consultório.");
                    }
                });
        }

        return updateWithNativeQuery(id, dto);
    }

    private Patient updateWithNativeQuery(UUID id, UpdatePatientDto dto) {
        Instant now = Instant.now();
        java.sql.Timestamp nowTimestamp = java.sql.Timestamp.from(now);

        List<FieldMapping> fields = new ArrayList<>();
        fields.add(new FieldMapping("updated_at", nowTimestamp, FieldType.TIMESTAMP));

        // Campos opcionais para atualização
        addOptionalField(fields, "full_name", dto.fullName(), FieldType.STRING);
        if (dto.taxId() != null) {
            String normalizedTaxId = dto.taxId().replaceAll("[.-]", "");
            addOptionalField(fields, "tax_id", normalizedTaxId, FieldType.STRING);
        }
        addOptionalField(fields, "birth_date", dto.birthDate(), FieldType.STRING);
        addOptionalField(fields, "contact_phone", dto.contactPhone(), FieldType.STRING);
        addOptionalField(fields, "identity_document", dto.identityDocument(), FieldType.STRING);
        addOptionalField(fields, "sex", dto.sex(), () -> dto.sex().name(), FieldType.ENUM);
        addOptionalField(fields, "secondary_contact_phone", dto.secondaryContactPhone(), FieldType.STRING);
        addOptionalField(fields, "email", dto.email(), FieldType.STRING);
        addOptionalField(fields, "zip_code", dto.zipCode(), FieldType.STRING);
        addOptionalField(fields, "address_street", dto.addressStreet(), FieldType.STRING);
        addOptionalField(fields, "address_number", dto.addressNumber(), FieldType.STRING);
        addOptionalField(fields, "address_complement", dto.addressComplement(), FieldType.STRING);
        addOptionalField(fields, "address_neighborhood", dto.addressNeighborhood(), FieldType.STRING);
        addOptionalField(fields, "address_city", dto.addressCity(), FieldType.STRING);
        addOptionalField(fields, "address_state", dto.addressState(), FieldType.STRING);
        addOptionalField(fields, "guardian_full_name", dto.guardianFullName(), FieldType.STRING);
        addOptionalField(fields, "guardian_tax_id", dto.guardianTaxId(), FieldType.STRING);
        addOptionalField(fields, "guardian_contact_phone", dto.guardianContactPhone(), FieldType.STRING);
        addOptionalField(fields, "health_insurance", dto.healthInsurance(), FieldType.STRING);
        addOptionalField(fields, "insurance_card_number", dto.insuranceCardNumber(), FieldType.STRING);
        addOptionalField(fields, "allergies", dto.allergies(), FieldType.STRING);
        addOptionalField(fields, "fitzpatrick_phototype", dto.fitzpatrickPhototype(), FieldType.INTEGER);
        addOptionalField(fields, "general_observations", dto.generalObservations(), FieldType.STRING);

        String sql = buildUpdateQuery(fields, id);
        executeUpdate(sql, fields, id);

        return findPatientById(id);
    }

    private String buildUpdateQuery(List<FieldMapping> fields, UUID id) {
        StringBuilder sql = new StringBuilder("UPDATE patients SET ");
        
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) sql.append(", ");
            FieldMapping field = fields.get(i);
            sql.append(field.getColumnName()).append(" = ").append(field.getPlaceholder());
        }
        
        sql.append(" WHERE id = CAST(? AS uuid)");
        return sql.toString();
    }

    private void executeUpdate(String sql, List<FieldMapping> fields, UUID id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            for (FieldMapping field : fields) {
                field.setParameter(stmt, paramIndex++);
            }
            
            stmt.setString(paramIndex, id.toString());
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Erro ao atualizar paciente: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void remove(UUID id, User user) {
        findOne(id, user);
        
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

    // Classe auxiliar para mapear campos e valores
    private static class FieldMapping {
        private final String columnName;
        private final Object value;
        private final FieldType type;

        public FieldMapping(String columnName, Object value, FieldType type) {
            this.columnName = columnName;
            this.value = value;
            this.type = type;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getPlaceholder() {
            return switch (type) {
                case UUID -> "CAST(? AS uuid)";
                case ENUM -> "CAST(? AS patients_sex_enum)";
                default -> "?";
            };
        }

        public void setParameter(PreparedStatement stmt, int index) throws java.sql.SQLException {
            switch (type) {
                case UUID -> stmt.setString(index, value.toString());
                case ENUM -> stmt.setString(index, value.toString());
                case TIMESTAMP -> stmt.setTimestamp(index, (java.sql.Timestamp) value);
                case BOOLEAN -> stmt.setBoolean(index, (Boolean) value);
                case INTEGER -> stmt.setInt(index, (Integer) value);
                case STRING -> stmt.setString(index, value != null ? value.toString() : null);
            }
        }
    }

    private enum FieldType {
        UUID, ENUM, TIMESTAMP, BOOLEAN, INTEGER, STRING
    }
}

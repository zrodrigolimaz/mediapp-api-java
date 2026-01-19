package com.mediapp.api.entity;

import com.mediapp.api.converter.SexTypeConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade JPA mapeada para a tabela patients.
 * Um Patient pertence a um Workspace (ManyToOne).
 */
@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "tax_id", nullable = false, length = 14)
    private String taxId;

    @Column(name = "identity_document", length = 20)
    private String identityDocument;

    @Column(name = "birth_date", nullable = false, length = 10)
    private String birthDate;

    @Convert(converter = SexTypeConverter.class)
    @Column(name = "sex", columnDefinition = "patients_sex_enum")
    private SexType sex;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "secondary_contact_phone", length = 20)
    private String secondaryContactPhone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "address_street", length = 255)
    private String addressStreet;

    @Column(name = "address_number", length = 10)
    private String addressNumber;

    @Column(name = "address_complement", length = 100)
    private String addressComplement;

    @Column(name = "address_neighborhood", length = 100)
    private String addressNeighborhood;

    @Column(name = "address_city", length = 100)
    private String addressCity;

    @Column(name = "address_state", length = 2)
    private String addressState;

    @Column(name = "guardian_full_name", length = 255)
    private String guardianFullName;

    @Column(name = "guardian_tax_id", length = 14)
    private String guardianTaxId;

    @Column(name = "guardian_contact_phone", length = 20)
    private String guardianContactPhone;

    @Column(name = "health_insurance", length = 100)
    private String healthInsurance;

    @Column(name = "insurance_card_number", length = 50)
    private String insuranceCardNumber;

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "fitzpatrick_phototype")
    private Integer fitzpatrickPhototype;

    @Column(name = "general_observations", columnDefinition = "TEXT")
    private String generalObservations;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getIdentityDocument() { return identityDocument; }
    public void setIdentityDocument(String identityDocument) { this.identityDocument = identityDocument; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public SexType getSex() { return sex; }
    public void setSex(SexType sex) { this.sex = sex; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getSecondaryContactPhone() { return secondaryContactPhone; }
    public void setSecondaryContactPhone(String secondaryContactPhone) { this.secondaryContactPhone = secondaryContactPhone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getAddressStreet() { return addressStreet; }
    public void setAddressStreet(String addressStreet) { this.addressStreet = addressStreet; }
    public String getAddressNumber() { return addressNumber; }
    public void setAddressNumber(String addressNumber) { this.addressNumber = addressNumber; }
    public String getAddressComplement() { return addressComplement; }
    public void setAddressComplement(String addressComplement) { this.addressComplement = addressComplement; }
    public String getAddressNeighborhood() { return addressNeighborhood; }
    public void setAddressNeighborhood(String addressNeighborhood) { this.addressNeighborhood = addressNeighborhood; }
    public String getAddressCity() { return addressCity; }
    public void setAddressCity(String addressCity) { this.addressCity = addressCity; }
    public String getAddressState() { return addressState; }
    public void setAddressState(String addressState) { this.addressState = addressState; }
    public String getGuardianFullName() { return guardianFullName; }
    public void setGuardianFullName(String guardianFullName) { this.guardianFullName = guardianFullName; }
    public String getGuardianTaxId() { return guardianTaxId; }
    public void setGuardianTaxId(String guardianTaxId) { this.guardianTaxId = guardianTaxId; }
    public String getGuardianContactPhone() { return guardianContactPhone; }
    public void setGuardianContactPhone(String guardianContactPhone) { this.guardianContactPhone = guardianContactPhone; }
    public String getHealthInsurance() { return healthInsurance; }
    public void setHealthInsurance(String healthInsurance) { this.healthInsurance = healthInsurance; }
    public String getInsuranceCardNumber() { return insuranceCardNumber; }
    public void setInsuranceCardNumber(String insuranceCardNumber) { this.insuranceCardNumber = insuranceCardNumber; }
    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public Integer getFitzpatrickPhototype() { return fitzpatrickPhototype; }
    public void setFitzpatrickPhototype(Integer fitzpatrickPhototype) { this.fitzpatrickPhototype = fitzpatrickPhototype; }
    public String getGeneralObservations() { return generalObservations; }
    public void setGeneralObservations(String generalObservations) { this.generalObservations = generalObservations; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}


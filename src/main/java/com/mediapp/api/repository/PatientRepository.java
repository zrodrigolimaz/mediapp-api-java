package com.mediapp.api.repository;

import com.mediapp.api.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    List<Patient> findByWorkspaceIdAndActiveTrueOrderByFullNameAsc(UUID workspaceId);

    Optional<Patient> findByIdAndWorkspaceIdAndActiveTrue(UUID id, UUID workspaceId);

    boolean existsByTaxIdAndWorkspaceIdAndActiveTrue(String taxId, UUID workspaceId);

    Optional<Patient> findByTaxIdAndWorkspaceIdAndActiveTrue(String taxId, UUID workspaceId);
}


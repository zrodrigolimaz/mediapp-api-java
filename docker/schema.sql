-- =================================================================================
-- MediApp - Schema do Banco de Dados PostgreSQL
-- =================================================================================

-- Habilita extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "btree_gist";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- =================================================================================
-- TIPOS ENUM PERSONALIZADOS
-- =================================================================================
CREATE TYPE user_role AS ENUM ('ADMIN', 'MEMBER');
CREATE TYPE document_type AS ENUM ('CPF', 'CNPJ');
CREATE TYPE appointment_status AS ENUM ('SCHEDULED', 'CONFIRMED', 'ARRIVED', 'WAITING', 'IN_PROGRESS', 'COMPLETED', 'CANCELED', 'NO_SHOW');
CREATE TYPE sex_type AS ENUM ('MALE', 'FEMALE', 'OTHER');

-- Alias para compatibilidade com a API Java
CREATE TYPE patients_sex_enum AS ENUM ('MALE', 'FEMALE', 'OTHER');

-- =================================================================================
-- ESTRUTURA MULTI-TENANT
-- =================================================================================
CREATE TABLE workspaces (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    document_type document_type NOT NULL,
    document_number VARCHAR(18) NOT NULL,
    owner_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (document_type, document_number)
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    crm VARCHAR(20) UNIQUE,
    role user_role NOT NULL DEFAULT 'MEMBER',
    digital_signature_url TEXT,
    password_reset_token VARCHAR(255),
    password_reset_expires TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Chave estrangeira após criação da tabela users
ALTER TABLE workspaces ADD CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE RESTRICT;

-- =================================================================================
-- ENTIDADES DE NEGÓCIO
-- =================================================================================
CREATE TABLE patients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    tax_id VARCHAR(14) NOT NULL,
    identity_document VARCHAR(20),
    birth_date VARCHAR(10) NOT NULL,
    sex patients_sex_enum,
    contact_phone VARCHAR(20) NOT NULL,
    secondary_contact_phone VARCHAR(20),
    email VARCHAR(255),
    zip_code VARCHAR(10),
    address_street VARCHAR(255),
    address_number VARCHAR(10),
    address_complement VARCHAR(100),
    address_neighborhood VARCHAR(100),
    address_city VARCHAR(100),
    address_state VARCHAR(2),
    guardian_full_name VARCHAR(255),
    guardian_tax_id VARCHAR(14),
    guardian_contact_phone VARCHAR(20),
    health_insurance VARCHAR(100),
    insurance_card_number VARCHAR(50),
    allergies TEXT,
    fitzpatrick_phototype INTEGER CHECK (fitzpatrick_phototype BETWEEN 1 AND 6),
    general_observations TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índice único parcial (apenas pacientes ativos)
CREATE UNIQUE INDEX patients_workspace_id_tax_id_active_unique ON patients (workspace_id, tax_id) WHERE is_active = true;

CREATE TABLE doctor_patient_assignments (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, patient_id)
);

CREATE TABLE records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    author_id UUID REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    subjective TEXT,
    objective TEXT,
    assessment TEXT,
    plan TEXT,
    chief_complaint TEXT,
    history_of_present_illness TEXT,
    dermatological_exam TEXT,
    diagnostic_hypothesis VARCHAR(255),
    cid10_code VARCHAR(10),
    record_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    status appointment_status NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT,
    record_id UUID REFERENCES records(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE procedure_points (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    record_id UUID NOT NULL REFERENCES records(id) ON DELETE CASCADE,
    diagram_area VARCHAR(50) NOT NULL,
    coordinates JSONB NOT NULL,
    procedure_name VARCHAR(255) NOT NULL,
    product_used VARCHAR(255),
    quantity DECIMAL(10, 2),
    product_lot VARCHAR(100)
);

CREATE TABLE photos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    record_id UUID NOT NULL REFERENCES records(id) ON DELETE CASCADE,
    procedure_point_id UUID REFERENCES procedure_points(id) ON DELETE SET NULL,
    storage_url VARCHAR(1024) NOT NULL,
    caption VARCHAR(255),
    photo_type VARCHAR(50) NOT NULL,
    tags TEXT[],
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =================================================================================
-- TABELAS DE SUPORTE
-- =================================================================================
CREATE TABLE workspace_settings (
    workspace_id UUID PRIMARY KEY REFERENCES workspaces(id) ON DELETE CASCADE,
    allow_all_doctors_to_see_all_patients BOOLEAN NOT NULL DEFAULT false,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    link_to VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE audits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID REFERENCES workspaces(id) ON DELETE SET NULL,
    target_table VARCHAR(100) NOT NULL,
    record_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    previous_data JSONB,
    new_data JSONB,
    action_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address INET
);

-- =================================================================================
-- TABELAS PARA ANÁLISE DE EXAMES
-- =================================================================================
CREATE TABLE lab_exams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    author_id UUID REFERENCES users(id) ON DELETE SET NULL,
    exam_date DATE,
    original_pdf_url VARCHAR(1024) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE lab_exam_results (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    lab_exam_id UUID NOT NULL REFERENCES lab_exams(id) ON DELETE CASCADE,
    group_title VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    value TEXT,
    reference_range TEXT,
    observation TEXT,
    doctor_gpt_analysis TEXT
);

-- =================================================================================
-- ÍNDICES PARA PERFORMANCE
-- =================================================================================
CREATE INDEX idx_appointments_on_start_time ON appointments(start_time);
CREATE INDEX idx_patients_on_workspace_id ON patients(workspace_id);
CREATE INDEX idx_users_on_workspace_id ON users(workspace_id);
CREATE INDEX idx_users_password_reset_token ON users(password_reset_token);
CREATE INDEX idx_records_on_patient_id ON records(patient_id);
CREATE INDEX idx_patients_full_name_trgm ON patients USING GIN (full_name gin_trgm_ops);
CREATE INDEX idx_lab_exams_on_patient_id ON lab_exams(patient_id);


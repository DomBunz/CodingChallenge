-- V1__initial_schema.sql
-- Initial database schema for insurance premium calculator

CREATE TABLE region_factors (
    id BIGSERIAL PRIMARY KEY,
    federal_state VARCHAR(100) NOT NULL,
    factor DECIMAL(5, 2) NOT NULL,
    CONSTRAINT uk_region_factors_federal_state UNIQUE (federal_state)
);

CREATE TABLE regions (
    id BIGSERIAL PRIMARY KEY,
    federal_state VARCHAR(100) NOT NULL, -- REGION1
    country VARCHAR(100) NOT NULL, -- REGION3
    area VARCHAR(100), -- REGION4
    city VARCHAR(100) NOT NULL, -- ORT
    postal_code VARCHAR(10) NOT NULL, -- POSTLEITZAHL
    district VARCHAR(100), -- AREA1
    region_factor_id BIGINT NOT NULL REFERENCES region_factors(id),
    CONSTRAINT uk_regions_multiple UNIQUE (federal_state, country, area, city, postal_code, district)
);

CREATE TABLE vehicle_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    factor DECIMAL(5, 2) NOT NULL,
    CONSTRAINT uk_vehicle_types_name UNIQUE (name)
);

CREATE TABLE mileage_factors (
    id BIGSERIAL PRIMARY KEY,
    min_mileage INTEGER NOT NULL,
    max_mileage INTEGER,
    factor DECIMAL(5, 2) NOT NULL,
    CONSTRAINT uk_mileage_factors_range UNIQUE (min_mileage, max_mileage)
);

-- applications do not have FKs to region/vehicle, as those factors may be changed by admins
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    annual_mileage INTEGER NOT NULL,
    vehicle_type VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    base_premium DECIMAL(10, 2) NOT NULL,
    mileage_factor DECIMAL(5, 2) NOT NULL,
    vehicle_factor DECIMAL(5, 2) NOT NULL,
    region_factor DECIMAL(5, 2) NOT NULL,
    calculated_premium DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    CONSTRAINT chk_application_status CHECK (status IN ('NEW', 'ACCEPTED', 'REJECTED'))
);

CREATE TABLE system_configurations (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL,
    config_value VARCHAR(255) NOT NULL,
    description TEXT,
    last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_system_configurations_key UNIQUE (config_key)
);

-- Create indices for better query performance
CREATE INDEX idx_regions_postal_code ON regions(postal_code);
CREATE INDEX idx_applications_created_at ON applications(created_at);
CREATE INDEX idx_applications_status ON applications(status);

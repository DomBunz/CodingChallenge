-- V3__security_schema.sql
-- Spring Security tables

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE authorities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE TABLE role_authorities (
    role_id BIGINT NOT NULL,
    authority_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, authority_id),
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    FOREIGN KEY (authority_id) REFERENCES authorities (id) ON DELETE CASCADE
);

INSERT INTO roles (name) VALUES 
    ('ROLE_ADMIN'),
    ('ROLE_AGENT'),
    ('ROLE_CUSTOMER'),
    ('ROLE_API_CLIENT');

INSERT INTO authorities (name) VALUES 
    -- Application management
    ('APPLICATION_CREATE'),
    ('APPLICATION_READ'),
    ('APPLICATION_READ_ALL'),
    ('APPLICATION_UPDATE'),
    ('APPLICATION_DELETE'),
    
    -- Premium calculation
    ('PREMIUM_CALCULATE'),
    ('PREMIUM_FACTORS_READ'),
    ('PREMIUM_FACTORS_UPDATE'),
    
    -- User management
    ('USER_CREATE'),
    ('USER_READ'),
    ('USER_READ_ALL'),
    ('USER_UPDATE'),
    ('USER_DELETE'),
    
    -- System management
    ('SYSTEM_CONFIG_READ'),
    ('SYSTEM_CONFIG_UPDATE');

-- ROLE_ADMIN gets all authorities
INSERT INTO role_authorities (role_id, authority_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'),
    id
FROM 
    authorities;

-- ROLE_AGENT authorities
INSERT INTO role_authorities (role_id, authority_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'ROLE_AGENT'),
    id
FROM 
    authorities 
WHERE 
    name IN (
        'APPLICATION_CREATE', 
        'APPLICATION_READ', 
        'APPLICATION_READ_ALL', 
        'APPLICATION_UPDATE',
        'PREMIUM_CALCULATE',
        'PREMIUM_FACTORS_READ',
        'USER_READ'
    );

-- ROLE_CUSTOMER authorities
INSERT INTO role_authorities (role_id, authority_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'ROLE_CUSTOMER'),
    id
FROM 
    authorities 
WHERE 
    name IN (
        'APPLICATION_CREATE', 
        'APPLICATION_READ',
        'PREMIUM_CALCULATE'
    );

-- ROLE_API_CLIENT authorities
INSERT INTO role_authorities (role_id, authority_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'ROLE_API_CLIENT'),
    id
FROM 
    authorities 
WHERE 
    name IN (
        'PREMIUM_CALCULATE',
        'APPLICATION_CREATE'
    );

-- Create admin user with bcrypt encoded password 'admin'
INSERT INTO users (username, password, email, first_name, last_name)
VALUES (
    'admin',
    '$2a$10$ugRpl4R9fMZ4VfYdhbs./ubFK6O4UbRc26F7vy7WYCa8zJcHC.r0K',
    'admin@example.com',
    'Admin',
    'User'
);

-- Create agent user with bcrypt encoded password 'agent'
INSERT INTO users (username, password, email, first_name, last_name)
VALUES (
    'agent',
    '$2a$10$XZ5O6ZwCWzy3TrPOiFT7denzfi3c6KiU4BTYJ5DKGOkzo137pVFeW',
    'agent@example.com',
    'Agent',
    'User'
);

-- Create customer user with bcrypt encoded password 'customer'
INSERT INTO users (username, password, email, first_name, last_name)
VALUES (
    'customer',
    '$2a$10$f4xd4ljMxs5kMNpGvA/souRrrt5l7SbC/D//Ms9L66ZXdtfLdOT72',
    'customer@example.com',
    'Customer',
    'User'
);

-- Create api_client user with bcrypt encoded password 'api_client'
INSERT INTO users (username, password, email, first_name, last_name)
VALUES (
    'api_client',
    '$2a$10$yFu4pqK4714tQMV0UoDqVeGkOsww8g37rGZheezhLpUrDqQBBfTOq',
    'api@example.com',
    'API',
    'Client'
);

-- Assign ROLE_ADMIN to admin user
INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE username = 'admin'),
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
);

-- Assign ROLE_AGENT to agent user
INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE username = 'agent'),
    (SELECT id FROM roles WHERE name = 'ROLE_AGENT')
);

-- Assign ROLE_CUSTOMER to customer user
INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE username = 'customer'),
    (SELECT id FROM roles WHERE name = 'ROLE_CUSTOMER')
);

-- Assign ROLE_API_CLIENT to api_client user
INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE username = 'api_client'),
    (SELECT id FROM roles WHERE name = 'ROLE_API_CLIENT')
);

-- Create indexes for better performance
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);

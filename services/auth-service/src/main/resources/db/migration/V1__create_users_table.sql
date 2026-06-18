-- ============================================================
-- V1: Create users table
-- Auth Service — PostgreSQL
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";  -- for gen_random_uuid()

CREATE TYPE user_role AS ENUM (
    'CUSTOMER',
    'SELLER',
    'ADMIN',
    'SUPPORT'
);

CREATE TYPE user_status AS ENUM (
    'PENDING_VERIFICATION',
    'ACTIVE',
    'LOCKED',
    'SUSPENDED',
    'DELETED'
);

CREATE TABLE users (
                       id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
                       email                   VARCHAR(255)    NOT NULL,
                       password_hash           VARCHAR(255)    NOT NULL,
                       first_name              VARCHAR(100)    NOT NULL,
                       last_name               VARCHAR(100)    NOT NULL,
                       role                    VARCHAR(20)     NOT NULL,
                       status                  VARCHAR(30)     NOT NULL DEFAULT 'PENDING_VERIFICATION',
                       failed_login_attempts   INTEGER         NOT NULL DEFAULT 0,
                       locked_until            TIMESTAMPTZ,
                       created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
                       updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
                       created_by              VARCHAR(100),
                       updated_by              VARCHAR(100),

                       CONSTRAINT pk_users PRIMARY KEY (id),
                       CONSTRAINT uq_users_email UNIQUE (email),
                       CONSTRAINT chk_users_role   CHECK (role   IN ('CUSTOMER','SELLER','ADMIN','SUPPORT')),
                       CONSTRAINT chk_users_status CHECK (status IN ('PENDING_VERIFICATION','ACTIVE','LOCKED','SUSPENDED','DELETED'))
);

-- Indexes
CREATE INDEX idx_users_email  ON users (LOWER(email));
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_role   ON users (role);

-- Auto-update updated_at on row change
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Seed a default ADMIN account (password: Admin@12345 — change immediately in prod)
-- Password hash generated with BCrypt cost 12
INSERT INTO users (
    id, email, password_hash, first_name, last_name,
    role, status, created_by, updated_by
) VALUES (
             gen_random_uuid(),
             'admin@smartorder.com',
             '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
             'Platform',
             'Admin',
             'ADMIN',
             'ACTIVE',
             'SYSTEM',
             'SYSTEM'
         );
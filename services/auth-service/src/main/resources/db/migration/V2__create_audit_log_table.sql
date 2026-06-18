-- ============================================================
-- V2: Create auth_audit_log table
-- Stores security events locally before Kafka publishes them
-- to the notification service (Transactional Outbox support)
-- ============================================================

CREATE TABLE auth_audit_log (
                                id              UUID            NOT NULL DEFAULT gen_random_uuid(),
                                user_id         UUID,
                                event_type      VARCHAR(50)     NOT NULL,
                                ip_address      VARCHAR(45),
                                details         TEXT,
                                created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

                                CONSTRAINT pk_auth_audit_log PRIMARY KEY (id)
);

CREATE INDEX idx_audit_log_user_id    ON auth_audit_log (user_id);
CREATE INDEX idx_audit_log_event_type ON auth_audit_log (event_type);
CREATE INDEX idx_audit_log_created_at ON auth_audit_log (created_at DESC);

-- ============================================================
-- V2b: Outbox table for reliable event publishing
-- The Transactional Outbox Pattern: events are written here
-- in the same DB transaction as the business operation,
-- then a scheduler reads and publishes them to Kafka.
-- ============================================================

CREATE TABLE outbox_events (
                               id              UUID            NOT NULL DEFAULT gen_random_uuid(),
                               aggregate_type  VARCHAR(50)     NOT NULL,   -- e.g. 'User'
                               aggregate_id    VARCHAR(255)    NOT NULL,   -- e.g. userId
                               event_type      VARCHAR(100)    NOT NULL,   -- e.g. 'UserRegistered'
                               payload         JSONB           NOT NULL,
                               published       BOOLEAN         NOT NULL DEFAULT FALSE,
                               created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
                               published_at    TIMESTAMPTZ,

                               CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_unpublished ON outbox_events (published, created_at)
    WHERE published = FALSE;
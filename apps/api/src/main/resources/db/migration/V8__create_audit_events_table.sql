-- Audit events table for tracking all important system actions (FR37)
CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    company_id UUID REFERENCES companies(id) ON DELETE SET NULL,
    event_type VARCHAR(50) NOT NULL,
    actor_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    target_type VARCHAR(50),
    target_id VARCHAR(255),
    payload JSONB,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_audit_events_company_id ON audit_events(company_id);
CREATE INDEX idx_audit_events_event_type ON audit_events(event_type);
CREATE INDEX idx_audit_events_timestamp ON audit_events(timestamp);
CREATE INDEX idx_audit_events_actor_id ON audit_events(actor_id);

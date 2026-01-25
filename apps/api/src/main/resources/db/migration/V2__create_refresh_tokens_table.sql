CREATE TABLE refresh_tokens
(
    token       VARCHAR(255) PRIMARY KEY,
    customer_id UUID                     NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    revoked_at  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_refresh_tokens__customer_id ON refresh_tokens (customer_id);
CREATE INDEX idx_refresh_tokens__expires_at ON refresh_tokens (expires_at);

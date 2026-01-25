-- V3__create_user_oauth_providers_table.sql
-- OAuth provider links for customers (GitHub, Google, etc.)

CREATE TABLE user_oauth_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(provider, provider_user_id)
);

-- Only index on user_id; UNIQUE constraint already indexes (provider, provider_user_id)
CREATE INDEX idx_user_oauth_providers__user_id ON user_oauth_providers(user_id);


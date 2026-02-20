CREATE TABLE packages (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    name VARCHAR(255) NOT NULL,
    registry VARCHAR(50) NOT NULL DEFAULT 'npm',
    imported_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE(company_id, name, registry)
);

CREATE INDEX idx_packages_company_id ON packages(company_id);
CREATE INDEX idx_packages_company_id_name ON packages(company_id, name);


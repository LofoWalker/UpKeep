# Story 1.1: Monorepo & Hexagonal Architecture Setup

Status: ready-for-dev

## Story

As a **developer**,
I want a monorepo structure with hexagonal architecture scaffolding,
so that I can start implementing features with clear separation of concerns.

## Acceptance Criteria

1. **Given** I clone the repository  
   **When** I open the project  
   **Then** I see the following structure:
   ```
   apps/
     web/          # React + Vite + TypeScript
     api/          # Quarkus + Java
   ```

2. **Given** the `apps/api/` directory  
   **When** I inspect its structure  
   **Then** it follows hexagonal architecture:
   ```
   src/main/java/com/upkeep/
     domain/           # Entities, Value Objects, Domain Services
     application/      # UseCases (ports in), port interfaces (ports out)
     infrastructure/   # Adapters: REST controllers, JPA repos, external services
   ```

3. **Given** the `apps/web/` directory  
   **When** I inspect its structure  
   **Then** it has a feature-first organization:
   ```
   src/
     features/
     components/
     lib/
     hooks/
   ```

4. **Given** the project root  
   **When** I check for workspace configuration  
   **Then** a root `package.json` with npm workspace configuration exists

5. **Given** npm is installed  
   **When** I run `npm install` at root  
   **Then** all dependencies for both apps are installed successfully

## Tasks / Subtasks

- [ ] Task 1: Initialize monorepo structure (AC: #1, #4, #5)
  - [ ] 1.1: Create root `package.json` with npm workspaces
  - [ ] 1.3: Create root `.gitignore` and `.nvmrc`
  - [ ] 1.4: Create `apps/` directory

- [ ] Task 2: Setup React frontend (`apps/web/`) (AC: #1, #3)
  - [ ] 2.1: Initialize Vite + React + TypeScript project
  - [ ] 2.2: Configure TailwindCSS with design tokens
  - [ ] 2.3: Initialize shadcn/ui
  - [ ] 2.4: Create feature-first folder structure
  - [ ] 2.5: Configure path aliases (`@/`)

- [ ] Task 3: Setup Quarkus backend (`apps/api/`) (AC: #1, #2)
  - [ ] 3.1: Initialize Quarkus project with required extensions
  - [ ] 3.2: Create hexagonal architecture package structure
  - [ ] 3.3: Add placeholder classes for each layer
  - [ ] 3.4: Configure application.properties for dev profile

- [ ] Task 4: Validate setup (AC: #5)
  - [ ] 4.1: Run `npm install` from root
  - [ ] 4.2: Verify `npm run dev` starts web app
  - [ ] 4.3: Verify `./mvnw quarkus:dev` starts API

## Dev Notes

### Architecture Compliance

**Hexagonal Architecture (Backend):**
The backend MUST follow hexagonal (ports & adapters) architecture:

```
com.upkeep/
├── domain/                    # CORE - No framework dependencies
│   ├── model/                 # Entities, Value Objects, Aggregates
│   ├── service/               # Domain Services (business rules)
│   └── exception/             # Domain-specific exceptions
│
├── application/               # USE CASES - Orchestrates domain
│   ├── port/
│   │   ├── in/               # Driving ports (use case interfaces)
│   │   └── out/              # Driven ports (repository/service interfaces)
│   └── usecase/              # Use case implementations
│
└── infrastructure/            # ADAPTERS - Framework-dependent
    ├── adapter/
    │   ├── in/
    │   │   └── rest/         # REST Controllers (driving adapters)
    │   └── out/
    │       ├── persistence/  # JPA Repositories (driven adapters)
    │       └── external/     # External service adapters
    └── config/               # Spring/Quarkus configuration
```

**Key Rules:**
- Domain layer has ZERO dependencies on infrastructure
- Application layer depends ONLY on domain
- Infrastructure adapts external systems to domain ports
- All dependencies point INWARD

### Tech Stack Versions (Pin These)

| Technology | Version | Notes |
|------------|---------|-------|
| Node.js | 20 LTS | Use `.nvmrc` |
| npm | 10.x | Workspace management |
| React | 18.x | Latest stable |
| Vite | 5.x | Build tool |
| TypeScript | 5.x | Both frontend |
| TailwindCSS | 3.4.x | Styling |
| shadcn/ui | latest | Component library |
| Java | 21 LTS | Backend runtime |
| Quarkus | 3.x | Backend framework |
| PostgreSQL | 16 | Database (for next story) |

### Frontend Structure (Feature-First)

```
apps/web/
├── src/
│   ├── components/
│   │   ├── ui/              # shadcn/ui components (auto-generated)
│   │   ├── common/          # Custom shared (PackageCard, BudgetBar, etc.)
│   │   └── layout/          # Navbar, PageLayout, TabNav
│   ├── features/
│   │   ├── auth/            # Authentication feature
│   │   ├── company/         # Company workspace feature
│   │   ├── allocation/      # Budget allocation feature
│   │   ├── maintainer/      # Maintainer portal feature
│   │   ├── public-company/  # Public sponsorship page
│   │   └── admin/           # Admin panel feature
│   ├── hooks/               # Custom React hooks
│   ├── lib/                 # Utilities (api client, formatters)
│   ├── pages/               # Route-level components
│   └── styles/              # Global styles, design tokens
├── public/
├── index.html
├── vite.config.ts
├── tailwind.config.ts
├── tsconfig.json
└── package.json
```

### Quarkus Extensions Required

```xml
<!-- pom.xml extensions -->
<dependencies>
  <!-- REST -->
  <dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-jackson</artifactId>
  </dependency>
  
  <!-- Validation -->
  <dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-validator</artifactId>
  </dependency>
  
  <!-- Database (for future stories) -->
  <dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-orm-panache</artifactId>
  </dependency>
  <dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jdbc-postgresql</artifactId>
  </dependency>
  
  <!-- Testing -->
  <dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5</artifactId>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

### Design Tokens (TailwindCSS)

Configure in `tailwind.config.ts`:

```typescript
// From architecture.md - Design System Integration
export default {
  theme: {
    extend: {
      colors: {
        primary: 'var(--primary)',
        success: 'var(--success)',
        warning: 'var(--warning)',
        error: 'var(--error)',
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      spacing: {
        // 4px base unit already default in Tailwind
      },
    },
  },
}
```

### Path Aliases Configuration

**vite.config.ts:**
```typescript
resolve: {
  alias: {
    '@': path.resolve(__dirname, './src'),
  },
}
```

**tsconfig.json:**
```json
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  }
}
```

### Commands Reference

**Root level:**
```bash
npm install          # Install all workspace dependencies
npm run dev          # Start web dev server (if script configured)
npm run dev -w web   # Start web specifically
```

**Backend:**
```bash
cd apps/api
./mvnw quarkus:dev    # Start Quarkus in dev mode
./mvnw test           # Run tests
```

### Project Structure Notes

- Monorepo managed by npm workspaces
- Each app has its own `package.json` (web) or `pom.xml` (api)
- Shared contracts/types can be added to `libs/` later if needed
- `infra/` folder removed (deferred decision)

### References

- [Source: architecture.md#Starter-Template-Evaluation] - React + Quarkus selection rationale
- [Source: architecture.md#Structure-Patterns] - Monorepo layout
- [Source: architecture.md#Frontend-Architecture] - Feature-first organization
- [Source: architecture.md#Naming-Patterns] - Code conventions
- [Source: epics.md#Story-1.1] - Original acceptance criteria

## Dev Agent Record

### Agent Model Used

_To be filled by dev agent_

### Completion Notes List

_To be filled during implementation_

### Change Log

_To be filled during implementation_

### File List

_To be filled after implementation - list all created/modified files_


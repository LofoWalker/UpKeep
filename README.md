# Upkeep Monorepo

Open-source fund allocation platform for npm package maintainers.

## Project Structure

```
upkeep/
├── apps/
│   ├── web/                 # React + Vite + TypeScript frontend
│   │   ├── src/
│   │   │   ├── features/    # Feature-first organization
│   │   │   ├── components/  # Reusable UI components
│   │   │   ├── hooks/       # Custom React hooks
│   │   │   ├── lib/         # Utilities and helpers
│   │   │   └── pages/       # Route-level components
│   │   ├── index.html
│   │   ├── vite.config.ts
│   │   ├── tailwind.config.ts
│   │   └── package.json
│   │
│   └── api/                 # Quarkus + Java backend
│       ├── src/main/java/com/upkeep/
│       │   ├── domain/      # Core business logic (no framework deps)
│       │   │   ├── model/   # Entities and value objects
│       │   │   ├── service/ # Domain services
│       │   │   └── exception/
│       │   ├── application/ # Use cases and ports
│       │   │   ├── port/in/  # Driving ports (use case interfaces)
│       │   │   ├── port/out/ # Driven ports (repository/service interfaces)
│       │   │   └── usecase/  # Use case implementations
│       │   └── infrastructure/ # Framework-specific adapters
│       │       └── adapter/
│       │           ├── in/rest/    # REST controllers
│       │           └── out/        # Persistence and external service adapters
│       ├── src/main/resources/
│       │   └── application.properties
│       └── pom.xml
│
└── package.json            # npm workspace configuration
```

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Frontend | React | 18.x |
| Build Tool | Vite | 5.x |
| Language | TypeScript | 5.x |
| Styling | TailwindCSS | 3.4.x |
| Components | shadcn/ui | latest |
| Backend | Quarkus | 3.x |
| Runtime | Java | 21 LTS |
| Package Manager | npm | 10.x |
| Node.js | Node.js | 20 LTS |

## Getting Started

### Prerequisites
- Node.js 20 LTS (use `.nvmrc`)
- npm 10.x
- Java 21 LTS
- Maven 3.8.x (for building Quarkus backend)

### Installation

```bash
# Install all dependencies (frontend and backend)
npm install
```

### Development

**Frontend:**
```bash
npm run dev
# or specifically
npm run dev:web
```

**Backend:**
```bash
npm run dev:api
# or
cd apps/api
./mvnw quarkus:dev
```

### Building

```bash
npm run build
```

## Architecture Principles

### Hexagonal Architecture (Backend)

The backend follows **hexagonal (ports & adapters)** architecture:

- **Domain Layer**: Core business logic with zero framework dependencies
- **Application Layer**: Use cases that orchestrate domain logic
- **Infrastructure Layer**: Framework-specific adapters (REST, persistence, external services)

### Feature-First Organization (Frontend)

The frontend is organized by features, making it easy to locate and maintain feature-specific code:

- **features/**: Feature modules (auth, company, allocation, etc.)
- **components/**: Reusable UI components
- **hooks/**: Custom React hooks
- **lib/**: Shared utilities and API client

## Development Workflow

1. Create a feature branch: `git checkout -b feat/feature-name`
2. Implement the feature
3. Test locally
4. Create a pull request
5. Code review and merge

## Design System

TailwindCSS configuration includes design tokens for colors, typography, and spacing.
All components should follow the design system guidelines.

## Next Steps

- Story 1.2: Database setup and user authentication
- Story 1.3: Core API endpoints
- Story 1.4: Frontend routing and layout


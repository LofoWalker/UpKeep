<p align="center">
  <img src="https://img.shields.io/badge/License-MIT-green.svg" alt="License: MIT">
  <img src="https://img.shields.io/badge/Java-21-orange.svg" alt="Java 21">
  <img src="https://img.shields.io/badge/Node.js-20%20LTS-brightgreen.svg" alt="Node.js 20 LTS">
  <img src="https://img.shields.io/badge/React-18.x-blue.svg" alt="React 18">
  <img src="https://img.shields.io/badge/Quarkus-3.x-red.svg" alt="Quarkus 3.x">
</p>

# Upkeep

**Open-source fund allocation platform for package maintainers.**

Upkeep enables companies to sustainably fund the open-source maintainers they depend on, through a structured voting and allocation mechanism based on real usage and risk.

> Transform open-source dependency into sustainable funding for maintainers, based on actual risk and usage.

## Why Upkeep?

Open-source is a critical pillar of modern software. Millions of companies use open-source libraries daily to power high-value products. Yet:

- Most projects are maintained by **one or two people**
- Funding relies on volunteering, sporadic sponsors, or indirect models
- There's a **massive gap** between economic usage and maintainer sustainability

Upkeep bridges this gap with a **structured, rational funding mechanism** that companies can justify internally.

## Key Features

- 🏢 **Company Workspaces** - Manage your open-source budget and track dependencies
- 📦 **Package Import** - Import npm dependencies via file upload or paste
- 💰 **Budget Allocation** - Set monthly budgets and allocate funds to critical packages
- 🗳️ **Structured Voting** - Allocate points to packages that represent real risk
- 👤 **Maintainer Profiles** - Opt-in system for maintainers to receive funding
- 🔐 **Package Claims** - Verify ownership and eligibility for packages
- 💸 **Payout System** - Transparent distribution based on company allocations
- 📊 **Admin Dashboard** - Monitor payout runs and handle failures

## Tech Stack

| Layer | Technology | Version |
|-------|------------|---------|
| **Frontend** | React + TypeScript | 18.x / 5.x |
| **Build Tool** | Vite | 5.x |
| **Styling** | TailwindCSS + shadcn/ui | 3.4.x |
| **Backend** | Quarkus (Java) | 3.x |
| **Runtime** | Java | 21 LTS |
| **Package Manager** | npm | 10.x |
| **Node.js** | Node.js | 20 LTS |

## Quick Start

### Prerequisites

- Node.js 20 LTS
- npm 10.x
- Java 21 LTS
- Maven 3.8.x

### Installation

```bash
# Clone the repository
git clone https://github.com/LofoWalker/upkeep.git
cd upkeep

# Install all dependencies
npm install
```

### Development

```bash
# Start frontend (React + Vite)
npm run dev:web

# Start backend (Quarkus)
npm run dev:api
# Or alternatively
cd apps/api && ./mvnw quarkus:dev
```

The frontend runs at `http://localhost:5173` and the API at `http://localhost:8080`.

### Build

```bash
# Build all apps
npm run build
```

## Project Structure

```
upkeep/
├── apps/
│   ├── web/                    # React + Vite frontend
│   │   ├── src/
│   │   │   ├── features/       # Feature modules
│   │   │   │   ├── auth/       # Authentication
│   │   │   │   ├── company/    # Company management
│   │   │   │   ├── allocation/ # Fund allocation
│   │   │   │   ├── maintainer/ # Maintainer profiles
│   │   │   │   └── admin/      # Admin dashboard
│   │   │   ├── components/     # Reusable UI components
│   │   │   ├── hooks/          # Custom React hooks
│   │   │   └── lib/            # Utilities and API client
│   │   └── package.json
│   │
│   └── api/                    # Quarkus backend
│       └── src/main/java/com/upkeep/
│           ├── domain/         # Core business logic
│           ├── application/    # Use cases and ports
│           └── infrastructure/ # REST adapters, persistence
│
├── docs/                       # Documentation
└── package.json                # Workspace configuration
```

## Architecture

Upkeep follows **Hexagonal Architecture** (Ports & Adapters) for the backend:

- **Domain Layer**: Core business logic with zero framework dependencies
- **Application Layer**: Use cases that orchestrate domain logic via ports
- **Infrastructure Layer**: Framework-specific adapters (REST controllers, persistence)

The frontend uses a **Feature-First Organization** for scalable development.

See [Architecture Documentation](./_bmad-output/planning-artifacts/architecture.md) for details.

## Security

Please report security vulnerabilities by following our [Security Policy](./SECURITY.md).

## License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.

## Acknowledgments

- All open-source maintainers who inspire this project
- The Quarkus and React communities
- Contributors and early adopters

---

<p align="center">
  <strong>Built with ❤️ to support open-source sustainability</strong>
</p>

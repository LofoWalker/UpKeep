## Contributing

Thanks for your interest in contributing to Upkeep.

### Development Setup

Prerequisites:

- Node.js 20
- npm
- Java 21
- Maven

Install dependencies:

- `npm install`

Run the apps:

- Frontend: `npm run dev:web`
- API: `npm run dev:api`

### How to Contribute

1. Create an issue describing the bug/feature.
2. Create a branch from the default branch.
3. Keep changes focused and small.
4. Add or update tests when relevant.
5. Make sure CI passes.

### Code Quality

Before opening a PR, please run:

- `npm run lint`
- `npm run build`
- `cd apps/api && ./mvnw test`

### Pull Request Guidelines

- Provide a clear description of what changed and why
- Link the relevant issue
- Include screenshots for UI changes
- Avoid unrelated formatting/refactors

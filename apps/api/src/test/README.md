# Tests - Upkeep API

Ce dossier contient tous les tests du projet Upkeep API.

## Structure

```
src/test/java/com/upkeep/
├── application/usecase/          # Tests unitaires des Use Cases
├── domain/model/                 # Tests unitaires des modèles Domain
│   ├── company/
│   ├── customer/
│   ├── invitation/
│   └── membership/
└── infrastructure/adapter/       # Tests d'intégration et adapters
    ├── in/rest/                  # Tests REST endpoints
    │   ├── auth/
    │   ├── common/exception/     # Tests ExceptionMappers
    │   └── company/
    └── out/                      # Tests adapters sortants
        ├── oauth/
        └── security/
```

## Statistiques

- **Total tests :** 294
- **Tests unitaires :** ~250
- **Tests d'intégration :** ~44
- **Couverture estimée :** 70-75%

## Nouveaux tests (2026-01-28)

### Exception Mapping
- `GlobalExceptionMapperTest` (16 tests)
- `ConstraintViolationExceptionMapperTest` (3 tests)

### Sécurité
- `BcryptPasswordHasherTest` (4 tests)
- `JwtTokenServiceTest` (9 tests)

### OAuth
- `InMemoryOAuthStateServiceTest` (4 tests)

### Domain Value Objects
- `InvitationTokenTest` (5 tests)
- `InvitationIdTest` (4 tests)
- `MembershipIdTest` (4 tests)

## Exécuter les tests

### Tous les tests
```bash
./mvnw test
```

### Un test spécifique
```bash
./mvnw test -Dtest=GlobalExceptionMapperTest
```

### Tests par pattern
```bash
./mvnw test -Dtest="*ExceptionMapperTest"
```

### Avec rapport de couverture (nécessite JaCoCo)
```bash
./mvnw test jacoco:report
```

## Conventions de test

### Nommage
- Classes de test : `{NomClasse}Test`
- Méthodes de test : `shouldDoXWhenY()`

### Annotations
- `@DisplayName` sur toutes les classes et méthodes
- `@QuarkusTest` pour les tests d'intégration
- `@Nested` pour grouper les tests par fonctionnalité

### Pattern AAA
Tous les tests suivent le pattern Arrange-Act-Assert :
```java
@Test
@DisplayName("should return 404 when customer not found")
void shouldReturn404WhenCustomerNotFound() {
    // Arrange
    CustomerNotFoundException exception = new CustomerNotFoundException("test@example.com");
    
    // Act
    Response response = mapper.toResponse(exception);
    
    // Assert
    assertEquals(404, response.getStatus());
}
```

### Imports
**JAMAIS de star imports** - toujours des imports explicites :
```java
// ❌ Interdit
import jakarta.ws.rs.*;

// ✅ Correct
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
```

## Mocks et Stubs

- **Mockito** pour les mocks
- **@InjectMock** pour l'injection de mocks Quarkus
- **RestAssured** pour les tests d'API REST

## Tests d'intégration

Les tests d'intégration utilisent :
- Base H2 en mémoire
- Transactions isolées par test
- Configuration de test (`application.properties` dans `src/test/resources`)

## Rapports

Les rapports de tests sont générés dans :
- `target/surefire-reports/` (XML)
- `target/site/jacoco/` (Couverture, si JaCoCo configuré)

## Ressources

- [Quarkus Testing Guide](https://quarkus.io/guides/getting-started-testing)
- [RestAssured Documentation](https://rest-assured.io/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

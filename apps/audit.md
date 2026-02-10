# 🔥 APOCALYPSE.MD - Audit Draconien du Projet Upkeep API

**Date :** 30 Janvier 2026
**Auditeur :** L'Architecte Draconien
**Sujet :** Audit impitoyable du projet Quarkus Upkeep API

---

## 1. LE VERDICT GLOBAL

### Note : 7.2 / 10

**Résumé Cinglant :**

Ce projet est *correct*. Pas brillant, pas désastreux — correct. L'architecture hexagonale est respectée dans ses grandes lignes, ce qui est déjà mieux que 80% des projets que j'audite. Cependant, derrière cette façade de propreté se cachent des compromis architecturaux, des violations subtiles de SOLID, et des incohérences qui trahissent un manque de rigueur dans l'application des principes.

Le domaine est relativement pur, mais des annotations Jakarta se sont infiltrées dans la couche application. Les use cases sont parfois trop permissifs avec leurs responsabilités. La gestion des transactions est déléguée aveuglément à l'infrastructure. Les tests sont présents mais manquent de cas limites critiques.

**Ce n'est pas un désastre, mais ce n'est pas non plus l'œuvre d'un artisan du code.**

---

## 2. L'ARCHITECTURE

### 2.1 Structure des Packages

```
com.upkeep/
├── domain/           ✅ Pur (à quelques exceptions près)
│   ├── exception/    ✅ Correct
│   └── model/        ✅ Bien organisé par sous-domaine
├── application/      ⚠️ Pollution détectée
│   ├── port/in/      ✅ Correct
│   ├── port/out/     ✅ Correct
│   └── usecase/      ⚠️ Annotations Jakarta présentes
└── infrastructure/   ✅ Bien isolée
    └── adapter/
        ├── in/rest/  ✅ Correct
        └── out/      ✅ Correct
```

### 2.2 Critique Architecturale

**Points Positifs :**

- La séparation en couches est claire et respectée
- Les Value Objects sont utilisés correctement (`Email`, `Password`, `CustomerId`, etc.)
- Les entités de domaine utilisent des factory methods (`create()`, `reconstitute()`)
- Les ports (interfaces) sont bien définis et séparent les préoccupations
- L'infrastructure est correctement isolée avec des mappers dédiés

**Points Négatifs Critiques :**

1. **Pollution de la couche Application** - Les use cases sont annotés avec `@ApplicationScoped` et `@Transactional` (Jakarta). C'est une violation du principe de pureté. La couche application devrait être agnostique du framework. Un décorateur transactionnel devrait être dans l'infrastructure.

2. **Dépendance inversée incorrecte** - `TokenService` dans `application/port/out/auth/` retourne des records `TokenClaims` et `RefreshResult` qui contiennent des primitives. Acceptable, mais ces types devraient être dans le domaine si on veut être rigoureux.

3. **Absence d'un module d'entrée clair** - Pas de classe `Main` ou de configuration explicite de l'assemblage des dépendances. Quarkus fait tout automagiquement, ce qui masque les dépendances réelles.

---

## 3. ANALYSE FICHIER PAR FICHIER

### 3.1 COUCHE DOMAINE

#### `domain/model/customer/Customer.java`

**Lignes 64-65 :**

```java
public void updateTimestamp() {
    this.updatedAt = Instant.now();
}
```

**Verdict :** 🟡 Cette méthode couple l'entité au temps système. Un `Clock` devrait être injecté ou le timestamp passé en paramètre pour permettre les tests déterministes.

---

#### `domain/model/customer/Email.java`

**Ligne 23 :**

```java
  value = normalizedValue;
```

**Verdict :** 🔴 **ERREUR SUBTILE !** Dans un record Java, la réassignation du paramètre `value` dans le constructeur compact ne modifie PAS la valeur stockée. Le record stockera toujours la valeur originale, pas `normalizedValue`. Ce bug signifie que les emails ne sont PAS normalisés en lowercase.

**Correction requise :** Utiliser un constructeur canonique ou une factory method.

---

#### `domain/model/invitation/Invitation.java`

**Lignes 86-89 :**

```java
public void accept() {
    if (!canBeAccepted()) {
        throw new IllegalStateException("Invitation cannot be accepted");
    }
```

**Verdict :** 🟡 `IllegalStateException` est une exception technique, pas une exception métier. Devrait être une `DomainException` dédiée comme `InvitationCannotBeAcceptedException`.

---

#### `domain/model/budget/Money.java`

**Ligne 29 :**

```java
long cents = amount.multiply(BigDecimal.valueOf(100)).longValue();
```

**Verdict :** 🟡 `longValue()` tronque silencieusement. Si quelqu'un passe `BigDecimal("10.999")`, on perd de la précision. Devrait utiliser `longValueExact()` ou vérifier qu'il n'y a pas de décimales au-delà de 2 chiffres.

---

#### `domain/exception/DomainValidationException.java`

**Verdict :** ✅ Propre, bien conçu, pas de dépendance framework.

---

#### `domain/model/audit/AuditEvent.java`

**Lignes 37-39 :**

```java
this.payload = new HashMap<>(payload);
```

**Verdict :** ✅ Copie défensive correcte. Bien.

**Ligne 55 :**

```java
Instant.now()
```

**Verdict :** 🟡 Encore une fois, couplage au temps système. Devrait accepter un `Clock` ou un `Instant` en paramètre.

---

### 3.2 COUCHE APPLICATION

#### `application/usecase/RegisterCustomerUseCaseImpl.java`

**Lignes 14-15 :**

```java
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
```

**Verdict :** 🔴 **VIOLATION ARCHITECTURALE MAJEURE !** Les annotations Jakarta n'ont rien à faire dans la couche application. Le use case devrait être un POJO pur. La gestion transactionnelle devrait être dans un décorateur ou dans l'adaptateur repository.

**Lignes 36-40 :**

```java
if (!command.password().equals(command.confirmPassword())) {
    throw new DomainValidationException("Passwords do not match", List.of(
            new FieldError("confirmPassword", "Passwords do not match")
    ));
}
```

**Verdict :** 🟡 Cette validation devrait être dans le `RegisterCommand` lui-même (self-validating command) ou dans un validateur dédié, pas dans le use case.

---

#### `application/usecase/AuthenticateCustomerUseCaseImpl.java`

**Mêmes violations Jakarta (lignes 12-13).**

**Lignes 33-34 :**

```java
Email email = new Email(command.email());
Password password = new Password(command.password());
```

**Verdict :** 🟡 Si le constructeur de `Email` ou `Password` lance une `DomainValidationException`, le message d'erreur exposera des détails de validation inutiles pour une authentification. Pour la sécurité, on devrait catch et transformer en `InvalidCredentialsException` pour ne pas révéler si c'est l'email ou le password qui est invalide.

---

#### `application/usecase/CreateCompanyUseCaseImpl.java`

**Ligne 23 :**

```java
@Inject
public CreateCompanyUseCaseImpl(...)
```

**Verdict :** 🟡 Incohérence de style. Certains use cases utilisent `@Inject` explicitement, d'autres non (injection par constructeur implicite). Choisissez un style et tenez-vous-y.

---

#### `application/usecase/AcceptInvitationUseCaseImpl.java`

**Lignes 51-52 :**

```java
throw new IllegalStateException("Invitation cannot be accepted");
```

**Verdict :** 🔴 Exception technique dans un flux métier. Devrait être une `DomainException`.

**Lignes 58-60 :**

```java
if (membershipRepository.existsByCustomerIdAndCompanyId(customerId, invitation.getCompanyId())) {
    invitation.accept();
    invitationRepository.save(invitation);
    throw new AlreadyMemberException();
}
```

**Verdict :** 🟡 Logique étrange : on accepte l'invitation PUIS on lance une exception. L'ordre des opérations est contre-intuitif et potentiellement bugué si la transaction échoue après le save.

---

#### `application/usecase/OAuthLoginUseCaseImpl.java`

**Ligne 38 :**

```java
throw new IllegalStateException("User not found for OAuth provider link");
```

**Verdict :** 🔴 Encore `IllegalStateException`. Ce cas représente une incohérence de données (un lien OAuth existe mais l'utilisateur non). Devrait être une exception métier dédiée ou une erreur système loggée différemment.

---

#### `application/usecase/SetCompanyBudgetUseCaseImpl.java`

**Verdict :** ✅ Relativement propre. Bonne séparation des responsabilités avec l'audit.

---

#### `application/port/in/RegisterCustomerUseCase.java`

**Verdict :** ✅ Interface propre avec records imbriqués. Pattern Command/Result bien appliqué.

---

#### `application/port/out/auth/TokenService.java`

**Verdict :** 🟡 L'interface expose `Customer` en paramètre (entité du domaine). C'est acceptable mais certains pourraient arguer qu'on devrait passer uniquement les données nécessaires (userId, email, accountType) pour découpler davantage.

---

### 3.3 COUCHE INFRASTRUCTURE

#### `infrastructure/adapter/in/rest/auth/AuthResource.java`

**Lignes 36-43 :**

```java
@ConfigProperty(name = "jwt.access-token-expiry-seconds", defaultValue = "900")
int accessTokenExpirySeconds;

@ConfigProperty(name = "jwt.refresh-token-expiry-seconds", defaultValue = "604800")
int refreshTokenExpirySeconds;

@ConfigProperty(name = "app.use-secure-cookies", defaultValue = "true")
boolean useSecureCookies;
```

**Verdict :** 🟡 Injection de configuration directement dans le Resource. Devrait être encapsulé dans un objet de configuration dédié (`CookieConfiguration`) pour respecter le SRP.

**Lignes 118-127 :**

```java
try {
    TokenClaims claims = tokenService.validateAccessToken(accessToken);
    MeResponse response = new MeResponse(claims.userId(), claims.email(), claims.accountType());
    return Response.ok(ApiResponse.success(response)).build();
} catch (Exception e) {
    return Response.status(401)
            .entity(ApiResponse.error(new ApiError(
                    "INVALID_TOKEN", "Invalid or expired token", null, null)))
            .build();
}
```

**Verdict :** 🔴 `catch (Exception e)` est un anti-pattern. On catch TOUT, y compris les NPE, les erreurs de runtime, etc. Devrait catch uniquement l'exception spécifique de validation de token.

---

#### `infrastructure/adapter/in/rest/company/CompanyResource.java`

**Lignes 70-73 :**

```java
TokenClaims claims = validateToken(accessToken);
if (claims == null) {
    return unauthorizedResponse();
}
```

**Verdict :** 🟡 Ce pattern se répète dans CHAQUE méthode. C'est une violation flagrante de DRY. Devrait utiliser un `ContainerRequestFilter` JAX-RS pour l'authentification centralisée.

---

#### `infrastructure/adapter/out/persistence/customer/CustomerEntity.java`

**Lignes 21-36 :**

```java
public UUID id;
public String email;
public String passwordHash;
```

**Verdict :** 🟡 Champs publics. Panache le permet, mais c'est discutable pour l'encapsulation. De plus, l'entité importe `AccountType` du domaine (ligne 3). Ce n'est pas grave mais certains puristes créeraient un enum séparé pour l'infrastructure.

---

#### `infrastructure/adapter/out/persistence/customer/CustomerMapper.java`

**Lignes 31-37 :**

```java
return Customer.reconstitute(
        new CustomerId(entity.id),
        new Email(entity.email),
        hash,
        entity.accountType,
        entity.createdAt,
        entity.updatedAt
);
```

**Verdict :** 🟡 Le mapper appelle le constructeur de `Email` qui fait de la validation. Si une email invalide est en base (données legacy, migration ratée), le mapper crashera. Le mapper devrait utiliser une méthode `Email.reconstitute()` qui bypass la validation.

---

#### `infrastructure/adapter/out/security/JwtTokenService.java`

**Lignes 29-33 :**

```java
@ConfigProperty(name = "jwt.access-token-expiry-seconds", defaultValue = "900")
int accessTokenExpirySeconds;

@ConfigProperty(name = "jwt.refresh-token-expiry-seconds", defaultValue = "604800")
int refreshTokenExpirySeconds;
```

**Verdict :** 🟡 Duplication avec `AuthResource.java`. Ces valeurs devraient être dans un objet de configuration partagé.

---

#### `infrastructure/adapter/out/oauth/GitHubOAuthAdapter.java`

**Ligne 45 :**

```java
this.httpClient = HttpClient.newHttpClient();
```

**Verdict :** 🔴 Création d'un `HttpClient` dans le constructeur. Ce client devrait être injecté pour permettre les tests et le pooling. De plus, `HttpClient.newHttpClient()` crée un client par défaut sans timeout configuré — potentiel blocage infini sur les appels GitHub.

**Lignes 69-74 :**

```java
HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(TOKEN_URL))
        ...
        .POST(HttpRequest.BodyPublishers.ofString(formBody))
        .build();
```

**Verdict :** 🟡 Pas de timeout configuré sur les requêtes. En production, un GitHub lent pourrait bloquer indéfiniment les threads.

---

#### `infrastructure/adapter/out/email/MockEmailService.java`

**Verdict :** ✅ C'est un mock, pas de critique. Mais attention : en production, il faudra une vraie implémentation. Y a-t-il un TODO ou une issue trackée pour ça ?

---

#### `infrastructure/adapter/in/rest/common/exception/GlobalExceptionMapper.java`

**Lignes 53-145 (pattern switch):**

```java
return switch (exception) {
    case DomainValidationException e -> Response...
    case InvalidCredentialsException e -> Response...
    // ... 15+ cas
```

**Verdict :** 🟡 Ce switch gigantesque viole l'Open/Closed Principle. Chaque nouvelle exception nécessite de modifier ce fichier. Une map de handlers ou un pattern de visiteur serait plus extensible.

---

### 3.4 TESTS

#### `test/.../RegisterCustomerUseCaseImplTest.java`

**Verdict :** ✅ Tests bien structurés avec des cas nominaux et des cas d'erreur.

**Manques identifiés :**

- Pas de test pour email en majuscules (normalisation)
- Pas de test pour password avec caractères spéciaux Unicode
- Pas de test pour les cas de concurrence (deux inscriptions simultanées)

---

#### `test/.../PasswordTest.java`

**Verdict :** ✅ Excellents tests paramétrés. Bonne couverture des cas limites.

---

#### `test/.../AuthResourceTest.java`

**Verdict :** ✅ Tests d'intégration complets avec `@QuarkusTest`.

**Manques identifiés :**

- Pas de test pour le rate limiting (s'il existe)
- Pas de test pour les cookies avec `SameSite` et `Secure`
- Pas de test de timeout sur les endpoints

---

### 3.5 CONFIGURATION

#### `application.properties`

**Lignes 19-21 :**

```properties
quarkus.datasource.username=upkeep
quarkus.datasource.password=upkeep
```

**Verdict :** 🟡 Credentials en dur dans la config par défaut. Devrait être `${DB_USERNAME:upkeep}` pour forcer l'utilisation de variables d'environnement.

**Verdict Global :** Configuration bien organisée avec des profils (dev/test/prod). Bien.

---

#### `checkstyle.xml`

**Verdict :** ✅ Configuration stricte et raisonnable. `AvoidStarImport` est activé. Bien.

---

#### `pom.xml`

**Verdict :** ✅ Dépendances bien gérées, versions centralisées. Pas de conflits visibles.

---

## 4. LA LISTE DES PÉCHÉS CAPITAUX

### 🔴 VIOLATIONS CRITIQUES

| # | Violation | Fichier | Impact |
|---|-----------|---------|--------|
| 1 | Annotations Jakarta dans la couche Application | `*UseCaseImpl.java` | Couplage framework, non-testable en isolation |
| 2 | Bug dans Email.java (normalisation cassée) | `Email.java:23` | Emails non normalisés, duplicates possibles |
| 3 | `catch (Exception e)` fourre-tout | `AuthResource.java:125` | Masque les erreurs, comportement imprévisible |
| 4 | HttpClient non injecté, sans timeout | `GitHubOAuthAdapter.java:45` | Blocage potentiel, non-testable |
| 5 | `IllegalStateException` dans le domaine | Multiple | Exceptions techniques dans le métier |

### 🟡 VIOLATIONS MODÉRÉES

| # | Violation | Fichier | Impact |
|---|-----------|---------|--------|
| 1 | Validation répétée dans use cases (token check) | `CompanyResource.java` | Violation DRY |
| 2 | Couplage au temps système (`Instant.now()`) | Multiple | Tests non-déterministes |
| 3 | GlobalExceptionMapper switch géant | `GlobalExceptionMapper.java` | Violation OCP |
| 4 | Duplication config (expiry seconds) | Auth/JwtTokenService | Violation DRY |
| 5 | Mapper qui valide à la reconstitution | `CustomerMapper.java` | Crash sur données legacy |
| 6 | Incohérence @Inject explicite/implicite | Use cases | Style incohérent |

### ⚪ VIOLATIONS MINEURES

| # | Violation | Fichier |
|---|-----------|---------|
| 1 | Champs publics dans les entités Panache | `*Entity.java` |
| 2 | Credentials en dur (même avec profil) | `application.properties` |
| 3 | Pas de TODO pour le vrai EmailService | `MockEmailService.java` |

---

## 5. ULTIMATUM - ACTIONS IMMÉDIATES

### PRIORITÉ ABSOLUE (Bugs)

1. **CORRIGER `Email.java`** — Le bug de normalisation est silencieux et dangereux. Réécrire avec un constructeur canonique ou une factory :

```java
   public record Email(String value) {
       public Email {
           // validation...
       }
       public static Email of(String raw) {
           return new Email(validated(raw.toLowerCase().trim()));
       }
   }
```

2. **Remplacer `catch (Exception e)`** — Utiliser une exception spécifique ou au minimum logger l'exception originale avant de la transformer.

### PRIORITÉ HAUTE (Architecture)

3. **Extraire les annotations Jakarta des use cases** — Créer des décorateurs transactionnels dans l'infrastructure :

```java
   // Dans infrastructure
   @ApplicationScoped
   @Transactional
   public class TransactionalRegisterCustomerUseCase implements RegisterCustomerUseCase {
       @Inject RegisterCustomerUseCaseImpl delegate;
       public RegisterResult execute(RegisterCommand cmd) { return delegate.execute(cmd); }
   }
```

4. **Centraliser l'authentification** — Implémenter un `ContainerRequestFilter` pour éviter la duplication du code de validation de token dans chaque endpoint.

5. **Injecter `HttpClient` dans `GitHubOAuthAdapter`** — Configurer des timeouts :

```java
   HttpClient.newBuilder()
       .connectTimeout(Duration.ofSeconds(10))
       .build();
```

### PRIORITÉ MOYENNE (Qualité)

6. **Remplacer `IllegalStateException` par des exceptions métier** — Créer `InvitationCannotBeAcceptedException`, `InconsistentOAuthStateException`, etc.

7. **Injecter `Clock` pour le temps** — Tous les `Instant.now()` devraient utiliser un `Clock` injectable :

```java
   private final Clock clock;
   // ...
   Instant now = clock.instant();
```

8. **Créer `Email.reconstitute(String)` et `Password.reconstitute(String)`** — Pour la reconstitution depuis la base sans revalidation.

9. **Refactorer `GlobalExceptionMapper`** — Utiliser une `Map<Class<? extends DomainException>, ExceptionHandler>` pour l'extensibilité.

### PRIORITÉ BASSE (Hygiène)

10. **Unifier le style d'injection** — Soit `@Inject` partout, soit injection par constructeur implicite partout.

11. **Extraire la configuration dans des objets dédiés** — `CookieConfiguration`, `JwtConfiguration`, etc.

12. **Ajouter les tests manquants** — Concurrence, normalisation email, timeouts.

---

## CONCLUSION

Ce projet a les fondations d'une bonne architecture hexagonale, mais l'exécution souffre de compromis trop nombreux. Le bug dans `Email.java` est particulièrement préoccupant car il passe inaperçu à tous les tests.

La pollution de la couche application par Jakarta est la violation la plus systémique. Quarkus rend cette pratique facile, mais facile ne veut pas dire correct.

**Ce code peut aller en production, mais chaque compromis aujourd'hui deviendra une dette technique demain.**

*L'Architecte Draconien a parlé.*

---

> *"Un code propre n'est pas celui qui fonctionne. C'est celui qui communique son intention avec clarté et qui résiste au changement avec grâce."*

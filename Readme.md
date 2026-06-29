# Auth Service

Authentication and authorization microservice for the Shopwire platform. Centralizes identity management, JWT token
lifecycle, RBAC, email/phone verification, and audit trails for all downstream services.

---

## Tech Stack

- **Java 25 + Spring Boot 4**
- **Spring Security** — stateless, cookie-based JWT (RS256 via `nimbus-jose-jwt`)
- **PostgreSQL** — read/write datasource routing via AOP (`@ReadOnly` / `@WriteOnly`)
- **Liquibase** — versioned schema migrations
- **Hibernate Envers** — full entity audit trail
- **Spring Mail** — async email dispatch
- **Spring Cloud** — optional Eureka, Config Server, HashiCorp Vault integration

---

## Architecture

```
AuthController       /api/v1/auth           (login, logout, refresh, forgot/reset-password)
UserController       /api/v1/user           (register, verify-email, verify-phone)
EntitlementController /api/v1/entitlements  (roles & permissions CRUD — ADMIN only)

JwtProvider (RS256)   ←→  TokenBlacklistService (JTI-based, in-memory)
CookieService         ←→  EmailService (async SMTP)
OtpService (in-mem)   ←→  EntitlementService

DB Routing AOP:
  @ReadOnly  → replica datasource
  @WriteOnly → primary datasource
```

---

## Database Schemas

| Schema         | Purpose                                                        |
|----------------|----------------------------------------------------------------|
| `user_auth`    | Users, roles, permissions, login attempts, verification tokens |
| `user_audit`   | Hibernate Envers revision tables (`*_AUD`)                     |
| `user_session` | User session records                                           |
| `user_oauth`   | OAuth2 provider data                                           |

Migrations run automatically on startup via Liquibase (`classpath:db/changelog-master.xml`).

---

## Security Model

| Control                | Implementation                                    |
|------------------------|---------------------------------------------------|
| Token signing          | RS256 (RSA-2048)                                  |
| Token storage          | `HttpOnly` cookies — not accessible to JavaScript |
| Token revocation       | JTI blacklist (evicted at token expiry)           |
| Password hashing       | BCrypt                                            |
| Session                | Stateless                                         |
| CSRF                   | Disabled (stateless JWT)                          |
| Security headers       | CSP, `X-Frame-Options: DENY`, HSTS                |
| Brute-force protection | Login attempt tracking with timed account lockout |

---

## Running Locally

**Prerequisites:** Java 25, Maven 3.9+, PostgreSQL 16

```bash
# Start a local PostgreSQL instance
docker run -d --name auth-db \
  -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=user_db \
  -p 5432:5432 postgres:16

# Run the service (dev profile)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Service: `http://localhost:8080`
Actuator: `http://localhost:8081/actuator`

Configuration is in `src/main/resources/application-dev.yaml`. Key properties to set locally: datasource
URLs/credentials, SMTP credentials, and RSA keypair (`jwt-private-key` / `jwt-public-key`).

---

## Build

```bash
./mvnw verify                  # compile, test, JaCoCo coverage report
./mvnw package                 # also builds Docker image
./mvnw sonar:sonar             # SonarQube analysis
```

OWASP dependency-check runs on `verify` and fails the build on any CVE with CVSS ≥ 7.

---

## License
 
MIT LICENSE
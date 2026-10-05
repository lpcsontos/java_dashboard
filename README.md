# Dashboard backend (Spring Boot)

The backend of a personal dashboard application, written in Java with Spring Boot. The part that is finished is the authentication layer: registration with email verification, login with JWT access tokens and refresh tokens, and role-based access control. The dashboard features themselves are still to come.

> \*\*Status:\*\* work in progress. Authentication works end to end locally; the rest of the application is not built yet.

## Features

* **Registration with email verification.** New accounts are disabled until the user clicks the link in the verification email. Verification tokens expire after 24 hours and can be re-sent.
* **JWT authentication.** Login returns a short-lived access token (HMAC-SHA256, 60 minutes by default) and a refresh token. A custom filter validates the `Authorization: Bearer …` header on every request, and the API is fully stateless.
* **Refresh tokens in Redis** with a 7-day lifetime, so they can be revoked on logout. They are indexed in both directions (user → token and token → user), so every lookup is a single key read instead of a scan.
* **Password hashing** with BCrypt, request validation with Jakarta Bean Validation, and consistent JSON error responses from a global exception handler.
* **Role-based access control:** roles (`ROLE\_USER`, `ROLE\_ADMIN`) are stored per user, and the `/api1/admin/\*\*` endpoints are restricted to admins.
* **API documentation** with Swagger UI (springdoc-openapi).
* **Tests:** unit tests with Mockito, controller tests with MockMvc, and integration tests that run against real PostgreSQL and Redis containers using Testcontainers. The test suite was written with the help of an AI assistant and reviewed by me.

## Tech stack

Java 21 · Spring Boot 3.5 · Spring Security · Spring Data JPA · PostgreSQL · Redis · Spring Mail · Docker Compose · JUnit 5 · Mockito · Testcontainers · Gradle (Kotlin DSL)

## API

All endpoints are under `/api1`.

|Method|Endpoint|Auth|Description|
|-|-|-|-|
|POST|`/auth/register`|–|Create an account and send a verification email|
|GET|`/auth/verify?token=…`|–|Activate the account|
|POST|`/auth/resend-verification`|–|Send a new verification email|
|POST|`/auth/login`|–|Returns `accessToken` and `refreshToken`|
|POST|`/auth/refresh`|–|Get a new access token with a refresh token|
|POST|`/auth/logout`|–|Revoke the refresh token|
|GET|`/me`|Bearer token|Returns a greeting with the signed-in user's email|

## Project structure

```
backend/src/main/java/dev/lpcsontos/dashboard/
├── config/          security, Redis cache and global error handling
└── modules/
    ├── auth/        controller, services (auth, JWT, refresh tokens), JWT filter, DTOs
    └── user/        user entity, repository, UserDetailsService, /me and admin endpoints
```

## Running locally

Requirements: JDK 21 and Docker.

1. Create the environment file and fill in the values:

```
   cd backend
   cp your.env.example .env
   ```

   `JWT\_SECRET` should be a long random string (at least 32 characters).

2. Start the application:

```
   ./gradlew bootRun
   ```

   Spring Boot's Docker Compose support starts PostgreSQL, Redis and MailHog from `compose.yaml` automatically.

3. Open:

   * Swagger UI: http://localhost:8080/swagger-ui.html
   * MailHog (the verification emails land here instead of a real inbox): http://localhost:8025

Run the tests with `./gradlew test` (Docker must be running for the integration tests).

## Planned

* Google sign-in (the user entity already has a `provider` field for it)
* More admin features for managing users
* The dashboard features themselves


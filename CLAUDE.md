# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Run a single test method
./mvnw test -Dtest=ClassName#methodName
```

## Architecture

**bowlingbrain** is a bowling score tracker REST API built with:

- **Java 25 / Spring Boot 4.0.5** — MVC servlet stack
- **Spring HATEOAS** — hypermedia-driven REST responses
- **MongoDB** — document storage (no normalization required for the nested frame/roll structure)
- **Planned:** OpenAPI Generator contract-first approach, Keycloak auth, Angular 21 frontend, Playwright/Cucumber E2E tests

## Coding guidelines

* Follow the clean code pattern
* Methods should be short max. 20 lines
* Method naming should be clear
* No comments -> use method naming to explain code
* Always write tests for every edge case
* Program following the domain driven pattern

### Domain model

The core entities (to be implemented under `de.europace.bowlingbrain`):


| Entity   | Key fields                                                  | Notes                           |
| -------- | ----------------------------------------------------------- | ------------------------------- |
| `Roll`   | `smashedPins` (1–9), `strike` (boolean)                    | Leaf node                       |
| `Frame`  | `rolls` (max 2, last frame max 3), `prevFrame`, `nextFrame` | Doubly-linked                   |
| `Player` | `name`, `frames` (max 10)                                   |                                 |
| `Game`   | `players`, `currentPlayer`                                  | Root aggregate saved to MongoDB |

### Package conventions

All production code lives under `de.europace.bowlingbrain`. Follow standard Spring layering: `controller` → `service` → `repository` (Spring Data MongoDB).

### Testing strategy

Use `spring-boot-starter-mongodb-test` (embedded MongoDB) for repository/integration tests and `spring-boot-starter-webmvc-test` (MockMvc) for controller-layer tests. Test against the embedded MongoDB instance rather than mocking it so schema and query issues surface early.

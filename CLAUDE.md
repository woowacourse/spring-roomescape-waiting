# Spring Roomescape Waiting — Project Notes

See global guidelines at `~/.claude/CLAUDE.md` (loaded automatically).

## Stack
- Spring Boot 3.4.4, Java 21, H2 (in-memory), JDBC (no JPA)
- Validation: `spring-boot-starter-validation`

## Key Conventions
- No JPA — all DB access via `JdbcTemplate` / `SimpleJdbcInsert`
- Domain objects are immutable value objects (no setters)
- Controllers return `ResponseEntity`; error responses follow RFC 9457 ProblemDetail (`ProblemDetailsAdvice`)
- `SessionService` is the orchestration layer; `ReservationService` / `WaitingService` are domain-scoped

## Endpoints Summary
- `GET/POST/PUT/PATCH/DELETE /themes`
- `GET/POST/PUT/PATCH/DELETE /times`
- `GET/POST/PUT/PATCH/DELETE /reservations`
- `GET/POST /sessions`, `POST /sessions/batch`
- `POST/DELETE /waitings`

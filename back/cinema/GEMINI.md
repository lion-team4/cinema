# Cinema Project Context (GEMINI.md)

## 1. Project Overview
**Name:** Cinema
**Description:** A Spring Boot-based backend application for a cinema/video streaming platform.
**Last Updated:** 2026-01-13 (Tuesday)

**Current Status:**
*   **Entities:** Core entities implemented. **`Payment` is Toss-compliant.**
    *   *Action Required:* `Settlement` (Account info missing), `WatchHistory` (Duration missing).
*   **DTOs:** Request/Response DTOs largely implemented in `src/main/java/com/example/cinema/dto`.
*   **Infrastructure:** Database connection, `QueryDslConfig`, `TossPaymentConfig` set up.
*   **Security:** `spring-boot-starter-security` present, but **JWT libraries (jjwt) missing** in `build.gradle`. `SecurityConfig` and Auth Logic not implemented.
*   **Layers:** Repository, Service, Controller layers are currently **Empty**.

## 2. Technical Stack
*   **Language:** Java 21
*   **Framework:** Spring Boot 3.5.9
*   **Build Tool:** Gradle
*   **Database:** MySQL (`cinema-db`)
*   **ORM:** JPA + Hibernate (`ddl-auto: update`)
*   **Query:** QueryDSL 5.0.0
*   **Docs:** SpringDoc OpenAPI (Swagger) v2.8.6
*   **Key Dependencies:**
    *   `spring-boot-starter-web`, `security`, `batch`, `validation`, `lombok`
    *   **MISSING:** `io.jsonwebtoken:jjwt-api` (and impl/jackson)

## 3. Project Structure & Status
`src/main/java/com/example/cinema`:
*   `entity/`: **Implemented (90%)**
    *   `Payment`: ✅ Toss PG fields (`orderId`, `failReason`) added.
    *   `BillingKey`: ✅ ready.
    *   `Settlement`: ⚠️ Needs Bank Account info.
    *   `WatchHistory`: ⚠️ Needs `exitedAt` or `duration`.
*   `dto/`: **Implemented** (Structure ready)
*   `config/`: `QueryDslConfig`, `TossPaymentConfig` ✅.
*   `repository/`: **Empty** (Next Step)
*   `service/`: **Empty**
*   `controller/`: **Empty**
*   `security/`: **Empty** (Critical)

## 4. Analysis & Action Items

### 🚨 Critical Gaps (Immediate Actions)
1.  **Entity Completion**:
    *   Create `SettlementAccount` entity (or add fields to `User`) for payouts.
    *   Add `watchTime` or `exitedAt` to `WatchHistory` for "20% view" calculation.
2.  **Dependencies**:
    *   Add JWT libraries to `build.gradle`.
3.  **Security Layer**:
    *   Implement `JwtTokenProvider`, `JwtAuthenticationFilter`, `SecurityConfig`.

### 📅 Implementation Roadmap
1.  **Phase 1: Foundation (Current)**
    *   [x] Basic Entity Design
    *   [ ] Fix Entity Gaps (`Settlement`, `WatchHistory`)
    *   [ ] Repository Layer Implementation (JPA + QueryDSL)

2.  **Phase 2: Core Logic (Security & User)**
    *   [ ] Security Configuration (JWT)
    *   [ ] User Service (Sign-up, Login, Profile)

3.  **Phase 3: Features**
    *   [ ] Content Service (Upload, Manage)
    *   [ ] Schedule Service
    *   [ ] Payment/Subscription (Toss Integration)

4.  **Phase 4: Interface**
    *   [ ] REST Controllers
    *   [ ] Swagger Verification

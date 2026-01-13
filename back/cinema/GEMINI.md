# Cinema Project Context (GEMINI.md)

## 1. Project Overview
**Name:** Cinema
**Description:** A Spring Boot-based backend application for a cinema/video streaming platform.
**Current Status (As of 2026-01-13):**
*   **Entities:** Core entities implemented matching `ENTITY.md`.
*   **DTOs:** Request/Response DTOs are largely implemented in `src/main/java/com/example/cinema/dto`.
*   **Infrastructure:** Database connection and basic Config (`QueryDsl`, `TossPayment`) are set up.
*   **Critical Missing Components:**
    *   **Security:** No `SecurityConfig`, JWT utilities, or Authentication filters.
    *   **Layers:** `Repository`, `Service`, and `Controller` packages are **empty**.
    *   **Business Logic:** No features (User, Content, Schedule) are actually functional yet.

**Key Features (Planned):**
*   **User Management:** Authentication (JWT), Profiles.
*   **Content Management:** Video uploads (S3), HLS streaming, Metadata.
*   **Cinema Scheduling:** Managing screening schedules (Coming Up, Happening Now).
*   **Interactive Features:** Reviews, Watch History, Live Theater Entry/Exit.
*   **Monetization:** Subscriptions, Payments (Toss), and Creator Settlements.

## 2. Technical Stack
*   **Language:** Java 21
*   **Framework:** Spring Boot 3.5.9
*   **Build Tool:** Gradle
*   **Database:** MySQL (configured as `cinema-db` on `localhost:13306`)
*   **ORM:** Spring Data JPA + Hibernate (`ddl-auto: update`)
*   **Query Framework:** QueryDSL 5.0.0
*   **API Documentation:** SpringDoc OpenAPI (Swagger) v2.8.6
*   **Key Libraries:**
    *   `spring-boot-starter-web`
    *   `spring-boot-starter-security`
    *   `spring-boot-starter-batch`
    *   `spring-boot-starter-validation`
    *   `lombok`

## 3. Project Structure
`src/main/java/com/example/cinema`:
*   `CinemaApplication.java`
*   `entity/`: **Implemented**. (`User`, `Content`, `Schedule`, `Subscription`, etc.)
*   `dto/`: **Implemented**. (`ContentSearchRequest`, `UserDetailResponse`, etc.)
*   `type/`: **Implemented**. Enums for status/types.
*   `config/`: `QueryDslConfig`, `TossPaymentConfig`. **Missing:** `SecurityConfig`.
*   `repository/`: **Empty**.
*   `service/`: **Empty**.
*   `controller/`: **Empty**.
*   `security/`: **Missing**. (Need `JwtTokenProvider`, `SecurityConfig`, `CustomUserDetailService`).

## 4. Documentation References
*   **`ENTITY.md`**: **CRITICAL** - Detailed database schema and rules.
*   **`DTO_PLAN.md`** & **`DTO_TODO.md`**: DTO planning docs.
*   **`TOSS_PAYMENT_PLAN.md`**: Payment integration guide.

## 5. Analysis Report & Roadmap
### Priority 1: Infrastructure & Security
*   **Task:** Implement `SecurityConfig` and JWT infrastructure (`JwtTokenProvider`, `JwtAuthenticationFilter`).
*   **Rationale:** "User Management" is the foundation for all other features (Content ownership, Subscriptions).

### Priority 2: Data Access Layer (Repository)
*   **Task:** Create Repository interfaces for all entities (`UserRepository`, `ContentRepository`, etc.).
*   **Task:** Implement `CustomRepository` interfaces for QueryDSL queries (e.g., `ContentRepositoryCustom`).

### Priority 3: Business Logic (Service)
*   **Task:** Implement `UserService` (Signup, Login, Profile).
*   **Task:** Implement `ContentService` (Upload, List, Detail).
*   **Task:** Implement `ScheduleService` (Manage schedules).

### Priority 4: API Layer (Controller)
*   **Task:** Create REST Controllers using standard `ApiResponse` wrapper.
*   **Task:** Verify with Swagger UI.
# Cinema Project Context (GEMINI.md)

## 1. Project Overview
**Name:** Cinema
**Description:** A Spring Boot-based backend application for a cinema/video streaming platform.
**Last Updated:** 2026-01-14 (Wednesday)

**Current Status:**
*   **Security:** **Implemented**. JWT infrastructure (`JwtTokenProvider`, `JwtAuthenticationFilter`, `ProjectSecurityConfig`) is fully operational.
*   **User Module:** **Implemented**. `UserService` handles Signup, Login (JWT), Profile, and Token Reissue.
*   **Subscription Module:** **Implemented**. `SubscriptionService` supports Plan Creation, Billing Key management, Recurring Payments (Toss), and History.
*   **Billing Module:** **Refactored**. `BillingController` uses `SubscriptionService` and new `BillingRequest`/`BillingResponse` DTOs.
*   **Content Module:** **Scaffolded but Empty**. `ContentController` and `ContentService` files exist but contain no logic. `ContentController` has an incorrect annotation.
*   **Schedule Module:** **Pending Implementation**. Entities (`ScheduleItem`, `ScheduleDay`) and DTOs exist, but `ScheduleService`, `ScheduleController`, and `ScheduleRepository` are **missing**.
*   **Infrastructure:** Database (MySQL), QueryDSL, Swagger, and Toss Payment configuration are ready.

## 2. Technical Stack
*   **Language:** Java 21
*   **Framework:** Spring Boot 3.5.9
*   **Build Tool:** Gradle
*   **Database:** MySQL (`cinema-db`)
*   **ORM:** JPA + Hibernate
*   **Query:** QueryDSL 5.0.0
*   **Docs:** SpringDoc OpenAPI (Swagger) v2.8.6
*   **Security:** Spring Security + JWT
*   **Payment:** Toss Payments

## 3. Project Structure & Status
`src/main/java/com/example/cinema`:
*   `config/`:
    *   `auth/`: ✅ `ProjectSecurityConfig`, `JwtTokenProvider`, `JwtAuthenticationFilter`.
    *   `TossPaymentConfig`, `QueryDslConfig`: ✅ Ready.
*   `controller/`:
    *   `auth/AuthController`: ✅ Implemented.
    *   `user/UserController`: ✅ Implemented.
    *   `subscription/SubscriptionController`: ✅ Implemented.
    *   `ContentController`: ⚠️ **Bug/Empty**. Exists but has no methods and incorrect `@RestController("/api")` annotation.
    *   `test/BillingController`: ✅ Refactored to use `SubscriptionService`.
*   `service/`:
    *   `user/UserService`: ✅ Implemented (Signup, Login, Profile, Reissue, Delete).
    *   `subscription/SubscriptionService`: ✅ Implemented (Toss Payment Integration, Billing Key, Recurring).
    *   `contentService/ContentService`: ❌ **Empty** (Class exists but no logic).
    *   `schedule/ScheduleService`: ❌ **Missing** (Does not exist).
*   `repository/`: ✅ Repositories created for User, Subscription, Content, Payment, etc. **Missing** `ScheduleRepository`.
*   `entity/`: ✅ Implemented (`User`, `Subscription`, `Content`, `ScheduleItem`, `Settlement`, etc.).
*   `dto/`:
    *   `billing/`: ✅ `BillingRequest`, `BillingResponse` (New).
    *   `auth`, `common`, `content`, `schedule`, `settlement`, `subscription`, `theater`, `user`: ✅ Defined.
*   `exception/`: ❌ **Empty**. Global exception handling is missing.

## 4. Analysis & Action Items

### 📊 Project Health Check (2026-01-14)
*   **Code Quality**:
    *   ⚠️ **Exception Handling**: The `com.example.cinema.exception` package is empty. No global `@ControllerAdvice` exists. Exceptions will return raw 500 errors to clients.
    *   ⚠️ **Test Endpoints**: `BillingController` (test) is exposed publicly (`permitAll` in `ProjectSecurityConfig`). This allows unauthenticated users to trigger billing logic. Must be secured or removed in production.
    *   ✅ **DTO Usage**: New `BillingRequest`/`BillingResponse` DTOs improve type safety and documentation for billing operations.
*   **Architecture**:
    *   ✅ **Layered Architecture**: Clear separation of Controller/Service/Repository.
    *   ✅ **Security**: JWT-based auth is correctly configured for most endpoints.
    *   ❌ **Missing Logic**: Content and Schedule modules are purely skeletal.

### 🚨 Critical Gaps (Immediate Actions)
1.  **Content Service Implementation**:
    *   **Fix**: `ContentController` annotation (`@RequestMapping` needed).
    *   **Implement**: `ContentService` methods: `createContent`, `getContent` (Detail), `searchContent` (QueryDSL), `updateContent`, `deleteContent`.
    *   **Expose**: `ContentController` endpoints.
    *   **Link**: Handle `MediaAsset` linking for content files/images.
2.  **Schedule Service Implementation**:
    *   **Create**: `ScheduleRepository`, `ScheduleService`, `ScheduleController`.
    *   **Logic**: Manage cinema schedules (`ScheduleItem`, `ScheduleDay`), check conflicts, retrieve by date/theater.
3.  **Global Exception Handling**:
    *   **Implement**: `GlobalExceptionHandler` with `@RestControllerAdvice`.
    *   **Define**: Custom Exception classes (`BusinessException`, `EntityNotFoundException`, etc.) and `ErrorResponse` DTO.

### 📅 Implementation Roadmap
1.  **Phase 1: Foundation & User (Completed)**
    *   [x] Entity Design
    *   [x] Repository Layer
    *   [x] Security (JWT)
    *   [x] User Service (Auth/Profile)

2.  **Phase 2: Commerce & Subscription (Completed)**
    *   [x] Toss Payment Integration (Billing Key, Recurring)
    *   [x] Subscription Service logic
    *   [x] Refactor Test Controllers to use Real Service
    *   [x] Create Billing DTOs

3.  **Phase 3: Core Content & Schedule (Current Priority)**
    *   [ ] **Fix ContentController & Implement ContentService**
    *   [ ] **Create & Implement Schedule Module**
    *   [ ] **Implement Global Exception Handling**
    *   [ ] Review System (Create/List reviews)

4.  **Phase 4: Advanced Features & Cleanup**
    *   [ ] Watch History Tracking
    *   [ ] Settlement Processing (Batch/Admin)
    *   [ ] **Security Hardening**: Remove/Secure `/test/**` endpoints.
    *   [ ] API Documentation (Swagger) Validation

## 5. Comprehensive Analysis Report (2026-01-14)

### 5.1 Overview
The project is in a **mid-development state**. The core authentication and subscription/payment infrastructure is well-established and operational. However, the core business logic related to the cinema domain (Movies, Schedules, Theaters) is largely missing or skeletal.

### 5.2 Key Findings
1.  **Payment & Subscription**: 
    *   Successfully integrated with Toss Payments (Billing Key issuance, Recurring Payments).
    *   Logic is robust, using proper entities (`Subscription`, `Payment`, `BillingKey`) and transactional services.
    *   Test infrastructure (`BillingController`) works but should be restricted in production.
2.  **Entity Relations**:
    *   Good use of JPA (`@OneToOne`, `@ManyToOne`) and `BaseEntity` for auditing.
    *   `User` entity is properly designed with lazy loading.
3.  **Architecture Gaps**:
    *   **Empty Modules**: `Content` and `Schedule` are critical for a Cinema app but are currently just placeholders. This is the biggest blocker for functional MVP.
    *   **Error Handling**: Complete lack of custom exception handling means debugging client-side errors will be difficult, and API responses will be inconsistent on failure.

### 5.3 Recommendations
*   **Priority 1 (Content)**: Implement `ContentService` immediately. Without movies/content, the schedule and reservation systems cannot be built.
*   **Priority 2 (Schedule)**: Build the `Schedule` module next. This links `Content` to `Theater`.
*   **Priority 3 (Exceptions)**: Implement a `GlobalExceptionHandler` to standardize API error responses (e.g., standard JSON error body instead of HTML stack traces).
*   **Security**: Ensure `BillingController` is eventually disabled or secured with `ROLE_ADMIN` to prevent payment abuse.
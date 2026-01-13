# Cinema Project Context (GEMINI.md)

## 1. Project Overview
**Name:** Cinema
**Description:** A Spring Boot-based backend application for a cinema/video streaming platform.
**Last Updated:** 2026-01-13 (Tuesday)

**Current Status:**
*   **Security:** **Implemented**. JWT infrastructure (`JwtTokenProvider`, `JwtAuthenticationFilter`, `ProjectSecurityConfig`) is fully operational.
*   **User Module:** **Implemented**. `UserService` handles Signup, Login (JWT), Profile, and Token Reissue.
*   **Subscription Module:** **Implemented**. `SubscriptionService` supports Plan Creation, Billing Key management, Recurring Payments (Toss), and History.
*   **Content Module:** **Pending Logic**. `ContentController` and `ContentService` classes exist but are **empty shells**.
*   **Schedule Module:** **Pending Logic**. Entities (`ScheduleItem`, `ScheduleDay`) and DTOs exist, but `ScheduleService` is **missing**.
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
    *   `ContentController`: ⚠️ **Empty** (Exists but has no methods).
*   `service/`:
    *   `user/UserService`: ✅ Implemented (Signup, Login, Profile, Reissue, Delete).
    *   `subscription/SubscriptionService`: ✅ Implemented (Toss Payment Integration, Billing Key, Recurring).
    *   `contentService/ContentService`: ❌ **Empty** (Class exists but no logic).
    *   `schedule/ScheduleService`: ❌ **Missing** (Does not exist).
*   `repository/`: ✅ Repositories created for most entities (`UserRepository`, `SubscriptionRepository`, etc.).
*   `entity/`: ✅ Implemented (`User`, `Subscription`, `Content`, `ScheduleItem`, `Settlement`, etc.).
*   `dto/`: ✅ Request/Response DTOs prepared for most modules (including Schedule).

## 4. Analysis & Action Items

### 🚨 Critical Gaps (Immediate Actions)
1.  **Content Service Implementation**:
    *   Implement `ContentService` methods: `createContent`, `getContent` (Detail), `searchContent` (QueryDSL), `updateContent`, `deleteContent`.
    *   Implement `ContentController` to expose these endpoints.
    *   Handle `MediaAsset` linking for content files/images.
2.  **Schedule Service Implementation**:
    *   Create `ScheduleService` to manage cinema schedules (`ScheduleItem`, `ScheduleDay`).
    *   Implement logic to check for time conflicts and retrieve schedules by date/theater.
3.  **Settlement & WatchHistory**:
    *   Implement logic to track `WatchHistory` (when a user watches content).
    *   Implement `Settlement` logic (calculating creator revenue based on views).

### 📅 Implementation Roadmap
1.  **Phase 1: Foundation & User (Completed)**
    *   [x] Entity Design
    *   [x] Repository Layer
    *   [x] Security (JWT)
    *   [x] User Service (Auth/Profile)

2.  **Phase 2: Commerce & Subscription (Completed)**
    *   [x] Toss Payment Integration (Billing Key, Recurring)
    *   [x] Subscription Service logic

3.  **Phase 3: Core Content & Schedule (Current Priority)**
    *   [ ] **Content Service Implementation** (Create/Read/Update/Delete, Search)
    *   [ ] **Schedule Service Implementation** (Manage screening times)
    *   [ ] Review System (Create/List reviews)

4.  **Phase 4: Advanced Features**
    *   [ ] Watch History Tracking
    *   [ ] Settlement Processing (Batch/Admin)
    *   [ ] Integration Tests
    *   [ ] API Documentation (Swagger) Validation

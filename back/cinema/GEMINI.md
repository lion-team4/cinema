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
*   **Content Module:** **Implemented (Partial)**. 
    *   **Repository**: ✅ Optimized (`getTagsByContentId`) & QueryDSL Implemented.
    *   **Service**: ✅ `ContentService` implemented (Search logic). Package refactored to `service.content`.
    *   **Controller**: ⚠️ **Bug**. `ContentController` exists but uses `@RestController("/contents")` (Bean Name) instead of `@RequestMapping("/contents")`. This causes endpoints to be mapped to root `/`. Duplicate file was removed.
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
    *   `content/ContentController`: ⚠️ **Annotation Bug**. `@RestController("/contents")` -> Needs `@RequestMapping("/contents")`.
    *   `test/BillingController`: ✅ Refactored to use `SubscriptionService`.
*   `service/`:
    *   `user/UserService`: ✅ Implemented.
    *   `subscription/SubscriptionService`: ✅ Implemented.
    *   `content/ContentService`: ✅ Implemented (Search).
    *   `schedule/ScheduleService`: ❌ **Missing**.
*   `repository/`: 
    *   `content/ContentRepository`: ✅ Optimized.
    *   `content/custom/ContentRepositoryImpl`: ✅ Implemented.
    *   **Missing** `ScheduleRepository`.
*   `entity/`: ✅ Implemented (`User`, `Subscription`, `Content`, `ScheduleItem`, `Settlement`, etc.).
*   `dto/`:
    *   `billing/`: ✅ `BillingRequest`, `BillingResponse` (New).
    *   `auth`, `common`, `content`, `schedule`, `settlement`, `subscription`, `theater`, `user`: ✅ Defined.
*   `exception/`: ❌ **Empty**. Global exception handling is missing.

## 4. Analysis & Action Items

### 📊 Project Health Check (2026-01-14)
*   **Code Quality**:
    *   ✅ **Duplicate Removed**: The conflicting `ContentController` in the parent package has been deleted.
    *   ✅ **Refactoring**: `ContentService` package renamed to `content` (from `contentService`), adhering to naming conventions.
    *   ⚠️ **Annotation Error**: `ContentController`'s `@RestController("/contents")` is semantically incorrect for URL mapping. It sets the bean name, not the path.
    *   ⚠️ **Exception Handling**: No global error handling yet.
*   **Architecture**:
    *   ✅ **Layered Architecture**: Controller -> Service -> Repository flow is now established for Content Search.
    *   ❌ **Missing Logic**: Schedule module is the next major block.

### 🚨 Critical Gaps (Immediate Actions)
1.  **Fix ContentController Annotation**:
    *   **Action**: Change `@RestController("/contents")` to `@RestController` + `@RequestMapping("/contents")`.
2.  **Schedule Service Implementation**:
    *   **Create**: `ScheduleRepository`, `ScheduleService`, `ScheduleController`.
    *   **Logic**: Manage cinema schedules (`ScheduleItem`, `ScheduleDay`).
3.  **Global Exception Handling**:
    *   **Implement**: `GlobalExceptionHandler` with `@RestControllerAdvice`.

### 📅 Implementation Roadmap
1.  **Phase 1: Foundation & User (Completed)**
    *   [x] Entity Design
    *   [x] Repository Layer
    *   [x] Security (JWT)
    *   [x] User Service (Auth/Profile)

2.  **Phase 2: Commerce & Subscription (Completed)**
    *   [x] Toss Payment Integration
    *   [x] Subscription Service logic

3.  **Phase 3: Core Content & Schedule (Current Priority)**
    *   [x] Content Repository & Test Data
    *   [x] Content Service (Search)
    *   [ ] **Fix ContentController Mapping**
    *   [ ] **Create & Implement Schedule Module**
    *   [ ] **Implement Global Exception Handling**

4.  **Phase 4: Advanced Features & Cleanup**
    *   [ ] Watch History Tracking
    *   [ ] Settlement Processing
    *   [ ] API Documentation (Swagger) Validation

## 5. Comprehensive Analysis Report (2026-01-14)

### 5.1 Overview
The **Content Module** is taking shape. The duplicate controller issue is resolved, and the service layer is implemented. However, a minor but functional bug exists in the Controller annotation which will misroute API requests.

### 5.2 Key Findings
1.  **Content Module**:
    *   **Progress**: Service and Repository layers are connected.
    *   **Bug**: `ContentController` mapping is incorrect (`@RestController("/contents")`). This means `GET /api/contents` (or similar) won't work; it will likely listen at `GET /`.
2.  **Refactoring**:
    *   The developer has cleaned up package names (`contentService` -> `content`) and removed the duplicate file.

### 5.3 Recommendations
*   **Immediate Fix**: Correct the `ContentController` annotation to `@RequestMapping("/contents")`.
*   **Next Feature**: Start the **Schedule Module**. This is the last major missing piece for the MVP.
*   **Technical Debt**: Add `GlobalExceptionHandler` to prevent raw stack traces from leaking to clients.
# Project Context: Cinema Streaming Backend

## Project Overview
This is a **Spring Boot-based backend application** for a **Cinema Streaming Service**. The platform allows users to subscribe, watch movie contents, and participate in synchronized "Theater" viewings where multiple users watch the same content simultaneously.

It handles **Content Management** (Video upload/processing), **Streaming** (HLS via AWS S3 & CloudFront), **Subscriptions/Payments** (Toss Payments), **Scheduling** (Movie time slots), and **Real-time Synchronization** (WebSocket/STOMP) for theater experiences.

## Key Technologies
- **Language:** Java 21
- **Framework:** Spring Boot 3.5.9
- **Build Tool:** Gradle
- **Database:** MySQL
- **ORM:** Spring Data JPA, QueryDSL 5.0
- **Security:** Spring Security, JWT
- **Cloud/Infra:** AWS S3 (Storage), AWS CloudFront (CDN), Docker (FFmpeg execution)
- **Payments:** Toss Payments API
- **Documentation:** Swagger/OpenAPI (SpringDoc)

## Architecture & Core Modules

### 1. Streaming Architecture
- **Upload:** Direct S3 upload via Presigned URLs.
- **Processing:** Asynchronous background jobs using Dockerized FFmpeg to transcode videos into HLS format (`.m3u8` + `.ts` segments).
- **Delivery:** Content served via AWS CloudFront CDN.
- **Playback:** HLS streaming for adaptive bitrate.

### 2. Theater (Synchronized Viewing)
- **Concept:** Users enter a virtual "Theater" for a specific `Schedule`.
- **Sync:** Server-controlled playback. No user pause/seek.
- **Technology:** WebSocket (STOMP) pushes `PlaybackState` (playing status, current position) calculated based on the schedule's `startAt` time.

### 3. Payment & Subscription
- **Provider:** Toss Payments.
- **Model:** Monthly subscription.
- **Flow:** Card registration (Billing Key) -> Recurring payment execution.
- **Settlement:** Monthly settlement calculation based on effective view counts.

## Key Directories & Files
- `src/main/java/com/example/cinema/`
    - `api/`: Controllers for specific asset handling.
    - `config/`: Configuration classes (Security, AWS, QueryDSL, Scheduler).
    - `controller/`: REST Controllers organized by domain (`auth`, `content`, `schedule`, `theater`, `subscription`).
    - `dto/`: Data Transfer Objects (Request/Response).
    - `entity/`: JPA Entities (`User`, `Content`, `ScheduleItem`, `Subscription`, `Payment`, `Settlement`).
    - `exception/`: Global exception handling (`BusinessException`, `GlobalExceptionHandler`).
    - `infra/`: Infrastructure layers (S3, FFmpeg, Payment clients).
    - `repository/`: JPA Repositories.
    - `service/`: Business logic.
    - `scheduler/`: Scheduled tasks (View count aggregation, Subscription renewal).

## Building & Running
- **Build:** `./gradlew build`
- **Run:** `./gradlew bootRun`
- **Test:** `./gradlew test`

## Development Conventions
- **Response Format:** All API responses are wrapped in `ApiResponse<T>`.
- **Exception Handling:** Use `BusinessException` with `ErrorCode` enum for logic errors.
- **Entity/DTO:** Strict separation between Entity and DTOs.
- **Service Layer:** Transactional business logic.
- **Configuration:** `application.yaml` manages environment-specific settings (Profiles: `test`, `dev`, `prod`).

## Important Documentation Files
- `API.md`: Detailed REST API endpoints.
- `FRS.md`: Functional Requirements Specification.
- `STREAMING_AND_S3_ARCHITECTURE.md`: Deep dive into the streaming setup.
- `TOSS_PAYMENT_PLAN.md`: Payment integration details.

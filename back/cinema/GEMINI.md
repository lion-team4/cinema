# GEMINI.md - Lion Cinema Project Context

## 1. Project Overview
**Name**: Lion Cinema (Back-End)
**Type**: Synchronous Movie Streaming Platform (Subscription-based)
**Core Concept**: Users watch content simultaneously based on a pre-defined schedule ("Theater" experience).
**Key Features**:
- **Sync Playback**: WebSocket-based state synchronization for all viewers.
- **Media Pipeline**: Upload -> FFmpeg Transcoding (HLS) -> S3 -> CloudFront.
- **Subscription**: Recurring payments via Toss Payments (Billing Key).
- **Settlement**: Creator revenue distribution based on views.
- **Review System**: Verifiable reviews based on actual watch history.

## 2. Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.9
- **Build Tool**: Gradle
- **Database**: MySQL (Prod), H2 (Test)
- **ORM**: JPA (Hibernate) + QueryDSL 5.0
- **Security**: Spring Security + JWT
- **Async/Batch**: Spring Batch (Settlements), `@Async` (Encoding)
- **Infrastructure**:
  - **AWS**: S3 (Storage), CloudFront (CDN)
  - **Payment**: Toss Payments (Billing API)
  - **WebSocket**: STOMP (Theater sync)
  - **Transcoding**: FFmpeg (Dockerized or local execution)

## 3. Architecture & Package Structure (`com.example.cinema`)

### Core Packages
- **`api`**: REST Controllers. Grouped by domain (`auth`, `content`, `schedule`, `subscription`, `theater`, `settlement`).
- **`entity`**: JPA Entities. The source of truth.
  - Key Entities: `User`, `Content`, `ScheduleItem`, `Subscription`, `Payment`, `Settlement`, `Review`.
- **`dto`**: Data Transfer Objects (Request/Response). Strictly separated from Entities.
- **`service`**: Business logic.
  - `asset`: S3 key generation, Presigned URLs.
  - `media`: CloudFront URL generation.
  - `encoding`: FFmpeg transcoding orchestration.
  - `theater`: Playback sync (`TheaterSyncService`) and access control (`TheaterPlaybackService`).
  - `settlement`: Revenue calculation and Batch processing.
  - `content`: Content management and Reviews (`ReviewService`).
- **`repository`**: Data access (JPA + QueryDSL).
- **`infra`**: Infrastructure adapters.
  - `s3`: AWS SDK implementation.
  - `ffmpeg`: Transcoding logic.
  - `payment`: Toss Payments client (`RestClient`).
- **`config`**: App configuration (`Security`, `WebSocket`, `Batch`, `S3`, `Toss`).

### Key Workflows

#### A. Content Upload & Encoding
1. **Metadata**: `POST /contents` (Create draft).
2. **Upload**: `POST /api/assets/presign` -> Client uploads raw MP4 to S3.
3. **Completion**: `POST /api/assets/complete` -> Triggers `EncodingJobService`.
4. **Transcoding**: `HlsTranscodeService` downloads MP4 -> FFmpeg (HLS) -> Uploads `.m3u8` & `.ts` to S3.
5. **Publishing**: Content becomes available for scheduling.

#### B. Theater Synchronization
1. **Schedule**: Admins create `ScheduleItem` (Start/End time).
2. **Entry**: Client calls `GET /theaters/{scheduleId}/playback` (Validation + CloudFront URL).
3. **Sync**: Client connects to WebSocket (`/ws`) and subscribes to `/topic/theaters/{id}/state`.
4. **Logic**: `TheaterSyncService` calculates `currentPosition = now - startAt`.
   - **No Seek**: Users cannot control playback; they only receive the calculated position.

#### C. Payment (Toss)
1. **Card Reg**: `TossPaymentClient` issues a **Billing Key** (Auth Key -> Billing Key).
2. **Subscription**: `SubscriptionService` creates record and triggers initial payment.
3. **Recurring**: `Batch` jobs or scheduled tasks trigger subsequent payments using Billing Key.

#### D. Review System
1. **Validation**: Checks `WatchHistory` to ensure user actually watched the content (`viewCounted=true`).
2. **Constraint**: One review per watch history entry.
3. **Crud**: Standard Create/Update/Delete with ownership checks.

## 4. Development Conventions
- **DTOs**: Always use DTOs for Controller I/O. Never expose Entities directly.
- **Exceptions**: Throw `BusinessException` with `ErrorCode` for logic errors.
- **Testing**:
  - `BillingTestService`/`BillingController` available for payment flows.
  - H2 Database used for testing.
- **Logging**: Slf4j (Lombok).

## 5. Current State
- **Implemented**:
  - **Core**: Basic Auth, Content CRUD, S3 Integration, HLS Encoding Pipeline.
  - **Streaming**: Theater Sync (WebSocket), CloudFront integration.
  - **Payment**: Toss Payment Integration (Billing Key, Recurring).
  - **Social**: Review System (WatchHistory integration).
  - **Search**: Advanced Schedule Search (QueryDSL, Date/Owner filtering).
  - **Settlement**: Spring Batch jobs (Revenue calc, Monthly reset, Fault tolerance).
- **Pending/In Progress**:
  - Security hardening (CloudFront Signed URLs - noted in docs).
  - Client-side precision sync (latency compensation).

## 6. Important Files
- `API.md`: Detailed REST API spec.
- `ENTITY.md`: Database schema documentation.
- `STREAMING_AND_S3_ARCHITECTURE.md`: Deep dive into the video pipeline.
- `TOSS_PAYMENT_PLAN.md`: Payment implementation details.
- `REVIEW_IMPLEMENTATION_PLAN.md`: Review feature plan.
- `SCHEDULE_SEARCH_IMPLEMENTATION_PLAN.md`: Search logic plan.
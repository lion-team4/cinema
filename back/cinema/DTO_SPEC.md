# DTO Specification

이 문서는 프로젝트에서 사용되는 Data Transfer Object (DTO) 목록과 그 용도를 정의합니다.

## 1. Common
| Class | Description |
| :--- | :--- |
| `ApiResponse` | API 응답 공통 래퍼 (성공/실패 여부, 데이터 포함) |
| `PageResponse` | 페이징 처리된 목록 응답 래퍼 |

## 2. Auth
**Package:** `com.example.cinema.dto.auth`

| Class | Description | Usage |
| :--- | :--- | :--- |
| `SignupRequest` | 회원가입 요청 | `POST /auth/signup` |
| `LoginRequest` | 로그인 요청 | `POST /auth/login` |
| `TokenRefreshRequest` | 토큰 재발급 요청 | `POST /auth/reissue` |
| `TokenResponse` | JWT 토큰 응답 | `POST /auth/login`, `POST /auth/reissue` |

## 3. User
**Package:** `com.example.cinema.dto.user`

| Class | Description | Usage |
| :--- | :--- | :--- |
| `UserGetResponse` | 유저 정보 조회 응답 | `GET /users/me`, `POST /auth/signup` |
| `UserUpdateRequest` | 유저 정보 수정 요청 | `PATCH /users/me` |
| `UserUpdateResponse` | 유저 정보 수정 응답 | `PATCH /users/me` |
| `UserDeleteRequest` | 회원 탈퇴 요청 (비밀번호 포함) | `DELETE /users/me` |
| `UserSearchResponse` | 유저 검색 목록 (Planned) | - |

## 4. Content
**Package:** `com.example.cinema.dto.content`

| Class | Description | Usage |
| :--- | :--- | :--- |
| `ContentRequest` | 콘텐츠 생성 요청 (1차) | `POST /contents` |
| `ContentAssetAttachRequest` | 에셋 추가 요청 (2차) | `PATCH /contents/{id}` |
| `ContentUpdateRequest` | 콘텐츠 수정 요청 | `PUT /contents/{id}` |
| `ContentResponse` | 콘텐츠 단건 조회 응답 | `POST /contents` |
| `ContentEditResponse` | 수정 폼 데이터 응답 | `GET /contents/{id}/edit`, `PUT /contents/{id}` |
| `ContentSearchRequest` | 콘텐츠 검색 조건 | `GET /contents` |
| `ContentSearchResponse` | 콘텐츠 검색 결과 요약 | `GET /contents` |
| `ReviewCreateRequest` | 리뷰 생성/수정 (Planned) | - |
| `ReviewListResponse` | 리뷰 목록 (Planned) | - |

## 5. Subscription & Payment
**Package:** `com.example.cinema.dto.subscription`

| Class | Description | Usage |
| :--- | :--- | :--- |
| `SubscriptionCreateRequest` | 구독 생성 요청 | `POST /users/subscriptions` |
| `FirstSubscriptionResponse` | 구독 생성 결과 | `POST /users/subscriptions` |
| `SubscriptionResponse` | 구독 정보 조회/변경 응답 | `GET /users/subscriptions`, `PUT /users/subscriptions` |
| `PaymentHistoryResponse` | 결제 내역 | `GET /users/subscriptions/payment-history` |
| `SubscriptionUpdateBillingRequest` | 결제 수단 변경 요청 | `PUT /users/subscriptions` |

## 6. Schedule
**Package:** `com.example.cinema.dto.schedule`

| Class | Description | Usage |
| :--- | :--- | :--- |
| `ScheduleCreateRequest` | 상영 일정 생성 요청 | `POST /schedules` |
| `ScheduleCreateResponse` | 상영 일정 생성 응답 | `POST /schedules` |
| `ScheduleEditRequest` | 상영 일정 수정 요청 | `PUT /schedules/{id}` |
| `ScheduleItemResponse` | 상영 일정 단건 응답 | `PUT /schedules/{id}` |
| `ScheduleLockRequest` | 편성 확정 요청 | `PUT /schedules/{id}/confirm` |
| `ScheduleLockResponse` | 편성 확정 응답 | `PUT /schedules/{id}/confirm` |

## 7. Theater
**Package:** `com.example.cinema.dto.theater`

| Class | Description | Usage |
| :--- | :--- | :--- |
| `TheaterEnterResponse` | 입장 응답 | `POST /theaters/{id}/enter` |
| `TheaterLeaveResponse` | 퇴장 응답 | `POST /theaters/{id}/leave` |
| `PlaybackInfoResponse` | 재생 정보 (URL 등) | `GET /theaters/{id}/playback` |
| `PlaybackStateResponse` | 재생 상태 (Sync) | `GET /theaters/{id}/state` |

## 8. Asset
**Package:** `com.example.cinema.api.AssetUploadController` (Inner Records)

| Class | Description | Usage |
| :--- | :--- | :--- |
| `PresignReq` | S3 Presigned URL 요청 | `POST /api/assets/presign` |
| `PresignRes` | S3 Presigned URL 응답 | `POST /api/assets/presign` |
| `CompleteReq` | 업로드 완료 처리 요청 | `POST /api/assets/complete` |
| `CompleteRes` | 완료/인코딩 시작 응답 | `POST /api/assets/complete` |

## 9. Settlement (Planned)
**Package:** `com.example.cinema.dto.settlement`

| Class | Description | Usage |
| :--- | :--- | :--- |
| `SettlementAccountRequest` | 정산 계좌 요청 | `POST/PUT /settlements/accounts` |
| `SettlementListResponse` | 정산 내역 응답 | `GET /settlements` |

# DTO Specification

이 문서는 프로젝트에서 사용되는 Data Transfer Object (DTO) 목록과 그 용도를 정의합니다.

## 1. Common
| Class | Description |
| :--- | :--- |
| `ApiResponse` | API 응답 공통 래퍼 (성공/실패 여부, 데이터 포함) |
| `PageResponse` | 페이징 처리된 목록 응답 래퍼 |

## 2. User
**Package:** `com.example.cinema.dto.user`

| Class | Description | Usage |
| :--- | :--- | :--- |
| `UserDetailResponse` | 유저 상세 정보 (본인/타인 프로필) | `GET /users/me`, `GET /users/search/{nick}/info` |
| `UserSearchResponse` | 유저 검색 목록 (경량화 정보) | `GET /users/search/{keyword}` |

## 3. Content
**Package:** `com.example.cinema.dto.content`

| Class | Description | Usage |
| :--- | :--- | :--- |
| `ContentSearchRequest` | 콘텐츠 검색 조건 (QueryParam 바인딩용) | `GET /contents/search` |
| `ContentSearchResponse` | 콘텐츠 목록/검색 결과 요약 | `GET /contents/search`, `GET /users/{nick}/contents` |
| `ReviewCreateRequest` | 리뷰 생성/수정 요청 | `POST/PUT /contents/reviews` |
| `ReviewListResponse` | 리뷰 목록 조회 | `GET /contents/reviews/search/{content-id}` |

## 4. Subscription & Payment
**Package:** `com.example.cinema.dto.subscription`

| Class | Description | Usage |
| :--- | :--- | :--- |
| `SubscriptionCreateRequest` | 구독 생성 요청 (결제 정보 포함) | `POST /users/subscriptions` |
| `SubscriptionResponse` | **현재 구독 상세 상태 조회** | `GET /users/subscription` |
| `PaymentHistoryResponse` | 과거 구독 결제 내역 조회 | `GET /users/subscriptions` |
| `SubscriptionUpdateBillingRequest` | 결제 수단 변경 요청 | `PUT /users/subscriptions` |

## 5. Settlement
**Package:** `com.example.cinema.dto.settlement`

| Class | Description | Usage |
| :--- | :--- | :--- |
| `SettlementAccountRequest` | 정산 계좌 등록/수정 요청 | `POST/PUT /settlements/accounts` |
| `SettlementListResponse` | 정산 내역 목록 조회 | `GET /settlements` |

## 6. Schedule
**Package:** `com.example.cinema.dto.schedule`

| Class | Description | Usage |
| :--- | :--- | :--- |
| `ScheduleCreateRequest` | 상영 일정 생성 요청 | `POST /schedules/{date}` |
| `ScheduleUpdateRequest` | 상영 일정 수정 요청 | `PUT /schedules/{id}` |

## 7. Theater
**Package:** `com.example.cinema.dto.theater`

| Class | Description | Usage |
| :--- | :--- | :--- |
| `TheaterEnterResponse` | 영화관 입장 응답 (토큰/상태 등) | `POST /theaters/{id}/enter` |
| `TheaterLogResponse` | 상영/시청 기록 (WatchHistory) | `POST /theaters/{id}/exit`, `GET /theaters/logs` |

## 8. Infrastructure (Internal)
**Package:** `com.example.cinema.infrastructure.payment.toss.dto`

| Class | Description | Note |
| :--- | :--- | :--- |
| `TossBillingResponse` | 토스 빌링키 발급 응답 | Internal Only |
| `TossPaymentResponse` | 토스 결제 승인 응답 | Internal Only |

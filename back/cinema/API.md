# API Specification

## 1. Auth (`/auth`)
| Method | Path | Description | Request | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/auth/signup` | 회원가입 | `SignupRequest` | `UserGetResponse` |
| `POST` | `/auth/login` | 로그인 (JWT 발급) | `LoginRequest` | `TokenResponse` |
| `POST` | `/auth/reissue` | 토큰 재발급 | `TokenRefreshRequest` | `TokenResponse` |
| `POST` | `/auth/logout` | 로그아웃 | - | - |

## 2. User (`/users`)
| Method | Path | Description | Request | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/users/me` | 내 정보 조회 | - | `UserGetResponse` |
| `PATCH` | `/users/me` | 내 정보 수정 | `UserUpdateRequest` | `UserUpdateResponse` |
| `DELETE` | `/users/me` | 회원 탈퇴 | `UserDeleteRequest` | - |

## 3. Subscription (`/users/subscriptions`)
| Method | Path | Description | Request | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/users/subscriptions` | 구독 생성 (최초 결제) | `SubscriptionCreateRequest` | `FirstSubscriptionResponse` |
| `GET` | `/users/subscriptions` | 내 구독 정보 조회 | - | `SubscriptionResponse` |
| `GET` | `/users/subscriptions/payment-history` | 결제 내역 조회 | - | `PageResponse<PaymentHistoryResponse>` |
| `PUT` | `/users/subscriptions` | 결제 수단 변경 | `SubscriptionUpdateBillingRequest` | `SubscriptionResponse` |
| `DELETE` | `/users/subscriptions` | 구독 해지 | - | - |

## 4. Content (`/contents`)
| Method | Path | Description | Request | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/contents` | 콘텐츠 생성 (1차) | `ContentRequest` | `ContentResponse` |
| `PATCH` | `/contents/{contentId}` | 콘텐츠 에셋 추가 (2차) | `ContentAssetAttachRequest` | `ContentResponse` |
| `GET` | `/contents/{contentId}/edit` | 수정 폼 조회 | - | `ContentEditResponse` |
| `PUT` | `/contents/{contentId}` | 콘텐츠 수정 | `ContentUpdateRequest` | `ContentEditResponse` |
| `DELETE` | `/contents/{contentId}` | 콘텐츠 삭제 | - | - |
| `GET` | `/contents` | 콘텐츠 검색 | `ContentSearchRequest` (Query) | `PageResponse<ContentSearchResponse>` |

## 5. Schedule (`/schedules`)
| Method | Path | Description | Request | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/schedules` | 상영 일정 생성 | `ScheduleCreateRequest` | `ScheduleCreateResponse` |
| `PUT` | `/schedules/{scheduleItemId}` | 상영 일정 수정 | `ScheduleEditRequest` | `ScheduleItemResponse` |
| `PUT` | `/schedules/{scheduleDayId}/confirm` | 일정 확정 (Lock) | `ScheduleLockRequest` | `ScheduleLockResponse` |
| `DELETE` | `/schedules/{scheduleItemId}` | 상영 일정 삭제 | - | - |

## 6. Theater (`/theaters`)
| Method | Path | Description | Request | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/theaters/{scheduleId}/enter` | 상영관 입장 | - | `TheaterEnterResponse` |
| `POST` | `/theaters/{scheduleId}/leave` | 상영관 퇴장 | - | `TheaterLeaveResponse` |
| `GET` | `/theaters/{scheduleId}/viewers` | 현재 시청자 수 | - | `Long` |
| `GET` | `/theaters/{scheduleId}/playback` | 재생 정보 조회 | - | `PlaybackInfoResponse` |
| `GET` | `/theaters/{scheduleId}/state` | 재생 상태 조회 | - | `PlaybackStateResponse` |

## 7. Asset (`/api/assets`)
| Method | Path | Description | Request | Response |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/assets/presign` | S3 업로드 URL 발급 | `PresignReq` | `PresignRes` |
| `POST` | `/api/assets/complete` | 업로드 완료 알림 (S3 -> DB) | `CompleteReq` | `CompleteRes` |
| `POST` | `/api/assets/contents/{contentId}/encoding/retry` | 인코딩 재시도 | - | `CompleteRes` |

> **Note**: WebSocket Endpoint for Theater Sync: `/ws` (Subscribe: `/topic/theaters/{scheduleId}/state`)
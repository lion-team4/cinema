# API Required DTO Development Plan

Based on `API.md` and `ENTITY.md`, the following DTOs are required.

## Naming Convention
- **Format:** `[Domain][Action][Suffix]`
- **Suffix:** `Request` (for Request Body), `Response` (for Response Body)
- **Example:** `UserLoginRequest`, `ContentDetailResponse`

## 1. User Domain (`dto/user`)
| API Endpoint | Method | DTO Name | Description |
|---|---|---|---|
| `/users/me` | GET | `UserDetailResponse` | My profile details (email, nickname, image) |
| `/users/search/{keyword}` | GET | `UserSearchResponse` | List of users (paging) |
| `/users/search/{nick}/info` | GET | `UserDetailResponse` | Other user's public profile |

## 2. Content Domain (`dto/content`)
| API Endpoint | Method | DTO Name | Description |
|---|---|---|---|
| `/contents/search` | GET | `ContentSearchResponse` | List of contents (paging, search) |
| `/users/{nick}/contents` | GET | `ContentSearchResponse` | User's content list |
| `/contents/reviews` | POST | `ReviewCreateRequest` | Create a review (rating, comment) |
| `/contents/reviews/{id}` | PUT | `ReviewUpdateRequest` | Update a review |
| `/contents/reviews/search/{contentId}` | GET | `ReviewListResponse` | List of reviews for a content |

## 3. Schedule Domain (`dto/schedule`)
| API Endpoint | Method | DTO Name | Description |
|---|---|---|---|
| `/schedules/{date}` | POST | `ScheduleCreateRequest` | Register daily schedule (list of items) |
| `/schedules/{id}` | PUT | `ScheduleUpdateRequest` | Update specific schedule item |
| `/schedules/{id}/confirm` | PUT | `ScheduleConfirmRequest` | Confirm schedule (optional, if body needed) |

## 4. Theater Domain (`dto/theater`)
| API Endpoint | Method | DTO Name | Description |
|---|---|---|---|
| `/theaters/{id}/enter` | POST | `TheaterEnterResponse` | Result of entering theater (sync info) |
| `/theaters/logs` | GET | `TheaterLogResponse` | Watch history logs |

## 5. Subscription/Payment Domain (`dto/subscription` or `dto/user`)
| API Endpoint | Method | DTO Name | Description |
|---|---|---|---|
| `/users/subscriptions` | POST | `SubscriptionCreateRequest` | Create subscription (plan, billing key) |
| `/users/subscriptions` | GET | `SubscriptionListResponse` | My subscription history |
| `/users/subscriptions` | PUT | `SubscriptionUpdateBillingRequest`| Update billing info |

## 6. Settlement Domain (`dto/settlement`)
| API Endpoint | Method | DTO Name | Description |
|---|---|---|---|
| `/settlements/accounts` | POST | `SettlementAccountRequest` | Register settlement account |
| `/settlements` | GET | `SettlementListResponse` | List of monthly settlements |

## Implementation Steps
1.  Create packages under `dto` if not present.
2.  Implement `Request` DTOs with Validation annotations (`@NotNull`, `@Size`).
3.  Implement `Response` DTOs with `static from(Entity)` methods or Builders.
4.  Ensure `Page` handling for Search APIs.

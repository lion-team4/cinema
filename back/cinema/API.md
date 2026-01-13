# API Specification

## Subscription
- `POST users/subscriptions`
  - 유저 구독 생성 (결제)
  - **Request**: `SubscriptionCreateRequest`
  - **Response**: `SubscriptionResponse` (or success status)

- `GET users/subscription`
  - **내 현재 구독 정보 조회**
  - **Response**: `SubscriptionResponse`

- `GET users/subscriptions`
  - 유저 구독 **결제 기록** 조회 (History)
  - **Response**: `PageResponse<PaymentHistoryResponse>`

**Query Parameters**

| 파라미터       | 타입     | 설명           |
| ---------- | ------ | ------------ |
| page       | int    | 페이지 번호 (0부터) |
| size       | int    | 페이지당 개수      |
| start-date | string | 기간 필터        |
| end-date   | string | 기간필터         |


- `PUT users/subscriptions`
  - 유저 결제 수단/정보 변경
  - **Request**: `SubscriptionUpdateBillingRequest`

- `DELETE users/subscriptions`
  - 유저 구독 해지
---

---

## Settlement
- `POST settlements/accounts`
  - 정산 계좌 등록
  - **Request**: `SettlementAccountRequest`

- `PUT settlements/accounts`
  - 정산 계좌 수정
  - **Request**: `SettlementAccountRequest`

- `DELETE settlements/accounts`
  - 정산 계좌 삭제

- `GET settlements`
  - 정산 내역 조회
  - **Response**: `PageResponse<SettlementListResponse>`

**Query Parameters**

| 파라미터       | 타입     | 설명           |
| ---------- | ------ | ------------ |
| page       | int    | 페이지 번호 (0부터) |
| size       | int    | 페이지당 개수      |
| start-date | string | 기간 필터        |
| end-date   | string | 기간필터         |


---

## Schedule
- `POST schedules/{schedule-date}`
  - 영화 상영 일정 생성
  - **Request**: `ScheduleCreateRequest`

- `PUT schedules/{schedule-id}`
  - 영화 상영 일정 수정
  - **Request**: `ScheduleUpdateRequest`

- `PUT schedules/{schedule-id}/confirm`
  - 상영 일정 확정 (수정 불가 처리)

- `DELETE schedules/{schedule-id}`
  - 삭제


---

## Theater
- `POST theaters/{schedule-id}/enter`
  - 영화관 입장 요청
  - 동시 접속 제어 / 시청 기록 생성
  - **Response**: `TheaterEnterResponse`

- `POST theaters/{schedule-id}/exit`
  - 영화관 퇴실 요청
  - **Response**: `TheaterLogResponse` (Optional)

- `GET theaters/logs`
    - 상영/시청 기록 조회
    - **Response**: `PageResponse<TheaterLogResponse>`

---

## User
- `GET users/me`
  - 내 정보 조회
  - **Response**: `UserDetailResponse`

- `GET users/search/{keyword}`
  - 타 유저 정보 조회
  - **Response**: `PageResponse<UserSearchResponse>`

**Query Parameters**

| 파라미터 | 타입  | 설명           |
| ---- | --- | ------------ |
| page | int | 페이지 번호 (0부터) |
| size | int | 페이지당 개수      |


- `GET users/search/{nick}/info`
  - 타유저 상세 조회 (프로필)
  - **Response**: `UserDetailResponse`

---

## Content
- `GET contents/search`
  - 영화(콘텐츠) 검색 및 조회
  - **Response**: `PageResponse<ContentSearchResponse>`

**Query Parameters**

| 파라미터        | 타입              | 설명                     |
| ----------- | --------------- | ---------------------- |
| page        | int             | 페이지 번호 (0부터)           |
| size        | int             | 페이지당 개수                |
| keyword     | string          | 검색 키워드                 |
| search-type | string          | 검색 기준 (TITLE, DIRECTOR 등) |
| tag         | string (repeat) | 태그 목록                  |
| tag-mode    | String          | `OR` / `AND` (기본: AND) |
| sort        | String          | view, createdAt        |
| asc         | bool            | asc / desc             |



- `GET users/{nick}/contents`
  - 특정 유저의 영상 목록 조회
  - **Response**: `PageResponse<ContentSearchResponse>`

**Query Parameters**

| 파라미터        | 타입              | 설명                     |
| ----------- | --------------- | ---------------------- |
| page        | int             | 페이지 번호 (0부터)           |
| size        | int             | 페이지당 개수                |
| keyword     | string          | 검색 키워드                 |
| search-type | string          | 검색 기준                  |
| tag         | string (repeat) | 태그 목록                  |
| tag-mode    | String          | `OR` / `AND`             |
| sort        | String          | view, createdAt        |
| asc         | bool            | asc / desc             |


- `POST contents/reviews`
  - 리뷰 생성
  - **Request**: `ReviewCreateRequest`

- `PUT contents/reviews/{review-id}`
  - 리뷰 수정
  - **Request**: `ReviewCreateRequest`

- `DELETE contents/reviews/{review-id}`
  - 리뷰 삭제

- `GET contents/reviews/search/{content-id}`
  - 리뷰 조회

  - **Response**: `PageResponse<ReviewListResponse>`
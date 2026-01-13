- `POST users/subscriptions`
  - 유저 구독 생성
  - 유저 정보는 디데일에서

- `GET users/subscriptions`
  - 유저 구독 결제 기록 조회

**Query Parameters**

| 파라미터       | 타입     | 설명           |
| ---------- | ------ | ------------ |
| page       | int    | 페이지 번호 (0부터) |
| size       | int    | 페이지당 개수      |
| start-date | string | 기간 필터        |
| end-date   | string | 기간필터         |


- `PUT users/subscriptions`
  - 유저 결제 정보 변경

- `DELETE users/subscriptions`
  - 유저 구독 해지
---


- `POST settlements/accounts`
  - 정산 계좌 등록

- `PUT settlements/accounts`
  - 정산 계좌 수정

- `DELETE settlements/accounts`
  - 정산을 나는 더이상 원치 않습니다

- `GET settlements`
  - 정산 내역 조회회

**Query Parameters**

| 파라미터       | 타입     | 설명           |
| ---------- | ------ | ------------ |
| page       | int    | 페이지 번호 (0부터) |
| size       | int    | 페이지당 개수      |
| start-date | string | 기간 필터        |
| end-date   | string | 기간필터         |


---

- `POST schedules/{schedule-date}`
  - 영화 상영 일정 생성

- `PUT schedules/{schedule-id}`
  - 영화 상영 일정 수정

- `PUT schedules/{schedule-id}/confirm`
  - 확정

- `DELETE schedules/{schedule-id}`
  - 삭제


---


- `POST theaters/{schedule-id}/enter`
  - 영화관 입장 요청
  - 동시 접속 제어
  - 시청 기록 생성

- `POST theaters/{schedule-id}/exit`
  - 영화관 퇴실 요청

- `GET theaters/logs`
    - 유저 정보는 디데일에서
    - 상영 기록 조회

- `GET users/me
  - 내 정보 조회



- `GET users/search/{keyword}`
  - 타 유저 정보 조회
**Query Parameters**

| 파라미터 | 타입  | 설명           |
| ---- | --- | ------------ |
| page | int | 페이지 번호 (0부터) |
| size | int | 페이지당 개수      |


- `GET users/search/{nick}/info`
  - 타유저 상세 조회


- `GET contents/search`
  - 영화 조회

**Query Parameters**

| 파라미터        | 타입              | 설명                     |
| ----------- | --------------- | ---------------------- |
| page        | int             | 페이지 번호 (0부터)           |
| size        | int             | 페이지당 개수                |
| keyword     | string          | 검색 키워드                 |
| search-type | string          | 검색 기준 제목 or 감독         |
| tag         | string (repeat) | 태그 목록                  |
| tag-mode    | Sting           | `OR` / `AND` (기본: AND) |
| sort        | String          | view, createdAt        |
| asc         | bool            | asc / desc             |



- `GET users/{nick}/contents`
  - 유저 영상 목록 조회

**Query Parameters**

| 파라미터        | 타입              | 설명                     |
| ----------- | --------------- | ---------------------- |
| page        | int             | 페이지 번호 (0부터)           |
| size        | int             | 페이지당 개수                |
| keyword     | string          | 검색 키워드                 |
| search-type | string          | 검색 기준 제목 or 감독         |
| tag         | string (repeat) | 태그 목록                  |
| tag-mode    | Sting           | `OR` / `AND` (기본: AND) |
| sort        | String          | view, createdAt        |
| asc         | bool            | asc / desc             |


- `POST contents/reviews`
  - 리뷰 생성

- `PUT contents/reviews/{review-id}`
  - 리뷰 수정

- `DELETE contents/reviews/{review-id}`
  - 리뷰 삭제

- `GET contents/reviews/search/{content-id}`
  - 리뷰 조회

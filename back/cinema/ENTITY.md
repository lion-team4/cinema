## 1) Core (MVP) 엔티티

### A. 회원/프로필

### 1) `users`

### 개요

| 항목 | 내용 |
| --- | --- |
| 역할 | 로그인(JWT), 닉네임/이메일 중복, 마이페이지/타유저 프로필의 주체 |

### 주요 필드

| 컬럼명 | 설명 |
| --- | --- |
| user_id (PK) | 사용자 PK |
| email (UNIQUE, NOT NULL) | 로그인 이메일, 유니크 |
| nickname (UNIQUE, NOT NULL) | 닉네임, 유니크 |
| password_hash (NOT NULL) | 비밀번호 해시 |
| profile_image_asset_id (FK -> media_assets.asset_id, NULL) | 프로필 이미지 FK |
| created_at | 생성 일시 |
| updated_at | 수정 일시 |
| deleted_at (soft delete 선택) | 소프트 삭제 시각 |
| seller | 불리안으로 판별 |

---

### B. 내 영상(콘텐츠) 관리 + S3 업로드

### 2) `contents` (영화/영상 메타)

### 개요

| 항목 | 내용 |
| --- | --- |
| 역할 | “영화 등록”, 포스터/영상과 매핑, 리스트 조회의 기준 테이블 |
| 비고 | 포스터 + 원본 + 상영본(HLS)을 FK로 명확히 연결 |

### 주요 필드

| 컬럼명 | 설명 |
| --- | --- |
| content_id (PK) | 콘텐츠 PK |
| owner_user_id (FK -> users.user_id, NOT NULL) | 소유자 유저 ID |
| title (NOT NULL) | 제목 |
| description (TEXT) | 설명 |
| poster_asset_id (FK -> media_assets.asset_id, NULL) | 포스터 이미지(PUBLIC, `POSTER_IMAGE`) |
| video_source_asset_id (FK -> media_assets.asset_id, NULL) | 원본 mp4(PRIVATE, `VIDEO_SOURCE`) |
| video_hls_master_asset_id (FK -> media_assets.asset_id, NULL) | 재생 m3u8(PRIVATE, `VIDEO_HLS_MASTER`) |
| status (DRAFT, PUBLISHED, HIDDEN 등) | 노출 상태 |
| created_at | 생성 일시 |
| updated_at | 수정 일시 |
| total_view | 전체 조회수 |
| month_view | 한달 조회수 (달마다 초기화) |

### 서비스 레이어 규칙(권장, 팀 합의용)

- `status=PUBLISHED` 조건: `poster_asset_id`와 `video_hls_master_asset_id`는 **필수**(NULL 금지처럼 운영)
- `/playback`은 **오직 `video_hls_master_asset_id`만** 사용 (원본 mp4로 재생하지 않음)
- “video_asset_id 선택 시 poster 자동 세팅” 로직은 그대로 가능:
    - 원본 업로드 직후에는 `video_source_asset_id`만 채워질 수 있음
    - 변환 완료 후 `video_hls_master_asset_id`를 채우고, 그 시점에 publish 가능

---

### 3) `media_assets` (S3 오브젝트 메타)

### 개요

| 항목 | 내용 |
| --- | --- |
| 역할 | S3 업로드 결과(키/URL/권한)를 DB에 표준화해서 매핑 |
| 핵심 | `asset_type`으로 “포스터/원본/상영본(HLS)”를 명확히 구분 |

### 주요 필드

| 컬럼명 | 설명 |
| --- | --- |
| asset_id (PK) | 에셋 PK |
| owner_user_id (FK -> users.user_id, NOT NULL) | 소유자 유저 ID |
| asset_type (NOT NULL) | `PROFILE_IMAGE`, `POSTER_IMAGE`, `VIDEO_SOURCE`, `VIDEO_HLS_MASTER` |
| bucket (NOT NULL) | S3 버킷 이름 |
| object_key (NOT NULL) | S3 key (예: `posters/...`, `uploads/...mp4`, `vod/hls/.../master.m3u8`) |
| content_type | MIME 타입 (예: `image/png`, `video/mp4`, `application/vnd.apple.mpegurl`) |
| visibility (NOT NULL) | `PUBLIC`, `PRIVATE` |
| size_bytes | 파일 크기(bytes) |
| duration_ms | 영상 길이(ms) — 주로 `VIDEO_SOURCE`에 사용 |
| created_at | 생성 일시 |
| updated_at | 수정 일시(선택이지만 권장) |

### `asset_type` 의미(팀 규칙으로 고정)

- `VIDEO_SOURCE` → `uploads/` 아래의 원본 mp4 (관객 노출 금지)
- `VIDEO_HLS_MASTER` → `vod/hls/{contentId}/master.m3u8` (재생 시작점)

---

---

### C. 상영 편성 / 달력 / 상태 (Coming Up, Happening Now, Closed)

### 6) `schedule_days` (일자 단위 편성 헤더)

### 개요

| 항목 | 내용 |
| --- | --- |
| 역할 | “일일 편성 등록”, “편성 확정 시 잠금”을 위한 날짜 단위 루트 |

### 주요 필드

| 컬럼명 | 설명 |
| --- | --- |
| schedule_day_id (PK) | 일자 편성 PK |
| content_id (FK -> contents.content_id, NOT NULL) | 편성 컨텐츠의 ID |
| schedule_date (DATE, NOT NULL) | 편성 날짜 |
| is_locked (BOOLEAN, NOT NULL, default false) | 편성 확정 여부 |
| locked_at | 잠금 시각 |
| created_at | 생성 일시 |
| updated_at | 수정 일시 |

---

### 7) `schedule_items` (실제 상영 슬롯)

### 개요

| 항목 | 내용 |
| --- | --- |
| 역할 | 상태 자동 변경, 현재/예정작 계산 근거 |

### 주요 필드

| 컬럼명 | 설명 |
| --- | --- |
| schedule_item_id (PK) | 상영 슬롯 PK |
| schedule_day_id (FK -> schedule_days.schedule_day_id, NOT NULL) | 속한 편성 일자 |
| content_id (FK -> contents.content_id, NOT NULL) | 상영할 콘텐츠 ID |
| start_at (DATETIME, NOT NULL) | 상영 시작 시각 |
| end_at (DATETIME, NOT NULL) | 상영 종료 시각 |
| status (COMING_UP, HAPPENING_NOW, CLOSED) | 상영 상태 |
| created_at | 생성 일시 |
| updated_at | 수정 일시 |

### 제약

| 항목 | 내용 |
| --- | --- |
| 시간 중복 방지 | 동일 일자 내 start/end overlap 금지 로직(서비스) |
| 잠금 시 수정 금지 | schedule_[days.is](http://days.is)_locked = true 이면 수정/삭제 금지 |

---

### D. 상영관 입장(동기화) + 리뷰

### 8) `watch_histories` (시청 이력)

### 개요

| 항목 | 내용 |
| --- | --- |
| 역할 | 상영관 입장 및 “본 사람만 리뷰” 판단 근거 데이터 |

### 주요 필드

| 컬럼명 | 설명 |
| --- | --- |
| watch_id (PK) | 시청 이력 PK |
| user_id (FK -> users.user_id, NOT NULL) | 시청자 유저 ID |
| schedule_item_id (FK -> schedule_items.schedule_item_id, NOT NULL) | 본 상영 슬롯 |
| created_at | 생성 일시 |

---

### 9) `reviews` (별점/리뷰)

### 개요

| 항목 | 내용 |
| --- | --- |
| 역할 | “영화 리뷰(본 사람만, 영화당 1번만)” 저장 |

### 주요 필드

| 컬럼명 | 설명 |
| --- | --- |
| review_id (PK) | 리뷰 PK |
| content_id (FK -> contents.content_id, NOT NULL) | 리뷰 대상 콘텐츠 |
| user_id (FK -> users.user_id, NOT NULL) | 리뷰 작성자 유저 ID |
| rating (1~5, NOT NULL) | 별점 |
| comment (TEXT) | 리뷰 내용 |
| created_at | 생성 일시 |
| updated_at | 수정 일시 |

### 제약

| 항목 | 내용 |
| --- | --- |
| 영화당 1회 | UNIQUE(content_id, user_id) |
| 본 사람만 | insert 시 watch_histories 존재 여부 검사 |

---

## 2) 구독/결제/정산 (기본 엔티티)

### 10) `subscription_plans`

### 개요

| 항목 | 내용 |
| --- | --- |
| 역할 | 구독 상품(플랜) 정의, 단일 플랜이면 생략 가능하나 확장성 위해 권장 |

### 주요 필드

| 컬럼명 | 설명 |
| --- | --- |
| plan_id (PK) | 플랜 PK |
| name | 플랜 이름 |
| price | 가격 |
| billing_cycle (MONTHLY) | 청구 주기 |
| is_active | 활성 여부 |

---

### 11) `subscriptions` (구독 관계)

### 개요

| 항목 | 내용 |
| --- | --- |
| 역할 | 구독자 ↔ 크리에이터 구독 관계 및 상태 관리 |

### 주요 필드

| 컬럼명 | 설명 |
| --- | --- |
| subscription_id (PK) | 구독 PK |
| subscriber_user_id (FK -> users.user_id, NOT NULL) | 구독자 유저 ID |
| creator_user_id (FK -> users.user_id, NOT NULL) | 크리에이터 유저 ID |
| plan_id (FK -> subscription_plans.plan_id) | 구독 플랜 ID |
| status (ACTIVE, PAUSED, CANCELED, EXPIRED) | 구독 상태 |
| current_period_start | 현재 청구 기간 시작 |
| current_period_end | 현재 청구 기간 종료 |
| billing_key_id (FK -> billing_keys.billing_key_id) | 정기 결제용 빌링키 ID |
| created_at | 생성 일시 |
| updated_at | 수정 일시 |

### 제약

| 항목 | 내용 |
| --- | --- |
| 중복 구독 방지 | UNIQUE(subscriber_user_id, creator_user_id) |

---

### 12) `billing_keys` (토스 빌링키)

### 개요

| 항목 | 내용 |
| --- | --- |
| 역할 | 카드 등록 및 PG에서 받은 빌링키 저장 |

### 주요 필드

| 컬럼명 | 설명 |
| --- | --- |
| billing_key_id (PK) | 빌링키 PK |
| subscription_id (FK -> subscriptions.subscription_id, NOT NULL) | 연결 구독 ID |
| provider (TOSS) | PG사 |
| billing_key (UNIQUE, NOT NULL) | PG에서 받은 빌링키 값 |
| card_last4 | 카드 뒤 4자리(선택) |
| card_brand | 카드 브랜드(선택) |
| status (ACTIVE, REVOKED) | 상태 |
| created_at | 생성 일시 |
| revoked_at | 해지 일시 |

---

### 13) `payments` (결제 이력)

### 개요

| 항목 | 내용 |
| --- | --- |
| 역할 | 승인/실패/환불 등 결제 이력 및 갱신일 결제 추적 |

### 주요 필드

| 컬럼명 | 설명 |
| --- | --- |
| payment_id (PK) | 결제 이력 PK |
| subscription_id (FK) | 관련 구독 ID |
| provider_payment_id | PG 거래 키 |
| amount | 결제 금액 |
| status (APPROVED, FAILED, CANCELED, REFUNDED) | 결제 상태 |
| paid_at | 실제 결제 시각 |
| created_at | 레코드 생성 일시 |

---

### 18) `settlements` (정산 결과)

### 개요

| 항목 | 내용 |
| --- | --- |
| 역할 | 크리에이터별 기간 정산 결과 |

### 주요 필드

| 컬럼명 | 설명 |
| --- | --- |
| settlement_id (PK) | 정산 PK |
| creator_user_id | 크리에이터 유저 ID |
| period_start | 정산 시작일 (26.01.01) |
| period_end | 정산 종료일 (26.01.01) |
| total_views | 기간 내 총 조회수 |
| amount | 정산 금액 |
| status | 정산 상태 |
| month_view | 한달 조회수 (달마다 초기화) |

---

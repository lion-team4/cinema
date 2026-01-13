# DTO 구현 체크리스트

## Phase 1: 사용자 도메인 (`dto/user`)
- [x] `UserDetailResponse`: 내 프로필 및 타 유저 정보 응답
- [x] `UserSearchResponse`: 유저 검색 결과 목록 응답

## Phase 2: 콘텐츠 도메인 (`dto/content`)
- [x] `ContentSearchResponse`: 콘텐츠 검색 및 목록 페이징 응답
- [x] `ReviewCreateRequest`: 리뷰 생성 요청 바디
- [x] `ReviewUpdateRequest`: 리뷰 수정 요청 바디
- [x] `ReviewListResponse`: 특정 콘텐츠의 리뷰 목록 응답

## Phase 3: 편성 및 상영관 도메인 (`dto/schedule`, `dto/theater`)
- [x] `ScheduleCreateRequest`: 일일 편성 등록 요청
- [x] `ScheduleUpdateRequest`: 편성 슬롯 수정 요청
- [x] `ScheduleConfirmRequest`: 편성 확정 요청
- [x] `TheaterEnterResponse`: 상영관 입장 결과 (동기화 정보 포함) 응답
- [x] `TheaterLogResponse`: 시청 기록 로그 응답

## Phase 4: 구독 및 정산 도메인 (`dto/subscription`, `dto/settlement`)
- [x] `SubscriptionCreateRequest`: 신규 구독 신청 요청
- [x] `SubscriptionListResponse`: 구독 및 결제 내역 목록 응답
- [x] `SubscriptionUpdateBillingRequest`: 결제 수단(빌링키) 변경 요청
- [x] `SettlementAccountRequest`: 정산 계좌 등록/수정 요청
- [x] `SettlementListResponse`: 월별 정산 내역 목록 응답

## Phase 5: 최종 검증
- [x] 모든 DTO에 Lombok 어노테이션 (`@Getter`) 적용 확인
- [x] Request DTO에 Validation 어노테이션 (`@NotNull`, `@NotBlank` 등) 적용 확인
- [x] 패키지 구조가 `src/main/java/com/example/cinema/dto/...`와 일치하는지 확인
- [x] Lombok 어노테이션 (`@Builder`) 사용하지 않고 DTO 생성시 정적 팩토리 매서드 사용 하도록 적용

## Phase 6: 공통 및 누락 DTO 보완 (`dto/common`, `dto/content`, `dto/subscription`)
- [x] `PageResponse`: 페이징 정보 공통 응답 DTO
- [x] `ContentSearchRequest`: 콘텐츠 검색 필터 요청 DTO
- [x] `PaymentHistoryResponse`: 결제 내역 응답 DTO (결제 기록 조회용)

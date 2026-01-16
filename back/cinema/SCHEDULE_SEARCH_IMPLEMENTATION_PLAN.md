# 영화 상영 일정 검색 기능 구현 계획

## 개요
`ScheduleSearchRequest.java`를 활용하여 다양한 조건(날짜, 잠금 여부, 닉네임 등)으로 영화 상영 일정을 조회하는 기능을 구현합니다.

## 구현 단계 (Phases)

### Phase 1: DTO 및 Repository 계층 구현
1.  **DTO 검토 및 보완**:
    *   `ScheduleSearchRequest` 필드 확인 (완료).
    *   검색 결과 반환을 위한 `ScheduleSearchResponse` DTO 생성 (기존 `ScheduleItemResponse`에 `nickname` 등 필요한 필드 추가 고려).
2.  **Custom Repository 인터페이스 정의**:
    *   `ScheduleItemRepositoryCustom` 인터페이스 생성.
    *   메서드 시그니처: `Page<ScheduleItem> search(ScheduleSearchRequest request)`
3.  **QueryDSL 구현체 작성**:
    *   `ScheduleItemRepositoryImpl` 클래스 생성.
    *   `BooleanBuilder`를 사용하여 동적 쿼리 구현 (날짜 범위, `isLocked`, `nickname` 필터링).
    *   `Content` 및 `User` 엔티티와 조인하여 필터링 수행.
4.  **Repository 통합**:
    *   `ScheduleItemRepository`가 `ScheduleItemRepositoryCustom`을 상속받도록 수정.

### Phase 2: Service 계층 구현
1.  **Service 메서드 추가**:
    *   `ScheduleService`에 `searchSchedules(ScheduleSearchRequest request)` 메서드 추가.
2.  **비즈니스 로직 구현**:
    *   Repository의 검색 메서드 호출.
    *   검색 결과를 `PageResponse<ScheduleSearchResponse>` 형태로 변환하여 반환.

### Phase 3: Controller 계층 구현
1.  **Endpoint 추가**:
    *   `ScheduleController`에 `GET /schedules/search` (또는 `GET /schedules`) 엔드포인트 추가.
2.  **파라미터 매핑**:
    *   `@ModelAttribute`를 사용하여 쿼리 파라미터를 `ScheduleSearchRequest`로 매핑.
3.  **응답 처리**:
    *   Service 호출 결과(PageResponse)를 `ApiResponse`로 감싸서 반환.

### Phase 4: 테스트 및 검증
1.  **빌드 테스트**: `./gradlew build` 수행.
2.  **기능 검증**: 구현된 검색 API가 조건별로 올바르게 동작하는지 확인.

# Cinema 프로젝트 컨텍스트 (GEMINI.md)

## 1. 프로젝트 개요
**이름:** Cinema
**설명:** 시네마 및 비디오 스트리밍 플랫폼을 위한 Spring Boot 기반 백엔드 애플리케이션.
**최종 업데이트:** 2026-01-16 (금요일)

**현재 상태:**
*   **보안 (Security):** **구현 완료**. JWT 인프라(`JwtTokenProvider`, `JwtAuthenticationFilter`, `ProjectSecurityConfig`) 정상 작동.
*   **사용자 모듈 (User):** **구현 완료**. 회원가입, 로그인, 프로필 관리, 토큰 재발급 기능 포함.
*   **구독 모듈 (Subscription):** **구현 완료**. 구독 플랜, 빌링키 관리, 토스 페이먼츠 연동 정기 결제 및 내역 조회 지원.
*   **콘텐츠 모듈 (Content):** **구현 완료**.
    *   **레포지토리**: QueryDSL을 이용한 검색 최적화 완료.
    *   **서비스**: 전체 CRUD 및 검색 로직 구현 완료.
    *   **컨트롤러**: `ContentController` 매핑 및 기능 검증 완료.
*   **스케줄 모듈 (Schedule):** **구현 완료 (부분적)**.
    *   **서비스**: `ScheduleService` 비즈니스 로직 구현 완료.
    *   **레포지토리**: `ScheduleDayRepository`, `ScheduleItemRepository` 존재.
    *   **컨트롤러**: `ScheduleController`가 존재하며 엔드포인트 검증 단계.
*   **예외 처리 (Exception Handling):** **리팩토링 완료**.
    *   `ErrorCode`, `BusinessException`, `GlobalExceptionHandler`를 통한 구조화.
    *   상세 내용은 `EXCEPTION_SPEC.md` 참조.
*   **인프라:** MySQL, QueryDSL, Swagger, 토스 페이먼츠 설정 완료.

## 2. 기술 스택
*   **언어:** Java 21
*   **프레임워크:** Spring Boot 3.5.9
*   **빌드 도구:** Gradle
*   **데이터베이스:** MySQL
*   **ORM:** JPA + Hibernate + QueryDSL 5.0.0
*   **문서화:** SpringDoc OpenAPI (Swagger) v2.8.6
*   **보안:** Spring Security + JWT

## 3. 프로젝트 구조
`src/main/java/com/example/cinema`:
*   `config/`: 보안, 토스 결제, QueryDSL 등 설정 파일.
*   `controller/`: 각 모듈별 API 컨트롤러.
*   `service/`: 비즈니스 로직. `encoding/`에 HLS 트랜스코딩 로직 포함.
*   `repository/`: JPA 레포지토리 및 QueryDSL 구현체.
*   `entity/`: JPA 엔티티 클래스.
*   `dto/`: 요청/응답용 DTO 클래스.
*   `exception/`: **리팩토링 완료**. 전역 예외 처리기 및 커스텀 예외 클래스.

## 4. 구현 로드맵 및 현황

### ✅ 완료됨
1.  **기반 구축**: 엔티티 설계, 레포지토리 레이어, 보안(JWT) 설정.
2.  **커머스**: 구독 서비스 및 토스 페이먼츠 연동.
3.  **콘텐츠 코어**: 콘텐츠 CRUD, 검색 서비스 및 컨트롤러 구현.
4.  **예외 처리 표준화**:
    *   모든 서비스 레이어에 `BusinessException` 적용.
    *   `ApiResponse`를 통한 JSON 응답 포맷 통일.
    *   `EXCEPTION_SPEC.md` 가이드 생성.

### 🚧 진행 중 / 검증 필요
1.  **스케줄 모듈**:
    *   서비스 및 레포지토리 구현 상태 확인 완료.
    *   `ScheduleController` 엔드포인트 작동 여부 및 통합 테스트 필요.
2.  **API 문서화**:
    *   Swagger UI를 통한 API 명세 최종 확인.

### 📅 향후 계획
1.  **스케줄 모듈 검증**: 스케줄 생성/조회 기능의 엔드투엔드 테스트.
2.  **시청 기록 (Watch History)**: 사용자별 시청 데이터 추적 로직 구현.
3.  **정산 (Settlement)**: 감독/판매자를 위한 정산 처리 시스템 구축.

## 5. 최근 변경 사항 (2026-01-16)
*   **예외 처리 리팩토링**: `UserService`, `SubscriptionService`, `ContentService`, `ScheduleService` 등 모든 서비스의 예외 메시지를 한국어로 통일하고 표준 구조 적용.
*   **버그 수정 및 정리**: `ContentController` 매핑 오류 확인 및 불필요한 import 제거.
*   **문서화**: 예외 처리 규격을 정리한 `EXCEPTION_SPEC.md` 추가 및 `GEMINI.md` 한글화 업데이트.
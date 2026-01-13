# Toss Payments 연동 분석 보고서

본 문서는 현재 프로젝트의 Toss Payments 연동 상태를 분석하고, 향후 구현이 필요한 항목들을 정리한 보고서입니다.

## 1. 개요
*   **분석 대상**: `TOSS_PAYMENT_PLAN.md` 및 관련 소스 코드 (`TossPaymentConfig`, `TossPaymentClient`, DTOs)
*   **목표**: 자동 결제(빌링) 시스템 구축
*   **현재 진행률**: **Phase 2 (Infrastructure) 완료**, Phase 3 (Business Logic) 미진행

## 2. 현재 구현 상태 분석 (AS-IS)

### ✅ 완료된 사항
1.  **환경 설정 (`Configuration`)**
    *   `TossPaymentConfig`: Spring Boot 3.2+ `RestClient`를 활용한 설정이 올바르게 구현되었습니다.
    *   `Basic Auth` 헤더 생성 로직(Secret Key 인코딩)이 정상적으로 포함되어 있습니다.
    *   `application.yaml`에서 `toss.secret-key`, `toss.url`을 주입받도록 설계되었습니다.

2.  **인프라 클라이언트 (`Infrastructure`)**
    *   `TossPaymentClient`:
        *   **빌링키 발급**: `/billing/authorizations/issue` 엔드포인트 호출 구현 완료.
        *   **결제 승인**: `/billing/{billingKey}` 엔드포인트 호출 구현 완료.
    *   Toss Payments Core API 명세와 일치합니다.

3.  **데이터 전송 객체 (`DTO`)**
    *   `TossBillingResponse`, `TossPaymentResponse`: API 응답을 매핑하기 위한 필드들이 충실히 구현되었습니다. (`Card`, `Receipt` 내부 클래스 포함)

## 3. 향후 구현 필요 사항 (TO-DO)

현재 인프라 계층(통신 모듈)은 완성되었으나, **비즈니스 로직과 데이터 영속성 계층이 부재**하여 실제 기능 동작은 불가능한 상태입니다.

### 🚨 핵심 누락 컴포넌트 (Priority: High)
1.  **서비스 계층 (`Service Layer`)**
    *   **`BillingService`**:
        *   프론트엔드에서 받은 `authKey`를 `TossPaymentClient`로 전달하여 `billingKey` 발급.
        *   발급된 키를 `BillingKey` 엔티티로 변환하여 DB 저장 (User와 연관 관계 설정).
    *   **`PaymentService`**:
        *   `BillingService`에서 저장된 키를 조회하여 정기 결제 요청.
        *   결제 성공 시 `Payment` 엔티티 생성 및 결제 이력 저장.
        *   `Subscription` 상태 업데이트 (예: `ACTIVE`로 변경).

2.  **리포지토리 계층 (`Repository Layer`)**
    *   `BillingKeyRepository`: 빌링키 저장/조회.
    *   `PaymentRepository`: 결제 이력 저장.
    *   `SubscriptionRepository`: 구독 정보 관리.

3.  **보안 및 인증 (`Security`)**
    *   결제는 민감한 작업이므로 **인증된 사용자(JWT)** 만 접근 가능해야 합니다. 현재 보안 설정 부재로 이 부분의 통합이 필요합니다.

## 4. 연동 시나리오 검증 계획

### 정상 흐름 (Happy Path)
1.  사용자가 프론트엔드(Toss Widget)에서 카드 등록 완료 → `authKey` 수신.
2.  프론트엔드 -> 백엔드: `POST /api/subscriptions` (Body: `authKey` 포함).
3.  백엔드(`BillingService`): `authKey` + `customerKey`로 Toss에 빌링키 요청.
4.  Toss -> 백엔드: `billingKey` 응답.
5.  백엔드: `BillingKey` DB 저장 후 즉시 1회차 결제 시도 (`PaymentService`).
6.  결제 성공 시 `Subscription` 생성 및 응답.

### 예외 처리 (Exception Handling)
*   **발급 실패**: 유효하지 않은 `authKey` 또는 타임아웃 처리.
*   **잔액 부족**: 결제 승인 실패 시 구독 생성 롤백 또는 '결제 실패' 상태 처리 로직 필요.

## 5. 결론 및 제언
Toss Payments 연동을 위한 **기반 공사(DTO, Client)는 성공적으로 완료**되었습니다. 다음 단계로 **Phase 3 (비즈니스 로직)** 구현에 착수해야 하며, 이를 위해 **Repository 계층 생성**과 **Service 계층 구현**이 시급합니다. 또한, 실제 결제 테스트를 위해 `application.yaml`에 테스트용 Secret Key가 올바르게 설정되어 있는지 확인이 필요합니다.

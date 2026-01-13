# Toss Payments 자동결제(빌링) 구현 계획

토스페이먼츠(Toss Payments)의 빌링 API를 연동하여 정기 구독 결제 시스템을 구축하기 위한 단계별 계획입니다.

## 📋 핵심 변경 사항
- **DTO 수정**: `billingKey` 직접 수신 방식 -> `authKey` 수신 후 백엔드 발급 방식으로 변경 (보안 강화)
- **설정 추가**: `application.yaml`에 토스 API 설정 추가
- **통신 구현**: `RestClient` 기반의 외부 API 통신 모듈 구현
- **로직 구현**: 빌링키 발급, 저장, 정기 결제 요청 비즈니스 로직 작성

---

## Phase 1: 설정 및 DTO 정비 (Setup & DTO)
**목표**: 외부 통신을 위한 환경 설정과 데이터 구조 변경
- [x] **`application.yaml` 설정**: `toss.secret-key`, `toss.url` 등 API 연동 정보 추가
- [x] **DTO 수정**:
    - `SubscriptionCreateRequest`: `billingKey` -> `authKey` 변경
    - `SubscriptionUpdateBillingRequest`: `billingKey` -> `authKey` 변경
- [x] **Toss 전용 DTO 생성 (`dto/payment/toss`)**:
    - `TossBillingResponse`: 빌링키 발급 응답 매핑용
    - `TossPaymentResponse`: 결제 승인 응답 매핑용

## Phase 2: 인프라 계층 구현 (Infrastructure)
**목표**: 토스 API와 통신하는 Client 구현 (Spring Boot 3.2+ `RestClient`)
- [x] **`TossPaymentConfig`**: `RestClient` Bean 설정 (Authorization Header 자동 주입 등)
- [x] **`TossPaymentClient`**: 실제 API 호출 메서드 구현
    - `issueBillingKey(String authKey, String customerKey)`: 빌링키 발급
    - `requestPayment(String billingKey, String orderId, Long amount, ...)`: 자동 결제 승인 요청

## Phase 3: 비즈니스 로직 구현 (Business Layer)
**목표**: 구독 생성 시 결제 흐름 제어 및 DB 트랜잭션 처리
- [ ] **`BillingService` 구현**:
    - 유저별 `customerKey` 생성/조회
    - `TossPaymentClient` 호출하여 빌링키 발급
    - `BillingKey` 엔티티 저장 (User 연관 관계 설정)
- [ ] **`PaymentService` 구현**:
    - 최초 구독 시 즉시 결제 처리
    - `Payment` 엔티티 저장 (결제 이력)
- [ ] **`SubscriptionService` 연동**:
    - 구독 생성 트랜잭션 내에서 `BillingService` -> `PaymentService` 순차 실행

## Phase 4: 테스트 및 검증 (Verification)
**목표**: 통합 테스트를 통한 결제 흐름 검증
- [ ] **단위 테스트**: `TossPaymentClient` Mocking을 통한 서비스 로직 테스트
- [ ] **통합 테스트**: 실제 토스 테스트 키(`test_sk_...`)를 사용한 E2E 테스트 (선택 사항)

---

## 🔗 참고 자료
- [토스페이먼츠 코어 API 문서 - 자동결제](https://docs.tosspayments.com/reference#%EC%9E%90%EB%8F%99%EA%B2%B0%EC%A0%9C)

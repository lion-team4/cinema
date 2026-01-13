# Toss Payments 정기결제 및 결제 테스트 구현 계획

## 1. 개요
이 문서는 토스페이먼츠의 **정기결제(빌링키 발급)**, **결제 승인**, **결제 이력 조회**, **빌링키 삭제** 기능을 테스트하기 위한 통합 구현 계획입니다.
기존 프로젝트의 **Entity (`User`, `Subscription`, `BillingKey`, `Payment`)**와 **Repository**를 최대한 활용하여 실제 서비스 흐름과 유사한 데이터 구조를 검증합니다.

## 2. 작업 대상 파일
*   **Controller**: `src/main/java/com/example/cinema/controller/test/BillingController.java`
*   **Service**: `src/main/java/com/example/cinema/service/test/BillingTestService.java`
*   **View**:
    *   `src/main/resources/templates/test/index.html` (빌링키 발급)
    *   `src/main/resources/templates/test/success.html` (발급 완료 및 기능 메뉴)
    *   `src/main/resources/templates/test/payment.html` (결제 요청 페이지 - *신규*)
    *   `src/main/resources/templates/test/history.html` (결제 이력 페이지 - *신규*)
    *   `src/main/resources/templates/test/fail.html` (실패 페이지)

## 3. 구현 로직 흐름

### A. 임시 유저 생성 (`GET /test/create-user`)
*   **목적**: 테스트 유저 생성.
*   **로직**: 닉네임 중복 체크 후 생성 -> `/test/billing` 리다이렉트.

### B. 빌링키 발급 (`GET /test/billing` -> `GET /test/success`)
1.  **발급 요청**: 프론트엔드 SDK로 카드 등록.
2.  **성공 처리 (`BillingTestService`)**:
    *   Toss API로 빌링키 발급.
    *   `BillingKey` Entity 저장.
    *   **중요**: `Subscription` Entity도 함께 생성하여 저장 (Payment와의 연관관계 형성을 위해 필수).
        *   Name: "테스트 정기구독"
        *   Price: 1000원 (기본값)
        *   Status: ACTIVE

### C. 결제 요청 (`GET/POST /test/pay`)
*   **UI**: 결제 금액 입력 폼 (`payment.html`).
*   **Service 로직**:
    1.  User의 활성화된 `Subscription` 조회.
    2.  연결된 `BillingKey` 조회.
    3.  `TossPaymentClient.requestPayment` 호출 (주문번호 생성).
    4.  `Payment` Entity 저장 (Success/Fail 상태 반영).
*   **결과**: 성공/실패 메시지 반환.

### D. 결제 이력 조회 (`GET /test/history`)
*   **UI**: 테이블 형태의 결제 내역 (`history.html`).
*   **Service 로직**:
    1.  User의 `Subscription` 조회.
    2.  `PaymentRepository.findBySubscription` 호출.
    3.  결과를 DTO 변환 후 View 전달.

### E. 빌링키 삭제/구독 해지 (`POST /test/billing/delete`)
*   **Service 로직**:
    1.  User의 `Subscription` 및 `BillingKey` 조회.
    2.  `BillingKey.status` -> `REVOKED`.
    3.  `Subscription.status` -> `CANCELED`.
    4.  DB 업데이트.
*   **결과**: 초기 화면(`index.html`)으로 리다이렉트.

## 4. 상세 구현 명세

### 4.1 BillingTestService 메서드
```java
// 빌링키 발급 + 구독 생성
public TossBillingResponse registerBillingKey(String authKey, String customerKey);

// 결제 승인 요청
public TossPaymentResponse pay(String customerKey, Long amount);

// 결제 이력 조회
public List<PaymentHistoryDto> getHistory(String customerKey);

// 빌링키 삭제 (구독 취소)
public void removeBillingKey(String customerKey);
```

### 4.2 URL 매핑 (BillingController)
*   `GET /test/billing`: 발급 페이지
*   `GET /test/success`: 발급 완료 처리
*   `GET /test/pay`: 결제 페이지 이동
*   `POST /test/pay`: 결제 실행
*   `GET /test/history`: 이력 조회
*   `POST /test/billing/delete`: 삭제 실행

### 4.3 Configuration
*   `application.yaml`의 `toss.client-key` 사용.

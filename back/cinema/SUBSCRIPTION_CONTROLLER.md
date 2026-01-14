# 구독 결제 Controller 구현 문서

## 개요

기존의 테스트용 `BillingController`는 nickname을 통해 유저 정보를 조회하는 방식으로 구현되어 있었습니다. 이번 구현에서는 JWT 인증을 통해 현재 로그인한 사용자의 정보를 가져와 구독 결제 서비스를 이용할 수 있는 REST API Controller를 구현했습니다.

## 구현 배경

- **기존 문제점**: `BillingController`는 테스트 목적으로 nickname 파라미터를 통해 유저를 찾아 구독 결제를 처리하고 있었습니다.
- **개선 사항**: 실제 프로덕션 환경에 적합하도록 JWT 토큰을 통한 인증 기반 Controller를 구현하여 보안을 강화하고 표준적인 REST API를 제공합니다.

## 구현 내용

### 1. SubscriptionController 생성

**파일 위치**: `back/cinema/src/main/java/com/example/cinema/controller/subscription/SubscriptionController.java`

JWT 인증을 사용하여 구독 관련 모든 기능을 제공하는 REST Controller를 구현했습니다.

### 2. 주요 기능

#### 2.1 구독 생성 (POST /users/subscriptions)

- **인증**: JWT 토큰 필요
- **기능**: 빌링키 발급 및 구독 생성, 초기 결제 처리
- **Request Body**: `SubscriptionCreateRequest`
  - `authKey`: PG사(프론트엔드 SDK)로부터 발급받은 1회성 인증 키
- **Response**: `ApiResponse<FirstSubscriptionResponse>`
  - 구독 정보와 초기 결제 내역을 함께 반환

#### 2.2 내 구독 정보 조회 (GET /users/subscription)

- **인증**: JWT 토큰 필요
- **기능**: 현재 로그인한 사용자의 구독 정보 조회
- **Response**: `ApiResponse<SubscriptionResponse>`
  - 구독 ID, 플랜 이름, 가격, 상태, 다음 결제 예정일 등 포함

#### 2.3 결제 내역 조회 (GET /users/subscriptions)

- **인증**: JWT 토큰 필요
- **기능**: 구독 결제 내역을 페이징 처리하여 조회
- **Query Parameters**:
  - `startDate` (Optional): 조회 시작 날짜 (ISO 8601 형식, 예: `2024-01-01T00:00:00`)
  - `endDate` (Optional): 조회 종료 날짜 (ISO 8601 형식)
  - `page` (Optional): 페이지 번호 (기본값: 0)
  - `size` (Optional): 페이지당 개수 (기본값: 20)
- **Response**: `ApiResponse<PageResponse<PaymentHistoryResponse>>`
  - 페이징 정보와 함께 결제 내역 목록 반환

#### 2.4 결제 수단 변경 (PUT /users/subscriptions)

- **인증**: JWT 토큰 필요
- **기능**: 구독 결제 수단(빌링키) 변경
- **Request Body**: `SubscriptionUpdateBillingRequest`
  - `authKey`: 새로 발급받은 1회성 인증 키
- **Response**: `ApiResponse<SubscriptionResponse>`
  - 업데이트된 구독 정보 반환

#### 2.5 구독 해지 (DELETE /users/subscriptions)

- **인증**: JWT 토큰 필요
- **기능**: 구독 해지 처리
- **Response**: `ApiResponse<Void>`
  - 성공 메시지만 반환

### 3. 인증 처리

모든 엔드포인트는 `@AuthenticationPrincipal CustomUserDetails userDetails`를 통해 현재 로그인한 사용자 정보를 가져옵니다.

```java
@AuthenticationPrincipal CustomUserDetails userDetails
```

- JWT 토큰은 `Authorization: Bearer {token}` 헤더로 전달됩니다.
- `JwtAuthenticationFilter`가 요청을 가로채 토큰을 검증하고 `SecurityContext`에 인증 정보를 설정합니다.
- `CustomUserDetails`에서 `userDetails.getUser().getUserId()`를 통해 사용자 ID를 추출합니다.

### 4. 날짜 파라미터 처리

결제 내역 조회 API의 날짜 파라미터는 `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)` 어노테이션을 사용하여 ISO 8601 형식으로 파싱됩니다.

**예시**:
```
GET /users/subscriptions?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59&page=0&size=20
```

### 5. 에러 처리

서비스 레이어에서 발생하는 예외들은 그대로 전파되어 Spring의 기본 예외 처리 메커니즘에 의해 처리됩니다.

주요 예외:
- `IllegalArgumentException`: 유저를 찾을 수 없는 경우
- `IllegalStateException`: 이미 활성화된 구독이 있거나, 구독 정보를 찾을 수 없는 경우
- `RuntimeException`: 결제 요청 중 오류 발생

## Postman 테스트 가이드

### 사전 준비

1. **서버 실행**: Spring Boot 애플리케이션이 실행 중이어야 합니다 (기본 포트: 8080)
2. **Postman 설치**: Postman 애플리케이션을 설치합니다

### 1단계: Postman Environment 설정 (선택사항)

Postman에서 환경 변수를 설정하면 토큰을 쉽게 관리할 수 있습니다.

1. Postman 우측 상단의 **"Environments"** 클릭
2. **"+"** 버튼을 클릭하여 새 Environment 생성
3. Environment 이름 입력 (예: `Cinema API`)
4. 다음 변수 추가:
   - `base_url`: `http://localhost:8080`
   - `access_token`: (비어있음, 로그인 후 자동 설정)
5. **"Save"** 클릭
6. 생성한 Environment를 선택합니다

### 2단계: 회원가입 (선택사항)

이미 계정이 있다면 이 단계를 건너뛸 수 있습니다.

#### 요청 설정

- **Method**: `POST`
- **URL**: `{{base_url}}/auth/signup` 또는 `http://localhost:8080/auth/signup`
- **Headers**:
  - `Content-Type`: `application/json`
- **Body** (raw, JSON):
```json
{
  "email": "test@example.com",
  "password": "password123",
  "nickname": "테스트유저"
}
```

#### 응답 예시

```json
{
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "userId": 1,
    "email": "test@example.com",
    "nickname": "테스트유저"
  }
}
```

### 3단계: 로그인 (JWT 토큰 받기)

구독 API를 사용하기 위해 먼저 JWT 토큰을 발급받아야 합니다.

#### 요청 설정

- **Method**: `POST`
- **URL**: `{{base_url}}/auth/login` 또는 `http://localhost:8080/auth/login`
- **Headers**:
  - `Content-Type`: `application/json`
- **Body** (raw, JSON):
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

#### 응답 예시

```json
{
  "message": "로그인에 성공하였습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiYXV0aCI6IlJPTEVfVVNFUiIsImV4cCI6MTcwOTk5OTk5OX0...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiYXV0aCI6IlJPTEVfVVNFUiIsImV4cCI6MTcxMDYwNDc5OX0...",
    "tokenType": "Bearer"
  }
}
```

#### 토큰 저장 방법

**방법 1: Environment Variable 사용 (권장)**

1. 로그인 응답에서 `accessToken` 값을 복사
2. Postman 하단의 **"Tests"** 탭 클릭
3. 다음 스크립트 추가:
```javascript
if (pm.response.code === 200) {
    const jsonData = pm.response.json();
    pm.environment.set("access_token", jsonData.data.accessToken);
}
```
4. 요청 실행 후, Environment의 `access_token` 변수가 자동으로 설정됩니다

**방법 2: 수동 복사**

응답에서 `accessToken` 값을 복사하여 다음 단계의 `{JWT_TOKEN}` 부분에 붙여넣습니다.

### 4단계: 구독 생성

#### 요청 설정

- **Method**: `POST`
- **URL**: `{{base_url}}/users/subscriptions` 또는 `http://localhost:8080/users/subscriptions`
- **Headers**:
  - `Authorization`: `Bearer {{access_token}}` (Environment 사용 시) 또는 `Bearer {JWT_TOKEN}` (수동 입력 시)
  - `Content-Type`: `application/json`
- **Body** (raw, JSON):
```json
{
  "authKey": "실제_프론트엔드에서_받은_authKey_값"
}
```

⚠️ **중요 참고사항**: 
- `authKey`는 **프론트엔드에서 Toss Payments SDK를 통해 카드 정보를 입력한 후 발급받는 1회성 인증 키**입니다.
- **Postman으로 직접 테스트하기 어려운 이유**: `authKey`를 얻으려면 실제 프론트엔드에서 Toss Payments 위젯을 통해 카드 정보를 입력해야 합니다.
- **테스트 방법**:
  1. **프론트엔드 통합**: Toss Payments SDK를 사용하는 프론트엔드 페이지를 통해 실제 카드 입력 후 받은 `authKey`를 사용
  2. **테스트 페이지 활용**: 프로젝트에 있는 `/test/billing` 같은 테스트 페이지를 사용하여 `authKey` 발급
- **에러 발생 시**: "authKey가 유효하지 않거나 만료되었습니다" 에러가 발생하면, 프론트엔드에서 새로운 `authKey`를 발급받아야 합니다.

#### 응답 예시

```json
{
  "message": "구독 생성이 완료되었습니다.",
  "data": {
    "subscription": {
      "subscriptionId": 1,
      "subscriberNickname": "테스트유저",
      "planName": "기본요금제",
      "price": 10000,
      "status": "ACTIVE",
      "nextPaymentDate": "2024-02-01T00:00:00"
    },
    "payment": {
      "paymentId": 1,
      "subscriptionId": 1,
      "planName": "기본요금제",
      "amount": 10000,
      "status": "APPROVED",
      "paidAt": "2024-01-01T00:00:00"
    }
  }
}
```

### 5단계: 구독 정보 조회

#### 요청 설정

- **Method**: `GET`
- **URL**: `{{base_url}}/users/subscription` 또는 `http://localhost:8080/users/subscription`
- **Headers**:
  - `Authorization`: `Bearer {{access_token}}` 또는 `Bearer {JWT_TOKEN}`

#### 응답 예시

```json
{
  "message": "구독 정보 조회 성공",
  "data": {
    "subscriptionId": 1,
    "subscriberNickname": "테스트유저",
    "planName": "기본요금제",
    "price": 10000,
    "status": "ACTIVE",
    "nextPaymentDate": "2024-02-01T00:00:00"
  }
}
```

### 6단계: 결제 내역 조회

#### 요청 설정

- **Method**: `GET`
- **URL**: `{{base_url}}/users/subscriptions` 또는 `http://localhost:8080/users/subscriptions`
- **Headers**:
  - `Authorization`: `Bearer {{access_token}}` 또는 `Bearer {JWT_TOKEN}`
- **Params** (Query Parameters):
  - `startDate` (Optional): `2024-01-01T00:00:00`
  - `endDate` (Optional): `2024-12-31T23:59:59`
  - `page` (Optional): `0` (기본값: 0)
  - `size` (Optional): `20` (기본값: 20)

**참고**: 날짜 형식은 ISO 8601 형식입니다 (`YYYY-MM-DDTHH:mm:ss`)

#### 응답 예시

```json
{
  "message": "결제 내역 조회 성공",
  "data": {
    "content": [
      {
        "paymentId": 1,
        "subscriptionId": 1,
        "planName": "기본요금제",
        "amount": 10000,
        "status": "APPROVED",
        "paidAt": "2024-01-01T00:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

### 7단계: 결제 수단 변경

#### 요청 설정

- **Method**: `PUT`
- **URL**: `{{base_url}}/users/subscriptions` 또는 `http://localhost:8080/users/subscriptions`
- **Headers**:
  - `Authorization`: `Bearer {{access_token}}` 또는 `Bearer {JWT_TOKEN}`
  - `Content-Type`: `application/json`
- **Body** (raw, JSON):
```json
{
  "authKey": "new_toss_billing_key_67890"
}
```

#### 응답 예시

```json
{
  "message": "결제 수단 변경이 완료되었습니다.",
  "data": {
    "subscriptionId": 1,
    "subscriberNickname": "테스트유저",
    "planName": "기본요금제",
    "price": 10000,
    "status": "ACTIVE",
    "nextPaymentDate": "2024-02-01T00:00:00"
  }
}
```

### 8단계: 구독 해지

#### 요청 설정

- **Method**: `DELETE`
- **URL**: `{{base_url}}/users/subscriptions` 또는 `http://localhost:8080/users/subscriptions`
- **Headers**:
  - `Authorization`: `Bearer {{access_token}}` 또는 `Bearer {JWT_TOKEN}`

#### 응답 예시

```json
{
  "message": "구독 해지가 완료되었습니다.",
  "data": null
}
```

### 테스트 순서 요약

1. ✅ **회원가입** (선택사항) → POST `/auth/signup`
2. ✅ **로그인** → POST `/auth/login` → `accessToken` 저장
3. ✅ **구독 생성** → POST `/users/subscriptions`
4. ✅ **구독 정보 조회** → GET `/users/subscription`
5. ✅ **결제 내역 조회** → GET `/users/subscriptions`
6. ✅ **결제 수단 변경** → PUT `/users/subscriptions`
7. ✅ **구독 해지** → DELETE `/users/subscriptions`

### 주의사항

1. **토큰 만료**: Access Token은 30분 후 만료됩니다. 만료 시 로그인 API를 다시 호출하여 새 토큰을 받아야 합니다.
2. **인증 필수**: 모든 구독 API는 `Authorization: Bearer {token}` 헤더가 필요합니다.
3. **authKey**: 구독 생성 및 결제 수단 변경 시 사용하는 `authKey`는 실제 Toss Payments SDK에서 발급받은 값이어야 합니다. (테스트 환경에서는 테스트 키 사용 가능)

## 사용 예시 (cURL)

### 1. 구독 생성

```bash
curl -X POST http://localhost:8080/users/subscriptions \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "authKey": "toss_billing_key_12345"
  }'
```

### 2. 구독 정보 조회

```bash
curl -X GET http://localhost:8080/users/subscription \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### 3. 결제 내역 조회

```bash
curl -X GET "http://localhost:8080/users/subscriptions?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59&page=0&size=20" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### 4. 결제 수단 변경

```bash
curl -X PUT http://localhost:8080/users/subscriptions \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "authKey": "new_toss_billing_key_67890"
  }'
```

### 5. 구독 해지

```bash
curl -X DELETE http://localhost:8080/users/subscriptions \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

## 코드 구조

### Controller 구조

```java
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "구독 관련 API")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    
    // 각종 엔드포인트 메서드들...
}
```

### 의존성

- `SubscriptionService`: 구독 관련 비즈니스 로직 처리
- `CustomUserDetails`: 현재 로그인한 사용자 정보 (Spring Security)

### 응답 형식

모든 응답은 `ApiResponse<T>` 래퍼로 감싸져 반환됩니다:

```java
ApiResponse<T> {
    String message;  // 성공/에러 메시지
    T data;          // 실제 데이터
}
```

## 보안 고려사항

1. **JWT 인증 필수**: 모든 엔드포인트는 JWT 토큰 인증을 필요로 합니다.
2. **사용자 분리**: `@AuthenticationPrincipal`을 통해 현재 로그인한 사용자만 자신의 구독 정보에 접근할 수 있습니다.
3. **입력 검증**: `@Valid` 어노테이션을 통해 요청 데이터의 유효성을 검증합니다.

## 기존 코드와의 차이점

### BillingController (테스트용)

- `@Controller` 사용 (뷰 반환)
- nickname 파라미터로 유저 조회
- 테스트 페이지 렌더링

### SubscriptionController (프로덕션용)

- `@RestController` 사용 (JSON 응답)
- JWT 토큰 기반 인증
- 표준 REST API 스타일
- Swagger 문서화 지원

## API 스펙 문서

자세한 API 스펙은 `API.md` 파일의 Subscription 섹션을 참고하세요.

## 향후 개선 사항

1. 예외 처리 통일 (GlobalExceptionHandler)
2. API 버전 관리 (v1, v2 등)
3. 상세한 에러 응답 메시지
4. 결제 내역 필터링 옵션 추가 (상태별, 금액별 등)


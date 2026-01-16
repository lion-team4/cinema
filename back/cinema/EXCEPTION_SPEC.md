# Cinema 프로젝트 예외 처리 규격 (Exception Handling Specification)

## 1. 개요
Cinema 프로젝트는 모든 비즈니스 로직 예외 및 시스템 예외를 중앙 집중식으로 관리하며, 클라이언트에게 일관된 에러 응답 포맷을 제공합니다.

---

## 2. 핵심 구성 요소

### 2.1 ErrorCode (Enum)
에러의 종류를 정의하며 HTTP 상태 코드, 내부 에러 코드, 기본 메시지를 관리합니다.
- **위치**: `com.example.cinema.exception.ErrorCode`
- **구조**: `HttpStatus status`, `String code`, `String message`

### 2.2 BusinessException (RuntimeException)
모든 커스텀 예외의 최상위 클래스입니다.
- **특징**: `ErrorCode`를 필수로 포함하며, 필요 시 상세 메시지를 오버라이딩할 수 있습니다.

### 2.3 GlobalExceptionHandler (@RestControllerAdvice)
애플리케이션 전역에서 발생하는 예외를 가로채서 처리합니다.
- **처리 대상**: `BusinessException`, `MethodArgumentNotValidException`, `AccessDeniedException`, 일반 `Exception` 등.

---

## 3. 에러 응답 포맷 (Error Response)
에러 발생 시 `ApiResponse` 형식을 통해 JSON 데이터를 반환합니다.

```json
{
  "message": "상세 에러 메시지 (한국어)",
  "data": null
}
```

---

## 4. 주요 에러 코드 정의

### 4.1 공통 (Common)
| 코드 | 상태 | 메시지 | 설명 |
| :--- | :--- | :--- | :--- |
| C001 | 400 | 잘못된 입력값입니다. | @Valid 검증 실패 또는 인자값 오류 |
| C002 | 405 | 지원하지 않는 HTTP 메서드입니다. | GET/POST 등 매핑 오류 |
| C003 | 404 | 데이터를 찾을 수 없습니다. | 엔티티 조회 실패 |
| C004 | 500 | 서버 내부 오류가 발생했습니다. | 시스템 처리 실패 |
| C005 | 403 | 접근 권한이 없습니다. | 권한 부족 (Spring Security) |

### 4.2 사용자 및 인증 (User & Auth)
| 코드 | 상태 | 메시지 | 설명 |
| :--- | :--- | :--- | :--- |
| U001 | 400 | 이미 사용 중인 이메일입니다. | 이메일 중복 |
| U002 | 404 | 사용자를 찾을 수 없습니다. | 사용자 ID/이메일 조회 실패 |
| U003 | 401 | 이메일 또는 비밀번호가 일치하지 않습니다. | 로그인 실패 |
| U004 | 401 | 유효하지 않은 토큰입니다. | JWT 만료 또는 위변조 |
| U005 | 400 | 이미 사용 중인 닉네임입니다. | 닉네임 중복 |

### 4.3 구독 및 일정 (Subscription & Content)
| 코드 | 상태 | 메시지 | 설명 |
| :--- | :--- | :--- | :--- |
| S001 | 409 | 이미 이용 중인 구독이 존재합니다. | 중복 구독 시도 |
| P001 | 400 | 결제 승인에 실패했습니다. | Toss API 승인 거절 |
| CT001 | 404 | 콘텐츠를 찾을 수 없습니다. | 영화/영상 정보 부재 |
| SC003 | 409 | 상영 일정이 겹칩니다. | 스케줄 중복 등록 시도 |

---

## 5. 사용 가이드 (How to use)

### 5.1 서비스 레이어에서 예외 던지기
```java
// 1. ErrorCode에 정의된 기본 메시지 사용
throw new BusinessException(ErrorCode.USER_NOT_FOUND);

// 2. 상황에 맞는 구체적인 메시지 전달 (권장)
throw new BusinessException("해당 영화(ID: 10)를 찾을 수 없습니다.", ErrorCode.CONTENT_NOT_FOUND);
```

### 5.2 컨트롤러 검증 예외
`@Valid` 어노테이션을 통한 DTO 검증 실패 시 `C001` 코드로 자동 처리됩니다.

---

## 6. 테스트 현황
- **GlobalExceptionHandlerTest**: `StandaloneSetup` 기반으로 모든 주요 예외 시나리오 검증 완료 (2026-01-16).
- **검증 항목**: BusinessException, IllegalArgumentException, AccessDeniedException, MethodNotAllowed 등.

# 🎬 WebSocket 채팅 & 스케줄 테스트 매뉴얼

이 문서는 Cinema 프로젝트의 실시간 채팅 기능과 스케줄 상태 변화를 테스트하기 위한 가이드입니다.

---

## 📋 목차

1. [사전 준비](#1-사전-준비)
2. [테스트 데이터 생성](#2-테스트-데이터-생성)
3. [스케줄 상태 테스트](#3-스케줄-상태-테스트) ⭐ NEW
4. [채팅 테스트 실행](#4-채팅-테스트-실행)
5. [멀티 유저 테스트](#5-멀티-유저-테스트)
6. [문제 해결](#6-문제-해결)

---

## 1. 사전 준비

### 1.1 서버 실행

Spring Boot 애플리케이션을 실행합니다.

```bash
cd back/cinema
./gradlew bootRun
```

또는 IDE에서 `CinemaApplication.java`를 실행합니다.

### 1.2 프로필 확인

WebSocket 테스트 기능은 **dev** 또는 **test** 프로필에서만 활성화됩니다.

`application.yaml`에서 프로필을 확인하세요:

```yaml
spring:
  profiles:
    active: dev  # 또는 test
```

---

## 2. 테스트 데이터 생성

채팅을 테스트하려면 **사용자**와 **스케줄(채팅방)**이 필요합니다.

### 2.1 테스트 사용자 생성

브라우저 주소창에 다음 URL을 입력합니다:

```
http://localhost:8080/test/data/create-subscriber
```

**결과:**
```json
{
  "success": true,
  "message": "테스트 구독자 생성 완료",
  "data": {
    "userId": 1,
    "email": "subscriber@test.com",
    "nickname": "구독자",
    "subscriptionActive": true,
    "message": "비밀번호: 12345678"
  }
}
```

> 💡 **기본 계정 정보**
> - 이메일: `subscriber@test.com`
> - 비밀번호: `12345678`

### 2.2 다른 이름으로 사용자 생성 (선택사항)

커스텀 사용자를 생성하려면:

```
http://localhost:8080/test/data/create-subscriber?email=user2@test.com&nickname=유저2&password=12345678
```

### 2.3 테스트 스케줄(채팅방) 생성

먼저 셀러(콘텐츠 소유자)를 생성합니다:

```
http://localhost:8080/test/data/create-seller
```

그 다음 상영 중인 스케줄을 생성합니다:

```
http://localhost:8080/test/data/create-playing-schedule
```

**결과:**
```json
{
  "success": true,
  "message": "PLAYING 상태 스케줄 생성 완료",
  "data": {
    "contentId": 1,
    "scheduleItemId": 1,  // ← 이것이 채팅방 번호입니다!
    "status": "PLAYING",
    "startAt": "2024-01-15T10:00:00",
    "endAt": "2024-01-15T12:00:00"
  }
}
```

> ⚠️ **중요**: `scheduleItemId`가 채팅방 번호입니다. 이 번호를 기억하세요!

---

## 3. 스케줄 상태 테스트 ⭐ NEW

스케줄러가 상태를 자동으로 변경하는 것을 확인할 수 있습니다.

### 3.1 스케줄 테스트 페이지 접속

```
http://localhost:8080/test/schedule
```

### 3.2 스케줄 상태 흐름

```
CLOSED → WAITING → PLAYING → ENDING → CLOSED
         (시작 10분 전)  (시작 시각)  (종료 시각)  (종료+10분)
```

### 3.3 빠른 테스트 설정

상태 변화를 빠르게 확인하려면:

| 설정 | 값 | 설명 |
|------|-----|------|
| 시작까지 (분) | `1` | 1분 후 시작 |
| 상영 시간 (분) | `2` | 2분간 상영 |

**예상 타임라인:**
- 0분: 스케줄 생성 (CLOSED)
- 바로: WAITING으로 변경 (시작 10분 이내이므로)
- 1분 후: PLAYING으로 변경
- 3분 후: ENDING으로 변경
- 13분 후: CLOSED로 변경

### 3.4 상태별 설명

| 상태 | 의미 | 입장 가능 |
|------|------|----------|
| `CLOSED` | 상영 전/후 (입장 불가) | ❌ |
| `WAITING` | 시작 10분 전 ~ 시작 (대기 중) | ✅ |
| `PLAYING` | 상영 중 | ✅ |
| `ENDING` | 종료 ~ 종료+10분 (마무리) | ❌ |

> 💡 스케줄러는 **10초마다** 상태를 체크합니다.

---

## 4. 채팅 테스트 실행

### 4.1 로그인 페이지 접속

브라우저에서 다음 URL로 접속합니다:

```
http://localhost:8080/test/ws/login
```

### 4.2 로그인 정보 입력

| 필드 | 값 |
|------|-----|
| 이메일 | `subscriber@test.com` |
| 비밀번호 | `12345678` |
| 스케줄 ID | `1` (위에서 생성된 scheduleItemId) |

**[로그인 & 채팅방 입장]** 버튼을 클릭합니다.

### 4.3 WebSocket 연결

채팅 페이지가 열리면:

1. **[🔌 연결]** 버튼을 클릭합니다.
2. 상태 표시가 "연결됨 ✓"로 바뀌면 연결 성공입니다.
3. 메시지를 입력하고 **[전송]** 버튼을 클릭하거나 Enter 키를 누릅니다.

### 4.4 직접 토큰으로 접속 (선택사항)

이미 액세스 토큰이 있다면 직접 채팅 페이지로 접속할 수 있습니다:

```
http://localhost:8080/test/ws/chat?scheduleId=1&token=YOUR_ACCESS_TOKEN&nickname=닉네임
```

---

## 5. 멀티 유저 테스트

실시간 채팅을 제대로 테스트하려면 여러 사용자가 필요합니다.

### 5.1 두 번째 사용자 생성

```
http://localhost:8080/test/data/create-subscriber?email=user2@test.com&nickname=친구&password=12345678
```

### 5.2 다른 브라우저/탭에서 접속

1. **Chrome 시크릿 모드** 또는 **다른 브라우저**(Firefox, Edge 등)를 엽니다.
2. `http://localhost:8080/test/ws/login` 접속
3. 두 번째 계정으로 로그인:
   - 이메일: `user2@test.com`
   - 비밀번호: `12345678`
   - 스케줄 ID: `1` (같은 채팅방)

### 5.3 실시간 채팅 확인

이제 두 창에서 메시지를 주고받으면 **실시간으로** 다른 창에 표시됩니다! 🎉

---

## 6. 문제 해결

### ❌ "연결 실패" 오류

**원인 1: 토큰 만료**
- 로그인 페이지로 돌아가서 다시 로그인하세요.

**원인 2: 서버가 실행되지 않음**
- 터미널에서 서버가 정상 실행 중인지 확인하세요.

### ❌ "사용자를 찾을 수 없습니다"

- 테스트 데이터를 먼저 생성했는지 확인하세요.
- `/test/data/create-subscriber` 호출 필요

### ❌ 메시지가 다른 창에 안 보임

- 두 사용자가 **같은 스케줄 ID**로 접속했는지 확인하세요.
- 두 창 모두 **[연결]** 버튼을 클릭했는지 확인하세요.

### ❌ 토큰이 보이지 않음

- 로그인 후 채팅 페이지 하단의 "Token:" 영역에 토큰이 표시됩니다.
- 토큰이 비어있으면 로그인에 실패한 것입니다.

---

## 📚 API 참고

### 테스트 데이터 생성 API

| 엔드포인트 | 설명 |
|-----------|------|
| `GET /test/data/create-subscriber` | 구독자 생성 |
| `GET /test/data/create-seller` | 판매자(셀러) 생성 |
| `GET /test/data/create-playing-schedule` | 상영 중 스케줄 생성 |
| `GET /test/data/setup-all` | 전체 테스트 데이터 한번에 생성 |

### WebSocket 테스트 페이지

| URL | 설명 |
|-----|------|
| `/test/ws/login` | 로그인 페이지 |
| `/test/ws/chat?scheduleId=1` | 채팅 페이지 (토큰 필요) |
| `/test/schedule` | 스케줄 상태 테스트 페이지 ⭐ |

### WebSocket 엔드포인트 (개발자용)

| 타입 | 경로 | 설명 |
|------|------|------|
| 연결 | `/ws-sockjs` | SockJS 연결 (브라우저용) |
| 연결 | `/ws` | 순수 WebSocket 연결 |
| 전송 | `/app/chat/{scheduleId}` | 채팅 메시지 전송 |
| 구독 | `/topic/theaters/{scheduleId}/chat` | 채팅 메시지 수신 |

---

## 🔐 인증 방식

WebSocket 연결 시 STOMP 헤더에 JWT 토큰을 포함해야 합니다:

```javascript
const headers = {
    'Authorization': 'Bearer ' + accessToken
};
stompClient.connect(headers, onConnected, onError);
```

---

## 📝 빠른 시작 (Quick Start)

### 🚀 스케줄 상태 테스트 (권장)

```
1. 서버 실행

2. 스케줄 테스트 페이지 접속:
   http://localhost:8080/test/schedule

3. "시작까지 1분, 상영시간 2분"으로 스케줄 생성

4. 상태 변화 관찰:
   - 생성 직후: WAITING (시작 10분 이내이므로)
   - 1분 후: PLAYING
   - 3분 후: ENDING
   - 13분 후: CLOSED
```

### 💬 채팅 테스트

```
1. 테스트 유저 DB에 직접 생성 (또는 /test/data/create-subscriber 사용)

2. 스케줄 테스트 페이지에서 스케줄 생성:
   http://localhost:8080/test/schedule

3. 채팅 테스트:
   http://localhost:8080/test/ws/login
   
4. 로그인 정보:
   - 이메일: subscriber@test.com
   - 비밀번호: 12345678
   - 스케줄 ID: (생성된 스케줄 ID)

5. [연결] 버튼 클릭 → 메시지 전송!
```

---

*마지막 업데이트: 2024년 1월*

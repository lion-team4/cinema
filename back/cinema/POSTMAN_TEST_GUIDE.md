# 🎬 Cinema API 테스트 가이드 (Postman)

## 📋 목차
1. [사전 준비](#1-사전-준비)
2. [테스트 데이터 생성](#2-테스트-데이터-생성)
3. [인증 (로그인)](#3-인증-로그인)
4. [상영관 입장/퇴장 테스트](#4-상영관-입장퇴장-테스트)
5. [재생 정보 조회](#5-재생-정보-조회)
6. [WebSocket 테스트](#6-websocket-테스트)
7. [전체 시나리오 흐름](#7-전체-시나리오-흐름)

---

## 1. 사전 준비

### 서버 실행
```bash
cd back/cinema
./gradlew bootRun --args='--spring.profiles.active=test'
```

### Postman 환경 변수 설정
```
base_url: http://localhost:8080
access_token: (로그인 후 자동 설정)
```

---

## 2. 테스트 데이터 생성

### 2-1. 전체 데이터 한번에 생성 (추천)

```http
GET {{base_url}}/test-data/setup-all
```

**Response:**
```json
{
  "code": 200,
  "message": "전체 테스트 데이터 생성 완료",
  "data": {
    "seller": {
      "email": "seller@test.com",
      "password": "12345678"
    },
    "subscriber": {
      "email": "subscriber@test.com",
      "password": "12345678"
    },
    "message": "모든 테스트 데이터가 생성되었습니다. /auth/login으로 로그인하세요."
  }
}
```

### 2-2. 개별 생성 (선택)

#### 셀러(판매자) 생성
```http
GET {{base_url}}/test-data/create-seller?email=seller@test.com&nickname=테스트셀러&password=12345678
```

#### 구독자 생성 (구독 활성화 상태)
```http
GET {{base_url}}/test-data/create-subscriber?email=subscriber@test.com&nickname=테스트구독자&password=12345678
```

#### PLAYING 상태 스케줄 생성
```http
GET {{base_url}}/test-data/create-playing-schedule?sellerEmail=seller@test.com&title=테스트영화&durationMinutes=120
```

**Response:**
```json
{
  "code": 200,
  "message": "PLAYING 상태 스케줄 생성 완료",
  "data": {
    "contentId": 1,
    "contentTitle": "테스트영화",
    "scheduleItemId": 1,  // ⬅️ 이 ID를 사용!
    "status": "PLAYING",
    "startAt": "2026-01-16T23:15:00",
    "endAt": "2026-01-17T01:15:00"
  }
}
```

#### WAITING 상태 스케줄 생성
```http
GET {{base_url}}/test-data/create-waiting-schedule?sellerEmail=seller@test.com&title=대기영화&startsInMinutes=10
```

---

## 3. 인증 (로그인)

### 3-1. 구독자로 로그인

```http
POST {{base_url}}/auth/login
Content-Type: application/json

{
  "email": "subscriber@test.com",
  "password": "12345678"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "로그인에 성공하였습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### 3-2. Postman에서 토큰 자동 저장

**Tests 탭에 추가:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("access_token", jsonData.data.accessToken);
}
```

---

## 4. 상영관 입장/퇴장 테스트

### 4-1. 상영관 입장

```http
POST {{base_url}}/theaters/1/enter
Authorization: Bearer {{access_token}}
```

**성공 Response:**
```json
{
  "code": 200,
  "message": "상영관 입장 성공",
  "data": {
    "watchId": 1,
    "scheduleItemId": 1,
    "contentTitle": "테스트영화",
    "enteredAt": "2026-01-16T23:20:00"
  }
}
```

**실패 케이스:**

| 상황 | 에러 메시지 |
|------|------------|
| 구독 미활성 | `구독이 필요한 서비스입니다.` |
| CLOSED 상태 | `현재 입장할 수 없는 상영관입니다. 상태: CLOSED` |
| 이미 입장 | `이미 입장한 상영관입니다.` |

### 4-2. 현재 시청자 수 조회

```http
GET {{base_url}}/theaters/1/viewers
Authorization: Bearer {{access_token}}
```

**Response:**
```json
{
  "code": 200,
  "message": "시청자 수 조회 성공",
  "data": 1
}
```

### 4-3. 상영관 퇴장

```http
POST {{base_url}}/theaters/1/leave
Authorization: Bearer {{access_token}}
```

**Response:**
```json
{
  "code": 200,
  "message": "상영관 퇴장 성공",
  "data": {
    "watchId": 1,
    "scheduleItemId": 1,
    "contentTitle": "테스트영화",
    "enteredAt": "2026-01-16T23:20:00",
    "leftAt": "2026-01-16T23:25:00"
  }
}
```

---

## 5. 재생 정보 조회

### 5-1. 재생 정보 (Playback Info)

```http
GET {{base_url}}/theaters/1/playback
Authorization: Bearer {{access_token}}
```

**Response:**
```json
{
  "code": 200,
  "message": "재생 정보 조회 성공",
  "data": {
    "assetId": 1,
    "videoUrl": "https://dxxxxx.cloudfront.net/test/hls/master.m3u8",
    "contentType": "application/vnd.apple.mpegurl",
    "durationMs": 7200000
  }
}
```

> ⚠️ **참고:** 테스트 데이터의 `videoUrl`은 실제 CloudFront에 파일이 없어서 재생은 안 됩니다.

### 5-2. 상영 상태 (Playback State)

```http
GET {{base_url}}/theaters/1/state
```

**Response:**
```json
{
  "code": 200,
  "message": "상영 상태 조회 성공",
  "data": {
    "status": "PLAYING",
    "playing": true,
    "positionMs": 300000,
    "playbackRate": 1.0,
    "serverTimeMs": 1737043200000
  }
}
```

| 필드 | 설명 |
|------|------|
| `status` | `WAITING`, `PLAYING`, `ENDING`, `CLOSED` |
| `playing` | 현재 재생 중 여부 |
| `positionMs` | 현재 재생 위치 (밀리초) |
| `serverTimeMs` | 서버 시간 (동기화용) |

---

## 6. WebSocket 테스트

### 6-1. Postman WebSocket 사용

1. **New** → **WebSocket Request** 선택
2. URL: `ws://localhost:8080/ws`

### 6-2. STOMP 연결 순서

#### Step 1: WebSocket 연결
```
ws://localhost:8080/ws
```

#### Step 2: STOMP CONNECT 프레임 전송
```
CONNECT
Authorization:Bearer eyJhbGciOiJIUzI1NiJ9...
accept-version:1.2
heart-beat:10000,10000

^@
```
> `^@`는 NULL 문자 (Ctrl+Shift+@)

#### Step 3: CONNECTED 응답 확인
```
CONNECTED
version:1.2
heart-beat:0,0

^@
```

#### Step 4: 상태 구독
```
SUBSCRIBE
id:sub-0
destination:/topic/theaters/1/state

^@
```

### 6-3. Apic 또는 wscat 사용 (대안)

**wscat 설치:**
```bash
npm install -g wscat
```

**연결:**
```bash
wscat -c ws://localhost:8080/ws
```

> ⚠️ **주의:** 순수 WebSocket은 STOMP 프레임을 직접 작성해야 해서 복잡합니다. 
> 프론트엔드 테스트 페이지 사용을 권장합니다.

---

## 7. 전체 시나리오 흐름

### 시나리오: 구독자가 영화 시청

```mermaid
sequenceDiagram
    participant Client
    participant Server
    participant WebSocket
    
    Note over Client: 1. 테스트 데이터 생성
    Client->>Server: GET /test-data/setup-all
    Server-->>Client: 셀러, 구독자, 스케줄 생성 완료
    
    Note over Client: 2. 로그인
    Client->>Server: POST /auth/login
    Server-->>Client: accessToken 발급
    
    Note over Client: 3. 상영관 입장
    Client->>Server: POST /theaters/1/enter
    Server-->>Client: watchId, enteredAt
    
    Note over Client: 4. 재생 정보 조회
    Client->>Server: GET /theaters/1/playback
    Server-->>Client: videoUrl (HLS)
    
    Note over Client: 5. WebSocket 연결
    Client->>WebSocket: CONNECT (with JWT)
    WebSocket-->>Client: CONNECTED
    
    Note over Client: 6. 상태 구독
    Client->>WebSocket: SUBSCRIBE /topic/theaters/1/state
    WebSocket-->>Client: 주기적 상태 업데이트
    
    Note over Client: 7. 영화 시청 중...
    
    Note over Client: 8. 퇴장
    Client->>Server: POST /theaters/1/leave
    Server-->>Client: leftAt 기록
```

### Postman Collection 순서

| # | Method | Endpoint | 설명 |
|---|--------|----------|------|
| 1 | GET | `/test-data/setup-all` | 테스트 데이터 생성 |
| 2 | POST | `/auth/login` | 구독자 로그인 |
| 3 | POST | `/theaters/1/enter` | 상영관 입장 |
| 4 | GET | `/theaters/1/viewers` | 시청자 수 확인 |
| 5 | GET | `/theaters/1/playback` | 재생 URL 조회 |
| 6 | GET | `/theaters/1/state` | 상영 상태 조회 |
| 7 | POST | `/theaters/1/leave` | 상영관 퇴장 |

---

## 📝 추가 테스트 케이스

### 비구독자 입장 시도

```http
# 1. 셀러로 로그인 (구독 없음)
POST {{base_url}}/auth/login
{
  "email": "seller@test.com",
  "password": "12345678"
}

# 2. 입장 시도 → 실패!
POST {{base_url}}/theaters/1/enter
Authorization: Bearer {{access_token}}
```

**Expected Response:**
```json
{
  "code": 403,
  "message": "구독이 필요한 서비스입니다."
}
```

### CLOSED 상태 입장 시도

1. 스케줄 상태가 `CLOSED`인 상영관에 입장 시도
2. `현재 입장할 수 없는 상영관입니다` 에러 발생

---

## 🔧 트러블슈팅

### 에러: "사용자를 찾을 수 없습니다"
→ `/test-data/setup-all` 먼저 실행

### 에러: "구독이 필요한 서비스입니다"
→ `subscriber@test.com`으로 로그인했는지 확인

### 에러: "스케줄을 찾을 수 없습니다"
→ `/test-data/create-playing-schedule` 실행하여 스케줄 생성

### WebSocket 연결 안 됨
→ JWT 토큰이 유효한지 확인, STOMP 프레임 형식 확인

---

## 📌 중요 참고사항

1. **테스트 데이터 API는 dev/test 프로필에서만 동작합니다**
2. **테스트용 영상 URL은 실제 재생되지 않습니다** (CloudFront에 파일 없음)
3. **WebSocket 테스트는 프론트엔드 환경에서 하는 것이 더 쉽습니다**
4. **스케줄 상태는 `ScheduleStatusScheduler`에 의해 자동 변경됩니다**

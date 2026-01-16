# 스트리밍 시스템 Q&A

## 목차
1. [데이터 흐름 이해하기](#데이터-흐름-이해하기)
2. [Q1: downloadToFile은 유저 로컬에서 실행되나?](#q1-downloadtofile은-유저-로컬에서-실행되나)
3. [Q2: S3 세그먼트 6초 관련](#q2-s3-세그먼트-6초-관련)
4. [Q3: Range 설정은 무엇인가?](#q3-range-설정은-무엇인가)
5. [Q4: 브로드캐스트와 서버/스레드 확장](#q4-브로드캐스트와-서버스레드-확장)

---

## 데이터 흐름 이해하기

### 🎬 전체 흐름 다이어그램

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        [1단계] 콘텐츠 업로드 및 인코딩                            │
└─────────────────────────────────────────────────────────────────────────────────┘

  Seller(감독)                    Spring Boot 서버                    AWS S3
      │                                │                                │
      │ ① 원본 영상 업로드             │                                │
      │ (Presigned URL 사용)           │                                │
      ├───────────────────────────────────────────────────────────────▶│
      │                                │                                │ source.mp4 저장
      │                                │                                │
      │ ② 업로드 완료 알림             │                                │
      ├───────────────────────────────▶│                                │
      │                                │                                │
      │                                │ ③ downloadToFile()             │
      │                                │    (서버 로컬에 다운로드)       │
      │                                │◀───────────────────────────────│
      │                                │                                │
      │                                │ ④ FFmpeg HLS 인코딩            │
      │                                │    /tmp/cinema-work/           │
      │                                │    ├── source.mp4              │
      │                                │    └── out/                    │
      │                                │        ├── index.m3u8          │
      │                                │        ├── seg_00000.ts        │
      │                                │        ├── seg_00001.ts        │
      │                                │        └── ...                 │
      │                                │                                │
      │                                │ ⑤ uploadFile()                 │
      │                                │    (인코딩 결과 업로드)         │
      │                                ├───────────────────────────────▶│
      │                                │                                │ hls/123/index.m3u8
      │                                │                                │ hls/123/seg_*.ts
      │                                │                                │
      │                                │ ⑥ 로컬 파일 삭제               │
      │                                │    safeDelete()                │

┌─────────────────────────────────────────────────────────────────────────────────┐
│                          [2단계] 시청자 스트리밍                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

  시청자 브라우저              Spring Boot 서버           CloudFront            S3
      │                            │                        │                   │
      │ ① GET /playback            │                        │                   │
      ├───────────────────────────▶│                        │                   │
      │◀───────────────────────────│                        │                   │
      │   { videoUrl: "https://    │                        │                   │
      │     cloudfront.net/hls/    │                        │                   │
      │     123/index.m3u8" }      │                        │                   │
      │                            │                        │                   │
      │ ② index.m3u8 요청 (직접!) │                        │                   │
      ├────────────────────────────────────────────────────▶│                   │
      │                            │                        │◀─ 캐시 미스 시 ──│
      │◀────────────────────────────────────────────────────│                   │
      │   #EXTM3U                   │                        │                   │
      │   #EXT-X-VERSION:3          │                        │                   │
      │   #EXT-X-TARGETDURATION:6   │                        │                   │
      │   #EXTINF:6.000,            │                        │                   │
      │   seg_00000.ts              │                        │                   │
      │   ...                       │                        │                   │
      │                            │                        │                   │
      │ ③ 세그먼트 요청 (HLS.js)   │                        │                   │
      ├─── seg_00000.ts ───────────────────────────────────▶│                   │
      │◀───────────────────────────────────────────────────│                   │
      ├─── seg_00001.ts ───────────────────────────────────▶│                   │
      │◀───────────────────────────────────────────────────│                   │
      │   ... (6초마다 새 세그먼트 요청)                     │                   │
```

### 핵심 포인트

| 구분 | uploadFile | downloadToFile |
|------|------------|----------------|
| **실행 위치** | Spring Boot 서버 | Spring Boot 서버 |
| **실행 시점** | 인코딩 완료 후 | 인코딩 시작 시 |
| **데이터 방향** | 서버 로컬 → S3 | S3 → 서버 로컬 |
| **용도** | HLS 세그먼트 업로드 | 원본 영상 다운로드 (인코딩용) |

**시청자는 서버를 거치지 않고 CloudFront에서 직접 스트리밍합니다!**

---

## Q1: downloadToFile은 유저 로컬에서 실행되나?

### ❌ 아닙니다!

`downloadToFile`은 **시청자와 무관**하게 **서버에서만** 실행됩니다.

### 실행 흐름

```java
// HlsTranscodeService.java - 서버에서 실행되는 코드

public int transcodeAndUpload(long contentId, String sourceKey, ...) {
    
    // 1. 서버 로컬 작업 디렉토리 생성
    Path jobDir = Paths.get(workDir).resolve("content-" + contentId);
    // 결과: /tmp/cinema-work/content-123/
    
    Path input = jobDir.resolve("source.mp4");
    // 결과: /tmp/cinema-work/content-123/source.mp4
    
    // 2. S3에서 서버 로컬로 다운로드 ← downloadToFile 사용!
    s3ObjectService.downloadToFile(sourceKey, input);
    
    // 3. FFmpeg로 HLS 인코딩 (서버에서 실행)
    ffmpeg.transcodeToHls(jobDir, input, outDir);
    
    // 4. 인코딩된 파일들을 S3로 업로드 ← uploadFile 사용!
    for (Path ts : tsFiles) {
        s3ObjectService.uploadFile(key, ts, "video/mp2t", ...);
    }
    
    // 5. 서버 로컬 파일 삭제 (정리)
    safeDelete(jobDir);
}
```

### 시청자의 영상 수신 방식

```
시청자 브라우저 ──────────────────────▶ CloudFront ──────────▶ S3
           직접 HTTP 요청                    CDN 캐싱
           (서버 경유 X)
```

시청자는:
1. **서버에서** CloudFront URL만 받음
2. **CloudFront에서** 직접 영상 스트리밍
3. **서버를 거치지 않음** (서버 부하 감소)

### 결론

| 구분 | 서버 | 시청자 |
|------|------|--------|
| `downloadToFile` | ⭕ 사용 (인코딩용) | ❌ 사용 안 함 |
| `uploadFile` | ⭕ 사용 (인코딩 결과 저장) | ❌ 사용 안 함 |
| 영상 수신 | ❌ | ⭕ CloudFront에서 직접 |

---

## Q2: S3 세그먼트 6초 관련

### ✅ 맞습니다!

HLS 인코딩 시 **6초 단위로 세그먼트**를 생성합니다.

### 세그먼트 생성 설정

```java
// FfmpegDockerRunner.java

cmd.add("-hls_time");
cmd.add("6");  // ← 6초 단위 세그먼트
cmd.add("-hls_playlist_type");
cmd.add("vod");
cmd.add("-hls_segment_filename");
cmd.add(containerOutDir + "/seg_%05d.ts");  // seg_00000.ts, seg_00001.ts, ...
```

### 결과물 예시

```
hls/123/
├── index.m3u8          # 플레이리스트 (세그먼트 목록)
├── seg_00000.ts        # 0~6초
├── seg_00001.ts        # 6~12초
├── seg_00002.ts        # 12~18초
└── ...
```

### index.m3u8 내용 예시

```
#EXTM3U
#EXT-X-VERSION:3
#EXT-X-TARGETDURATION:6        ← 각 세그먼트 최대 6초
#EXT-X-MEDIA-SEQUENCE:0
#EXT-X-PLAYLIST-TYPE:VOD
#EXTINF:6.000,                  ← 이 세그먼트 길이: 6초
seg_00000.ts
#EXTINF:6.000,
seg_00001.ts
#EXTINF:6.000,
seg_00002.ts
#EXTINF:4.500,                  ← 마지막은 6초보다 짧을 수 있음
seg_00003.ts
#EXT-X-ENDLIST
```

### HLS 스트리밍 동작 원리

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         HLS.js (클라이언트) 동작                            │
└─────────────────────────────────────────────────────────────────────────────┘

시간 →  0초      6초      12초     18초     24초
        │        │        │        │        │
        ▼        ▼        ▼        ▼        ▼
     ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐
     │seg_0 │ │seg_1 │ │seg_2 │ │seg_3 │ │seg_4 │
     └──────┘ └──────┘ └──────┘ └──────┘ └──────┘
        │        │        │
        │        │        │
        ▼        ▼        ▼
   [현재 재생] [버퍼링] [프리페치]

1. HLS.js가 index.m3u8 파싱
2. 현재 재생 위치 기준 2~3개 세그먼트 미리 다운로드
3. 재생하면서 다음 세그먼트 계속 요청
4. 버퍼가 유지되면 끊김 없이 재생
```

### 중요한 점

- **S3는 "보내주는 방식"이 아님** - 그냥 파일 저장소
- **클라이언트(HLS.js)가 능동적으로 요청**
- 세그먼트 분할은 **인코딩 시점에 서버에서** 결정됨
- CloudFront는 **캐싱만** 담당

---

## Q3: Range 설정은 무엇인가?

### 현재 코드에는 HTTP Range 설정이 없습니다!

### HTTP Range Request란?

```
일반적인 파일 요청:
GET /movie.mp4
→ 전체 2GB 파일 다운로드

Range 요청:
GET /movie.mp4
Range: bytes=1000000-2000000
→ 1MB~2MB 구간만 다운로드
```

### HLS vs Progressive Download 비교

| 방식 | HLS (현재 사용) | Progressive Download |
|------|----------------|---------------------|
| **파일 구조** | 여러 개의 작은 세그먼트 (.ts) | 하나의 큰 파일 (.mp4) |
| **요청 방식** | 세그먼트 단위 HTTP GET | Range 헤더로 부분 요청 |
| **서버 부담** | 낮음 (CDN 캐싱 효율적) | 높음 (Range 처리 필요) |
| **적응형 비트레이트** | ⭕ 지원 | ❌ 미지원 |
| **Seek (건너뛰기)** | 해당 세그먼트만 요청 | Range로 해당 위치 요청 |

### HLS에서 Range가 필요 없는 이유

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            HLS 방식 (현재)                                   │
└─────────────────────────────────────────────────────────────────────────────┘

사용자가 1분 30초로 Seek 시:
1. HLS.js가 1:30에 해당하는 세그먼트 계산
   → 1:30 = 90초 → 90/6 = 15번째 세그먼트
2. seg_00015.ts 전체를 요청 (Range 불필요)
3. 해당 세그먼트부터 재생 시작

┌─────────────────────────────────────────────────────────────────────────────┐
│                       Progressive Download 방식                              │
└─────────────────────────────────────────────────────────────────────────────┘

사용자가 1분 30초로 Seek 시:
1. 브라우저가 1:30에 해당하는 바이트 위치 계산
   → moov atom에서 위치 정보 참조
2. Range: bytes=15000000-16000000 요청
3. 서버가 해당 범위만 응답
```

### 결론

현재 시스템에서는:
- **HLS 방식을 사용**하므로 Range 요청 불필요
- 세그먼트 단위로 이미 분할되어 있음
- 클라이언트가 필요한 세그먼트만 요청
- **S3/CloudFront가 "보내는 방식"을 제어하는 게 아님**
  - S3는 저장소
  - CloudFront는 캐시
  - **클라이언트(HLS.js)가 주도적으로 요청**

---

## Q4: 브로드캐스트와 서버/스레드 확장

### 5명 시청 시 서버 5개가 필요한가?

### ❌ 아닙니다! 단일 서버로 충분합니다.

### 현재 아키텍처 이해

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          영상 데이터 흐름                                    │
└─────────────────────────────────────────────────────────────────────────────┘

                    ┌──────────────────────────────────────────┐
                    │            CloudFront (CDN)               │
                    │                                          │
   시청자 A ───────▶│    hls/123/seg_00000.ts (캐시됨)        │◀─── S3
   시청자 B ───────▶│    hls/123/seg_00001.ts (캐시됨)        │
   시청자 C ───────▶│    ...                                  │
   시청자 D ───────▶│                                          │
   시청자 E ───────▶│                                          │
                    └──────────────────────────────────────────┘

  ※ 영상 데이터는 서버를 거치지 않음!
  ※ CloudFront가 5명에게 동일 캐시 데이터 제공

┌─────────────────────────────────────────────────────────────────────────────┐
│                          동기화 신호 흐름                                    │
└─────────────────────────────────────────────────────────────────────────────┘

                    ┌──────────────────────────────────────────┐
                    │         Spring Boot 서버                  │
                    │                                          │
                    │  ┌────────────────────────────────────┐  │
   시청자 A ◀──────▶│  │    SimpleBroker (인메모리)         │  │
   시청자 B ◀──────▶│  │                                    │  │
   시청자 C ◀──────▶│  │  /topic/theaters/123/state         │  │
   시청자 D ◀──────▶│  │  └─ 구독자: [A, B, C, D, E]        │  │
   시청자 E ◀──────▶│  │                                    │  │
                    │  │  PlaybackState (수십 바이트)        │  │
   Host ───────────▶│  │  { playing, positionMs, ... }      │  │
                    │  └────────────────────────────────────┘  │
                    └──────────────────────────────────────────┘

  ※ 동기화 메시지는 매우 작음 (수십 바이트)
  ※ 영상 데이터가 아닌 "현재 재생 위치" 정보만 전송
```

### 서버가 처리하는 것 vs 처리하지 않는 것

| 구분 | 서버 처리 | 트래픽 |
|------|----------|--------|
| 영상 데이터 | ❌ CloudFront가 처리 | 수 Mbps/유저 |
| 재생 정보 (PlaybackInfo) | ⭕ 최초 1회 | 수백 바이트 |
| 동기화 메시지 (PlaybackState) | ⭕ WebSocket | 수십 바이트 (구독 시) |

### WebSocket 스레드 모델

```java
// Spring WebSocket은 NIO 기반 (비동기 I/O)
// 연결당 스레드가 아님!

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");  // 인메모리 브로커
    }
}
```

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     Spring WebSocket 스레드 모델                             │
└─────────────────────────────────────────────────────────────────────────────┘

                        ┌─────────────────────────────────┐
                        │     Netty/Tomcat NIO 스레드     │
                        │     (소수의 이벤트 루프)         │
                        │                                 │
  Connection A ────────▶│  ┌─────────────────────────┐    │
  Connection B ────────▶│  │   Event Queue           │    │
  Connection C ────────▶│  │   [msg1, msg2, msg3...] │    │
  Connection D ────────▶│  └─────────────────────────┘    │
  Connection E ────────▶│              │                  │
       ...              │              ▼                  │
  Connection N ────────▶│     Worker Thread Pool          │
                        │     (8~16개 정도)               │
                        └─────────────────────────────────┘

※ 1만 연결 = 1만 스레드 ❌
※ 1만 연결 = 소수 스레드 + 이벤트 큐 ⭕
```

### 상태 조회 동작 (재생 제어 없음)

```java
// TheaterWsController.java - 재생 제어 없음, 스케줄 기반 자동 재생

@SubscribeMapping("/theaters/{scheduleId}/state")
public PlaybackState subscribeState(@DestinationVariable long scheduleId) {
    // 클라이언트가 구독 시 현재 재생 상태 반환
    // 스케줄 시작 시간 기준으로 자동 계산된 상태
    return syncService.getState(scheduleId);
}
```

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     스케줄 기반 자동 재생 동작                               │
└─────────────────────────────────────────────────────────────────────────────┘

시청자 → SUBSCRIBE /topic/theaters/123/state
          │
          ▼
   ┌──────────────────┐
   │ TheaterWsController │
   │ syncService.getState() │
   │ → PlaybackState        │
   │   (스케줄 시작 시간 기준) │
   └──────────────────┘
          │
          ▼ 구독 응답으로 PlaybackState 반환
   ┌──────────────────────────────────────┐
   │        현재 재생 상태 계산            │
   │                                      │
   │  스케줄 시작: 14:00:00               │
   │  현재 시간:   14:30:00               │
   │  재생 위치:   30분 = 1,800,000ms     │
   │                                      │
   │  PlaybackState {                     │
   │    playing: true,                    │
   │    positionMs: 1800000,              │
   │    playbackRate: 1.0,                │
   │    serverTimeMs: ...                 │
   │  }                                   │
   └──────────────────────────────────────┘
          │
          └──▶ 시청자에게 응답
```

### 확장 전략

#### 1단계: 단일 서버 최적화 (현재)

```
동시 접속 ~1,000명 수준
- SimpleBroker (인메모리)
- 단일 Spring Boot 인스턴스
- 영상은 CloudFront가 처리
```

#### 2단계: 수평 확장 (Redis Pub/Sub)

```yaml
# application.yaml 수정
spring:
  redis:
    host: redis-cluster.example.com
    port: 6379
```

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // SimpleBroker 대신 Redis 브로커 사용
        registry.enableStompBrokerRelay("/topic")
                .setRelayHost("redis-host")
                .setRelayPort(6379);
    }
}
```

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    Redis Pub/Sub 기반 확장                                   │
└─────────────────────────────────────────────────────────────────────────────┘

                           ┌─────────────────┐
                           │   Redis Cluster  │
                           │   (Pub/Sub)      │
                           └─────────────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
              ▼                    ▼                    ▼
     ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
     │   Server #1     │  │   Server #2     │  │   Server #3     │
     │   (~1000 users) │  │   (~1000 users) │  │   (~1000 users) │
     └─────────────────┘  └─────────────────┘  └─────────────────┘
              │                    │                    │
        ┌─────┴─────┐        ┌─────┴─────┐        ┌─────┴─────┐
        │  Users    │        │  Users    │        │  Users    │
        │  A~J      │        │  K~T      │        │  U~Z      │
        └───────────┘        └───────────┘        └───────────┘

※ Host가 Server#1에 PLAY 명령 전송
※ Server#1이 Redis에 발행
※ Server#2, #3도 Redis에서 수신하여 자신의 구독자에게 전달
```

#### 3단계: 대규모 확장 (Kafka + 전용 브로커)

```
동시 접속 10만+ 명
- Apache Kafka / AWS Kinesis
- 전용 메시지 브로커 클러스터
- Auto-scaling 구성
```

### 현재 코드의 동시성 설정

```java
// AsyncConfig.java
@Bean
public Executor encodingExecutor() {
    return Executors.newFixedThreadPool(2);  // 인코딩용 (2개 스레드)
}

// WebSocket은 별도 설정 없이 Tomcat/Netty 기본값 사용
// 일반적으로 200~500 동시 연결 처리 가능
```

### 성능 예측

| 동시 접속자 | 서버 대수 | 브로커 | 비고 |
|------------|----------|--------|------|
| ~500명 | 1대 | SimpleBroker | 현재 설정으로 가능 |
| ~5,000명 | 2~3대 | Redis Pub/Sub | Load Balancer 추가 |
| ~50,000명 | 10대+ | Kafka | 전문 인프라 필요 |

### 핵심 포인트

1. **영상 데이터는 서버를 거치지 않음** → CloudFront가 처리
2. **서버는 동기화 신호만 전송** → 매우 가벼움
3. **WebSocket은 연결당 스레드가 아님** → NIO/이벤트 기반
4. **SimpleBroker는 인메모리** → 빠르지만 단일 서버 한정
5. **확장 시 Redis/Kafka로 브로커 교체** → 코드 변경 최소화

---

## 요약 테이블

| 질문 | 답변 |
|------|------|
| downloadToFile 실행 위치 | **서버** (인코딩용), 시청자 ❌ |
| 시청자 영상 수신 방식 | **CloudFront에서 직접** 스트리밍 |
| 6초 세그먼트 | **인코딩 시점에 서버가 분할**, S3는 저장만 |
| Range 설정 | **HLS는 Range 불필요** (세그먼트 단위 요청) |
| 5명 시청 시 서버 | **1대로 충분** (영상은 CDN, 동기화만 서버) |
| 스레드 모델 | **NIO/이벤트 기반** (연결당 스레드 ❌) |
| 확장 방법 | SimpleBroker → **Redis Pub/Sub** → Kafka |


┌─────────────────────────────────────────────────────────────────────────────┐
│                    [1단계] 콘텐츠 업로드 (Seller)                            │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
    POST /api/assets/presign ───────┼───────▶ S3 Presigned URL
    PUT (직접 업로드) ──────────────┼───────▶ S3에 source.mp4 저장
    POST /api/assets/complete ──────┼───────▶ EncodingJobService.start()
                                    │              │
                                    │              ▼
                                    │         HlsTranscodeService
                                    │              │
                                    │              ▼
                                    │         S3에 HLS 세그먼트 업로드
                                    │         (hls/{contentId}/index.m3u8)
                                    │              │
                                    │              ▼
                                    │         Content.videoHlsMaster 연결
                                    │
    POST /schedules ────────────────┼───────▶ ScheduleItem 생성
                                    │         (Content와 연결됨)

┌─────────────────────────────────────────────────────────────────────────────┐
│                    [2단계] 상영관 입장 (Viewer)                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
    POST /theaters/{id}/enter ──────┼───────▶ WatchHistory 생성
                                    │         (구독/스케줄 상태 검증)
                                    │
    GET /theaters/{id}/playback ────┼───────▶ ScheduleItem → Content → MediaAsset
                                    │         → CloudFront URL 반환
                                    │
    WebSocket SUBSCRIBE ────────────┼───────▶ PlaybackStateResponse 수신
                                    │         (스케줄 시작 시간 기준 positionMs)
                                    │
                              [영상 재생 + 동기화]

                              1. Seller가 Content 생성 (제목, 설명 등)
2. Seller가 영상 업로드 → complete() 호출
3. ★ 자동으로 HLS 인코딩 시작 (비동기)
4. 인코딩 완료 → S3에 HLS 파일들 저장됨
   └── hls/123/index.m3u8
   └── hls/123/segment_0.ts
   └── hls/123/segment_1.ts
   └── ...

[Phase 2: 스케줄 등록]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
5. Seller가 ScheduleDay 생성 (날짜 지정)
6. Seller가 ScheduleItem 생성 (startAt, endAt 지정)
   └── status = CLOSED (초기값)

[Phase 3: 상영 시간 도래] (스케줄러가 자동 처리)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
7. startAt - 10분 → 스케줄러가 CLOSED → WAITING 전환
8. startAt 도달 → 스케줄러가 WAITING → PLAYING 전환
9. User가 /theaters/{scheduleId}/enter 호출
10. User가 /theaters/{scheduleId}/playback 호출
    → videoHlsMaster의 CloudFront URL 반환
11. 프론트에서 HLS.js로 재생
12. endAt 도달 → PLAYING → ENDING → CLOSED

상태전이 타임라인
시간축 ─────────────────────────────────────────────────────────────►

     CLOSED        WAITING         PLAYING           ENDING      CLOSED
        │             │               │                 │           │
        │   -10분     │   startAt     │     endAt       │   +10분   │
        ├─────────────┼───────────────┼─────────────────┼───────────┤
        │             │               │                 │           │
입장가능              입장가능         입장가능           입장불가    입장불가
영상없음              카운트다운       영상재생          유예화면    Kick


✅ 흐름 검증
🎬 상영관 입장 + 영상 싱크 흐름
단계	설명	상태
1. 상영관 입장	POST /theaters/{id}/enter → TheaterEnterResponse 반환	✅ 맞음
2. STOMP 연결	/ws 엔드포인트로 CONNECT (JWT 포함)	✅ 맞음
3. 상태 구독	/topic/theaters/{id}/state 구독	✅ 맞음
4. 초기 싱크	@SubscribeMapping으로 즉시 PlaybackStateResponse 수신	✅ 맞음
5. 영상 싱크	positionMs로 비디오 플레이어 seek	✅ 맞음
6. 상태 변경	스케줄러가 DB 업데이트 → 서비스 호출 → 브로드캐스트	✅ 맞음
7. 클라 수신	상태 변경 시 모달/kick 처리 가능	✅ 맞음
💬 채팅 흐름
단계	설명	상태
1. 메시지 전송	/app/chat/{id}로 SEND	✅ 맞음
2. prefix 제거	/app 제거 → /chat/{id}로 라우팅	✅ 맞음
3. 컨트롤러	@MessageMapping("/chat/{scheduleId}") 호출	✅ 맞음
4. 브로드캐스트	@SendTo("/topic/theaters/{id}/chat")로 전체 전송	✅ 맞음

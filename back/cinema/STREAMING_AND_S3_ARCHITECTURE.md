# 영화 동시 송출 및 S3 아키텍처 문서

## 목차
1. [개요](#개요)
2. [전체 아키텍처](#전체-아키텍처)
3. [S3 및 CloudFront 구성](#s3-및-cloudfront-구성)
4. [동시 송출(Theater) 시스템](#동시-송출theater-시스템)
5. [현재 코드 분석 결과](#현재-코드-분석-결과)
6. [개선이 필요한 사항](#개선이-필요한-사항)
7. [권장 수정 사항](#권장-수정-사항)

---

## 개요

이 프로젝트는 **구독 기반 영화 동시 송출 서비스**입니다. 
- 구독자(Subscriber)들이 동일한 영상을 **동시에 시청**합니다.
- **스케줄 시작 시간** 기준으로 모든 시청자가 동일한 재생 위치에서 시청합니다.
- **재생 제어 없음** - 스케줄에 따라 자동 재생됩니다.

### 핵심 기술 스택
| 구분 | 기술 |
|------|------|
| 백엔드 | Spring Boot, Spring WebSocket (STOMP) |
| 저장소 | AWS S3 (영상/이미지 저장) |
| CDN | AWS CloudFront (영상 배포) |
| 인코딩 | FFmpeg (Docker) → HLS 변환 |
| 인증 | JWT |

---

## 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              클라이언트 (Next.js)                            │
└─────────────────────────────────────────────────────────────────────────────┘
                    │                                    │
                    ▼                                    ▼
        ┌─────────────────────┐              ┌─────────────────────┐
        │   REST API 요청      │              │   WebSocket 연결     │
        │   (재생 정보 조회)   │              │   (상태 동기화)      │
        └─────────────────────┘              └─────────────────────┘
                    │                                    │
                    ▼                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Spring Boot 백엔드                                 │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐  │
│  │ TheaterPlayback     │  │ TheaterWsController │  │ TheaterSyncService  │  │
│  │ Controller          │  │ (STOMP)             │  │ (스케줄 기반 동기화)│  │
│  └─────────────────────┘  └─────────────────────┘  └─────────────────────┘  │
│             │                                                │               │
│             ▼                                                ▼               │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                    CloudFrontUrlService                              │    │
│  │                    (S3 객체 → CDN URL 변환)                          │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                    │
                    ▼
        ┌─────────────────────┐
        │   AWS CloudFront    │
        │   (CDN 배포)        │
        └─────────────────────┘
                    │
                    ▼
        ┌─────────────────────┐
        │      AWS S3         │
        │   (영상/이미지)     │
        └─────────────────────┘
```

---

## S3 및 CloudFront 구성

### S3 버킷 구조

```
lion-cinema-content-team4/
├── profiles/                    # 프로필 이미지
│   └── {userId}/
│       └── profile.jpg
├── posters/                     # 영화 포스터
│   └── {contentId}/
│       └── poster.jpg
├── uploads/                     # 원본 영상 (업로드용)
│   └── {contentId}/
│       └── source.mp4
└── hls/                         # HLS 변환 결과
    └── {contentId}/
        ├── index.m3u8           # HLS 마스터 플레이리스트
        ├── seg_00000.ts         # 세그먼트 파일들
        ├── seg_00001.ts
        └── ...
```

### S3 설정 (application.yaml)

```yaml
aws:
  region: ap-northeast-2
  s3:
    bucket: lion-cinema-content-team4
  cloudfront:
    domain: d3q8b3ts21eo3m.cloudfront.net
```

### 주요 서비스 클래스

#### 1. PresignService - Presigned URL 생성
```java
// 위치: com.example.cinema.infra.s3.PresignService

@Service
public class PresignService {
    public URL presignPut(String objectKey, String contentType) {
        // 15분간 유효한 업로드 URL 생성
        // 클라이언트가 직접 S3에 업로드 가능
    }
}
```

**사용 흐름:**
1. 클라이언트가 `/api/assets/presign` 호출
2. 서버가 Presigned URL 반환
3. 클라이언트가 해당 URL로 직접 S3에 PUT 요청
4. 업로드 완료 후 `/api/assets/complete` 호출

#### 2. S3ObjectService - S3 객체 관리
```java
// 위치: com.example.cinema.infra.s3.S3ObjectService

@Service
public class S3ObjectService {
    // 파일 존재 및 유효성 검증 (재시도 포함)
    public HeadObjectResponse assertReady(String key, long minBytes, 
                                          String expectedContentTypePrefix,
                                          int maxAttempts, long[] backoffMillis);
    
    // S3 → 로컬 다운로드 (인코딩용)
    public void downloadToFile(String key, Path dest);
    
    // 로컬 → S3 업로드 (인코딩 결과물)
    public void uploadFile(String key, Path file, String contentType, String cacheControl);
}
```

#### 3. CloudFrontUrlService - CDN URL 생성
```java
// 위치: com.example.cinema.service.media.CloudFrontUrlService

@Service
public class CloudFrontUrlService {
    public String toPublicUrl(String objectKey) {
        // S3 object key → CloudFront 공개 URL 변환
        // 예: "hls/123/index.m3u8" 
        //    → "https://d3q8b3ts21eo3m.cloudfront.net/hls/123/index.m3u8"
    }
}
```

#### 4. S3KeyFactory - S3 키 생성 규칙
```java
// 위치: com.example.cinema.service.asset.S3KeyFactory

@Component
public class S3KeyFactory {
    public String posterKey(long contentId, String contentType, String fileName);
    // → "posters/{contentId}/poster.{ext}"
    
    public String videoSourceKey(long contentId);
    // → "uploads/{contentId}/source.mp4"
    
    public String hlsMasterKey(long contentId);
    // → "hls/{contentId}/index.m3u8"
}
```

---

## 동시 송출(Theater) 시스템

### 송출 흐름

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                              동시 송출 시작 흐름                              │
└──────────────────────────────────────────────────────────────────────────────┘

1. [사전 준비] 콘텐츠 업로드 및 스케줄 생성
   
   Creator → POST /contents                    # 콘텐츠 메타 생성
          → POST /api/assets/presign           # 업로드 URL 획득
          → PUT (S3 직접 업로드)               # 영상 업로드
          → POST /api/assets/complete          # 업로드 완료 알림
                                               # → 자동 HLS 인코딩 시작
          → POST /schedules                    # 상영 스케줄 등록

2. [상영 시작] 시청자가 상영관 입장

   Viewer  → GET /theaters/{scheduleId}/playback   # 재생 정보 요청
          ← { videoUrl, durationMs, ... }          # CloudFront URL 반환
          
          → STOMP CONNECT /ws                      # WebSocket 연결
          → SUBSCRIBE /topic/theaters/{id}/state   # 상태 구독
          ← PlaybackState 자동 수신                # 스케줄 기반 재생 위치

3. [상영 중] 자동 재생 (재생 제어 없음)

   - 스케줄 시작 시간(startAt) 기준으로 현재 재생 위치 자동 계산
   - 모든 시청자가 동일한 재생 위치에서 시청
   - PLAYING 상태일 때만 재생 (스케줄 상태 기반)
```

### 핵심 컴포넌트

#### 1. TheaterPlaybackController - 재생 정보 조회
```java
// 위치: com.example.cinema.controller.theater.TheaterPlaybackController

@RestController
@RequestMapping("/theaters")
public class TheaterPlaybackController {
    
    @GetMapping("/{scheduleId}/playback")
    public ResponseEntity<ApiResponse<PlaybackInfo>> playback(@PathVariable long scheduleId) {
        // 스케줄 ID → 비디오 URL 반환
        // PlaybackInfo: { assetId, videoUrl, contentType, durationMs }
    }
}
```

#### 2. TheaterWsController - WebSocket 핸들러
```java
// 위치: com.example.cinema.controller.theater.TheaterWsController

@Controller
public class TheaterWsController {
    
    /**
     * 클라이언트가 구독 시 현재 재생 상태 반환
     * - 재생 제어 없음
     * - 스케줄 시작 시간 기준 자동 계산된 상태 반환
     */
    @SubscribeMapping("/theaters/{scheduleId}/state")
    public PlaybackState subscribeState(@DestinationVariable long scheduleId) {
        return syncService.getState(scheduleId);
    }
}
```

#### 3. TheaterSyncService - 재생 상태 관리 (스케줄 기반)
```java
// 위치: com.example.cinema.service.theater.TheaterSyncService

@Service
public class TheaterSyncService {
    
    /**
     * 스케줄 시작 시간 기준으로 현재 재생 위치 계산
     * - 재생 제어 없음 (자동 재생)
     * - PLAYING 상태일 때만 재생
     */
    public PlaybackState getState(long scheduleId) {
        ScheduleItem item = scheduleItemRepository.findById(scheduleId);
        
        // PLAYING 상태일 때만 재생
        boolean isPlaying = item.getStatus() == ScheduleStatus.PLAYING;
        
        // 스케줄 시작 시간부터 경과된 시간 = 재생 위치
        long positionMs = 0L;
        if (isPlaying) {
            long startAtMs = item.getStartAt().toInstant(ZoneOffset.UTC).toEpochMilli();
            positionMs = Math.max(0L, serverTimeMs - startAtMs);
        }
        
        return new PlaybackState(isPlaying, positionMs, 1.0, serverTimeMs);
    }
}
```

**재생 위치 계산 원리:**
```
스케줄 시작 시간: 2024-01-15 14:00:00 (UTC)
현재 서버 시간:  2024-01-15 14:30:00 (UTC)

재생 위치 = 현재 시간 - 시작 시간
         = 30분 = 1,800,000 ms
```

#### 4. ScheduleAssetResolver - 스케줄 ↔ 에셋 매핑
```java
// 위치: com.example.cinema.service.theater.ScheduleAssetResolver

public interface ScheduleAssetResolver {
    Long resolveVideoAssetId(long scheduleId);
}

// 현재 구현: InMemoryScheduleAssetResolver (테스트용)
@Component
public class InMemoryScheduleAssetResolver implements ScheduleAssetResolver {
    private final ConcurrentHashMap<Long, Long> scheduleToAsset = new ConcurrentHashMap<>();
    // 수동으로 매핑해야 함 → 실제 DB 연동 필요
}
```

### DTO 정의

```java
// PlaybackInfo - 재생 정보 (REST 응답)
public class PlaybackInfo {
    private Long assetId;
    private String videoUrl;     // CloudFront URL
    private String contentType;  // "video/mp4" 또는 "application/vnd.apple.mpegurl"
    private Long durationMs;
}

// PlaybackState - 재생 상태 (WebSocket 응답)
public class PlaybackState {
    private boolean playing;      // 재생 중 여부 (PLAYING 상태일 때 true)
    private long positionMs;      // 현재 재생 위치 (스케줄 시작부터 경과 시간)
    private double playbackRate;  // 재생 속도 (항상 1.0)
    private long serverTimeMs;    // 서버 현재 시간
}
```

---

## 현재 코드 분석 결과

### ✅ 잘 구현된 부분

| 영역 | 내용 |
|------|------|
| **S3 업로드** | Presigned URL 기반 클라이언트 직접 업로드 지원 |
| **HLS 인코딩** | FFmpeg Docker로 비동기 HLS 변환 파이프라인 |
| **WebSocket** | STOMP 프로토콜 기반 실시간 통신 |
| **상태 동기화** | 스케줄 시작 시간 기준 재생 위치 자동 계산 |
| **스케줄 관리** | 상영 시간 겹침 검증, 상태 전이 스케줄러 |

### ⚠️ 문제점 및 개선 필요 사항

#### 1. InMemoryScheduleAssetResolver - DB 연동 미구현
**현재 상태:**
```java
@Component
public class InMemoryScheduleAssetResolver implements ScheduleAssetResolver {
    private final ConcurrentHashMap<Long, Long> scheduleToAsset = new ConcurrentHashMap<>();
    // 비어있음! 수동으로 put() 해야 함
}
```

**문제:** 
- scheduleId로 재생 정보 조회 시 항상 null 반환
- 실제 서비스에서 영상 재생 불가

#### 2. TheaterPlaybackService - VIDEO_SOURCE만 검사
**현재 상태:**
```java
public PlaybackInfo getPlaybackInfo(long scheduleId) {
    // ...
    if (asset.getAssetType() != AssetType.VIDEO_SOURCE) {
        return null;  // VIDEO_HLS_MASTER는 반환 안 됨!
    }
    // ...
}
```

**문제:**
- HLS 스트리밍을 위한 VIDEO_HLS_MASTER 타입을 지원하지 않음
- 동시 송출에서 HLS(적응형 비트레이트) 사용 시 문제

#### 3. 구독 검증 없음
**현재 상태:**
- 재생 정보 조회 시 사용자의 구독 상태 확인 없음
- 누구나 CloudFront URL 획득 가능

#### 4. CloudFront Signed URL 미사용
**현재 상태:**
```java
public String toPublicUrl(String objectKey) {
    return domain + "/" + key;  // 공개 URL
}
```

**문제:**
- 유료 콘텐츠를 누구나 접근 가능
- URL이 노출되면 무제한 재생 가능

#### 5. 설정 경로 불일치
**application.yaml:**
```yaml
aws:
  cloudfront:
    domain: d3q8b3ts21eo3m.cloudfront.net
```

**CloudFrontProperties:**
```java
@ConfigurationProperties(prefix = "cloudfront")  # "aws.cloudfront"가 아님!
```

---

## 권장 수정 사항

### 1. ScheduleAssetResolver DB 연동 구현

```java
@Component
@RequiredArgsConstructor
public class DbScheduleAssetResolver implements ScheduleAssetResolver {
    
    private final ScheduleItemRepository scheduleItemRepository;
    
    @Override
    public Long resolveVideoAssetId(long scheduleId) {
        return scheduleItemRepository.findById(scheduleId)
            .map(item -> {
                Content content = item.getContent();
                // HLS 우선, 없으면 원본 영상
                if (content.getVideoHlsMaster() != null) {
                    return content.getVideoHlsMaster().getAssetId();
                }
                if (content.getVideoSource() != null) {
                    return content.getVideoSource().getAssetId();
                }
                return null;
            })
            .orElse(null);
    }
}
```

### 2. TheaterPlaybackService 수정

```java
public PlaybackInfo getPlaybackInfo(long scheduleId) {
    Long assetId = scheduleAssetResolver.resolveVideoAssetId(scheduleId);
    if (assetId == null) return null;

    MediaAsset asset = mediaAssetRepository.findById(assetId).orElse(null);
    if (asset == null) return null;

    // HLS와 VIDEO_SOURCE 모두 지원
    if (asset.getAssetType() != AssetType.VIDEO_SOURCE 
        && asset.getAssetType() != AssetType.VIDEO_HLS_MASTER) {
        return null;
    }

    String url = cloudFrontUrlService.toPublicUrl(asset.getObjectKey());
    
    // HLS인 경우 content-type 조정
    String contentType = asset.getAssetType() == AssetType.VIDEO_HLS_MASTER 
        ? "application/vnd.apple.mpegurl" 
        : (asset.getContentType() != null ? asset.getContentType() : "video/mp4");

    PlaybackInfo dto = new PlaybackInfo();
    dto.setAssetId(asset.getAssetId());
    dto.setVideoUrl(url);
    dto.setContentType(contentType);
    dto.setDurationMs(asset.getDurationMs());
    return dto;
}
```

### 3. 구독 검증 추가

```java
// TheaterPlaybackController
@GetMapping("/{scheduleId}/playback")
public ResponseEntity<ApiResponse<PlaybackInfo>> playback(
        @PathVariable long scheduleId,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    // 구독 상태 확인
    User user = userDetails.getUser();
    if (user.getSubscription() == null 
        || !user.getSubscription().getIsActive()) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("구독이 필요한 서비스입니다."));
    }
    
    PlaybackInfo info = playbackService.getPlaybackInfo(scheduleId);
    if (info == null) {
        return ResponseEntity.ok(ApiResponse.error("재생 정보를 찾을 수 없습니다."));
    }
    return ResponseEntity.ok(ApiResponse.success("재생 정보 조회 성공", info));
}
```

### 4. CloudFront Signed URL 구현 (선택)

```java
@Service
public class CloudFrontSignedUrlService {
    
    @Value("${cloudfront.key-pair-id}")
    private String keyPairId;
    
    @Value("${cloudfront.private-key-path}")
    private String privateKeyPath;
    
    public String generateSignedUrl(String objectKey, Duration validity) {
        // AWS SDK CloudFront Signer 사용
        // 만료 시간이 있는 서명된 URL 생성
    }
}
```

### 5. 설정 경로 수정

**CloudFrontProperties.java:**
```java
@ConfigurationProperties(prefix = "aws.cloudfront")  // 수정
public class CloudFrontProperties {
    // ...
}
```

---

## 클라이언트 구현 가이드

### 1. 상영관 입장 흐름

```typescript
// 1. 재생 정보 조회
const playbackInfo = await fetch(`/theaters/${scheduleId}/playback`, {
  headers: { Authorization: `Bearer ${token}` }
}).then(res => res.json());

// 2. 비디오 플레이어 초기화
const player = new Hls();
player.loadSource(playbackInfo.data.videoUrl);
player.attachMedia(videoElement);

// 3. WebSocket 연결
const stompClient = new Client({
  brokerURL: 'ws://server/ws',
  connectHeaders: { Authorization: `Bearer ${token}` }
});

// 4. 상태 구독 - 구독 시 현재 상태 자동 수신
stompClient.subscribe(`/topic/theaters/${scheduleId}/state`, (message) => {
  const state = JSON.parse(message.body);
  syncPlayer(state);
});
```

### 2. 재생 동기화 로직

```typescript
function syncPlayer(state: PlaybackState) {
  const serverNow = state.serverTimeMs;
  const clientNow = Date.now();
  const drift = clientNow - serverNow; // 시간 보정값
  
  // 서버 시간과 클라이언트 시간 차이 보정
  const currentPosition = state.positionMs + drift;
  
  // 오차가 500ms 이상이면 seek
  const playerPosition = player.currentTime * 1000;
  if (Math.abs(currentPosition - playerPosition) > 500) {
    player.currentTime = currentPosition / 1000;
  }
  
  // 재생/일시정지 동기화
  if (state.playing && player.paused) {
    player.play();
  } else if (!state.playing && !player.paused) {
    player.pause();
  }
}
```

---

## 참고: HLS 인코딩 파이프라인

```
업로드 완료 알림
      ↓
┌─────────────────────────────────────────────────┐
│ EncodingJobService.start(contentId)             │
│   └─> 비동기 실행: runEncoding()                 │
└─────────────────────────────────────────────────┘
      ↓
┌─────────────────────────────────────────────────┐
│ HlsTranscodeService.transcodeAndUpload()        │
│   1. S3에서 source.mp4 다운로드                  │
│   2. FFmpeg Docker로 HLS 변환                   │
│      - seg_00000.ts, seg_00001.ts, ...          │
│      - index.m3u8                               │
│   3. 변환된 파일들 S3 업로드                     │
│      - hls/{contentId}/seg_*.ts                 │
│      - hls/{contentId}/index.m3u8               │
└─────────────────────────────────────────────────┘
      ↓
┌─────────────────────────────────────────────────┐
│ EncodingTxService.linkHlsMaster()               │
│   - Content.videoHlsMaster에 연결               │
└─────────────────────────────────────────────────┘
```

**FFmpeg 명령어 (Docker 내부):**
```bash
ffmpeg -y -i /work/source.mp4 \
  -c:v h264 -c:a aac \
  -f hls \
  -hls_time 6 \
  -hls_playlist_type vod \
  -hls_segment_filename /work/out/seg_%05d.ts \
  /work/out/index.m3u8
```

---

## 요약

현재 구현된 동시 송출 시스템은 기본적인 아키텍처가 잘 설계되어 있으나, **실제 서비스를 위해서는 다음 수정이 필수**입니다:

1. ✅ `ScheduleAssetResolver` DB 연동 구현
2. ⚠️ 구독 상태 검증 추가
3. ⚠️ 설정 경로 불일치 수정

추가로 권장되는 보안 강화:
- CloudFront Signed URL 도입
- WebSocket 연결 시 구독 상태 재검증

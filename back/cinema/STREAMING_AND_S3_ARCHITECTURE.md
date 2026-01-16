# 영화 동시 송출 및 S3 아키텍처 문서

## 목차
1. [개요](#개요)
2. [전체 아키텍처](#전체-아키텍처)
3. [S3 및 CloudFront 구성](#s3-및-cloudfront-구성)
4. [동시 송출(Theater) 시스템](#동시-송출theater-시스템)
5. [HLS 인코딩 파이프라인](#hls-인코딩-파이프라인)
6. [향후 개선 사항](#향후-개선-사항)

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
| 인증 | JWT, Spring Security |

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
│  │ Service             │  │ (STOMP)             │  │ (스케줄 기반 동기화)│  │
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
- **위치**: `com.example.cinema.infra.s3.PresignService`
- **역할**: 클라이언트가 S3에 직접 업로드할 수 있도록 PUT용 Presigned URL을 생성합니다 (15분 유효).

#### 2. S3ObjectService - S3 객체 관리
- **위치**: `com.example.cinema.infra.s3.S3ObjectService`
- **역할**: S3 객체 존재 여부 확인(`assertReady`), 다운로드, 업로드를 수행합니다.

#### 3. CloudFrontUrlService - CDN URL 생성
- **위치**: `com.example.cinema.service.media.CloudFrontUrlService`
- **역할**: S3 Object Key를 CloudFront 도메인 기반의 Public URL로 변환합니다.

#### 4. S3KeyFactory - S3 키 생성 규칙
- **위치**: `com.example.cinema.service.asset.S3KeyFactory`
- **역할**: 각 에셋 타입(포스터, 비디오, HLS 등)에 따른 표준화된 S3 Key 경로를 생성합니다.

---

## 동시 송출(Theater) 시스템

### 송출 흐름

```
1. [사전 준비] 콘텐츠 업로드 및 스케줄 생성
   
   Creator → POST /contents                    # 콘텐츠 메타 생성
          → POST /api/assets/presign           # 업로드 URL 획득
          → PUT (S3 직접 업로드)               # 영상 업로드
          → POST /api/assets/complete          # 업로드 완료 알림 (자동 HLS 인코딩 시작)
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

### 핵심 로직 구현

#### 1. 재생 정보 조회 및 검증 (TheaterPlaybackService)
- **구독 검증**: 사용자의 구독 상태(`isActive`)를 확인하여 미구독 시 접근을 차단합니다.
- **에셋 우선순위**: HLS(`VIDEO_HLS_MASTER`) 에셋을 우선 반환하며, 없을 경우 원본(`VIDEO_SOURCE`)을 반환합니다.
- **DB 연동**: `ScheduleItem`을 통해 연결된 `Content`의 에셋 정보를 조회합니다.

#### 2. WebSocket 상태 동기화 (TheaterSyncService)
- **시간 기반 동기화**: 서버 시간과 스케줄 시작 시간을 비교하여 현재 재생 위치(`positionMs`)를 계산합니다.
- **상태 방송**: 클라이언트가 `/topic/theaters/{scheduleId}/state`를 구독하면 즉시 현재 상태를 전송합니다.

### DTO 정의

```java
// PlaybackInfoResponse (REST 응답)
public record PlaybackInfoResponse(
    Long assetId,
    String videoUrl,     // CloudFront URL
    String contentType,  // "application/vnd.apple.mpegurl" or "video/mp4"
    Long durationMs
) {}

// PlaybackStateResponse (WebSocket 응답)
public record PlaybackStateResponse(
    boolean playing,      // 재생 중 여부 (PLAYING 상태일 때 true)
    long positionMs,      // 현재 재생 위치 (스케줄 시작부터 경과 시간)
    double playbackRate,  // 재생 속도 (항상 1.0)
    long serverTimeMs     // 서버 현재 시간
) {}
```

---

## HLS 인코딩 파이프라인

```
업로드 완료 알림 (/api/assets/complete)
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

---

## 향후 개선 사항

1. **CloudFront Signed URL 도입**
   - 현재 Public URL을 사용하고 있어, URL 유출 시 비로그인 사용자도 접근 가능합니다.
   - CloudFront Signed URL/Cookie를 적용하여 보안을 강화할 필요가 있습니다.

2. **재생 상태 정밀 동기화**
   - 네트워크 지연(Latency)을 고려한 클라이언트 측 시간 보정 로직을 고도화할 수 있습니다.
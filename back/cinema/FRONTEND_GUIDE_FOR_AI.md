# Lion Cinema Streaming Service - Frontend Development Guide

This document serves as a comprehensive guide for an AI agent to build the frontend application for the **Lion Cinema Backend**.

## 1. Project Overview
- **Domain:** Cinema Streaming Platform with "Theater" (Synchronized Viewing) capability.
- **Backend Stack:** Spring Boot 3, Java 21.
- **Frontend Goals:** Modern, responsive SPA/SSR application.
- **Recommended Stack:**
    - **Framework:** Next.js (App Router) or React + Vite.
    - **Language:** TypeScript (Strict mode).
    - **Styling:** Tailwind CSS + Shadcn/UI (or Material UI).
    - **State Management:** Zustand or React Query (TanStack Query).
    - **Video:** HLS.js or Video.js.
    - **Real-time:** `@stomp/stompjs` + `sockjs-client`.

---

## 2. API Interaction Standard

### Base Configuration
- **Base URL:** `http://localhost:8080` (Default dev environment)
- **Response Wrapper:** All API responses are wrapped in `ApiResponse<T>`.

```typescript
// Common Response Interface
interface ApiResponse<T> {
  message: string;
  data: T;
}

// Page Response Interface
interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
```

### Authentication & Interceptors
- **Auth Pattern:** JWT (Access Token + Refresh Token).
- **Header:** `Authorization: Bearer <accessToken>`
- **Logic:**
  1. Login returns `accessToken` and `refreshToken`.
  2. Store tokens securely (e.g., HTTP-only cookies or local storage if simple).
  3. **Interceptor:**
     - On 401 Unauthorized, call `POST /auth/reissue` with the refresh token.
     - Retry the original request with the new access token.

---

## 3. Core Features & Implementation Details

### A. Authentication & User (`/auth`, `/users`)
- **Login:** `POST /auth/login` (Body: email, password).
- **Signup:** `POST /auth/signup` (Body: email, password, username, nickname, etc.).
- **My Profile:** `GET /users/me` (Response: `UserDetailResponse`).
- **User Search:** `GET /users/search/{nick}/info` (Response: `UserDetailResponse`).
- **User Content List:** `GET /users/{nick}/contents` (Query: page, size, sort, etc.).

### B. Content Discovery & Streaming (`/contents`)
- **Search API:** `GET /contents`
    - **Query Params:**
        - `keyword`: Search text (Title, Description, etc.)
        - `page`, `size`: Pagination
        - `tag`: Filter by tags (can be repeated)
        - `tag-mode`: `AND` | `OR`
        - `sort`: `view` (View Count), `createdAt` (Date)
        - `asc`: `true` | `false`
- **Video Format:** HLS (`.m3u8`).
- **Player:** Use `HLS.js` to play the URL provided in the content response.
- **Upload Flow (Admin/Creator):**
  1. `POST /api/assets/presign` -> Get Presigned URL.
  2. `PUT` file to the S3 URL directly (Frontend uploads to AWS).
  3. `POST /api/assets/complete` -> Notify backend to start processing.

### C. Reviews (`/contents/reviews`)
- **List:** `GET /contents/reviews/search/{contentId}` (Response: `PageResponse<ReviewListResponse>`).
- **Create:** `POST /contents/reviews` (Body: `ReviewCreateRequest`).
- **Update:** `PUT /contents/reviews/{reviewId}`.
- **Delete:** `DELETE /contents/reviews/{reviewId}`.

### D. Theater (Synchronized Viewing)
- **Concept:** Multiple users watch a movie together at a fixed schedule.
- **WebSocket:**
  - **Endpoint:** `ws://localhost:8080/ws` (or `/ws-sockjs` for fallback).
  - **Library:** `@stomp/stompjs`.
  - **Topic:** `/topic/theaters/{scheduleId}/state`.
  - **Logic:**
    - Connect to WS on entering the theater page.
    - Subscribe to the topic.
    - **Sync Logic:** The server sends `PlaybackState`.
      - If `state` is `PLAYING`, seek video to `currentPosition` (server time).
      - Disable user controls (Pause/Seek) generally, or re-sync if they drift.

### E. Subscription & Payments (`/users/subscriptions`)
- **Provider:** Toss Payments.
- **Flow:**
  1. Frontend uses Toss Payments Widget/SDK to authorize a card.
  2. On success, Toss returns a `paymentKey` or `authKey`.
  3. Call `POST /users/subscriptions` with the key to finalize subscription on the backend.

---

## 4. Key TypeScript Interfaces (Based on DTOs)

### Auth & User
```typescript
interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  grantType: string;
  expiresIn: number;
}

interface UserDetailResponse {
  userId: number;
  email: string;
  nickname: string;
  profileImage: string | null; // S3 Object Key
  seller: boolean; // Creator status
}
```

### Content & Search
```typescript
interface ContentSearchResponse {
  contentId: number;
  title: string;
  description: string;
  posterImage: string | null; // S3 Key for thumbnail
  status: 'PUBLISHED' | 'DRAFT' | 'PRIVATE';
  ownerNickname: string;
  totalView: number;
}

interface Content {
  id: number;
  title: string;
  description: string;
  thumbnailUrl: string;
  videoUrl: string; // m3u8 URL
  director: string;
  releaseYear: number;
  tags: string[];
}
```

### Reviews
```typescript
interface ReviewCreateRequest {
  rating: number; // 1 ~ 5
  'content-id': number; // Note the kebab-case in JSON
  comment?: string;
}

interface ReviewListResponse {
  reviewId: number;
  rating: number;
  comment: string;
  writerNickname: string;
  createdAt: string; // ISO DateTime
}
```

### Schedule & Theater
```typescript
interface ScheduleItem {
  id: number;
  contentId: number;
  contentTitle: string;
  startAt: string; // ISO DateTime
  endAt: string;
}

interface PlaybackState {
  scheduleId: number;
  isPlaying: boolean;
  currentPositionSeconds: number; // Sync to this timestamp
  timestamp: string; // Server time of calculation
}
```

### Payment
```typescript
interface Subscription {
  id: number;
  status: 'ACTIVE' | 'EXPIRED' | 'CANCELED';
  nextPaymentDate: string;
  billingProvider: 'TOSS';
}
```

---

## 5. Directory Structure Suggestion (Next.js)
```
/app
  /(auth)/login/page.tsx
  /(auth)/signup/page.tsx
  /(main)/page.tsx             // Home (Featured Content)
  /(main)/search/page.tsx      // Search Results (Grid of ContentSearchResponse)
  /(main)/contents/[id]/page.tsx // VOD Player & Reviews
  /(main)/theater/[id]/page.tsx  // Sync Theater Player
  /(main)/users/[nick]/page.tsx  // User Profile & Content List
  /components
    /player/HlsPlayer.tsx
    /theater/ChatRoom.tsx
    /review/ReviewList.tsx
    /review/ReviewForm.tsx
    /ui/button.tsx
  /lib
    /api.ts                 // Axios instance
    /store.ts               // Zustand store (Auth, etc.)
    /hooks/useWebSocket.ts  // Stomp hook
```
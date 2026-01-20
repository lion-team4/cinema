export interface ApiResponse<T> {
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface UserDetailResponse {
  userId: number;
  email: string;
  nickname: string;
  profileImage: string | null;
  seller: boolean;
}

export type ContentStatus = 'PUBLISHED' | 'DRAFT' | 'PRIVATE';

export interface ContentSearchResponse {
  contentId: number;
  title: string;
  description: string;
  posterImage: string | null;
  status: ContentStatus;
  ownerNickname: string;
  totalView: number;
  durationMs?: number | null;
}

export interface ContentDetail {
  id: number;
  title: string;
  description: string;
  thumbnailUrl: string;
  videoUrl: string; // m3u8 URL
  director: string;
  releaseYear: number;
  tags: string[];
}

export interface ReviewListResponse {
  reviewId: number;
  rating: number;
  comment: string | null;
  writerNickname: string;
  createdAt: string;
}

export interface PlaybackState {
  scheduleId: number;
  isPlaying: boolean;
  currentPositionSeconds: number;
  timestamp: string;
}

export interface SubscriptionResponse {
  id: number;
  status: 'ACTIVE' | 'EXPIRED' | 'CANCELED';
  nextPaymentDate: string;
  billingProvider?: 'TOSS' | string;
}

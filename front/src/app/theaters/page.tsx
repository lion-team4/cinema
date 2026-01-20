'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { type ApiResponse, type ContentSearchResponse, type PageResponse, type ReviewListResponse } from '@/types';
import { useAuthStore } from '@/lib/store';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import SectionHeader from '@/components/ui/SectionHeader';

type ScheduleStatus = 'WAITING' | 'PLAYING' | 'ENDING' | 'CLOSED';

type ScheduleSearchResponse = {
  scheduleItemId: number;
  contentId: number;
  contentTitle: string;
  creatorNickname: string;
  startAt: string;
  endAt: string;
  status: ScheduleStatus;
  isLocked: boolean;
};

export default function TheatersPage() {
  const router = useRouter();
  const { user } = useAuthStore();

  const [contents, setContents] = useState<ContentSearchResponse[]>([]);
  const [schedules, setSchedules] = useState<ScheduleSearchResponse[]>([]);
  const [viewerCounts, setViewerCounts] = useState<Record<number, number>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState('');
  const [detail, setDetail] = useState<{
    contentId: number;
    title: string;
    description: string;
    status: 'PUBLISHED' | 'DRAFT' | 'PRIVATE';
    ownerNickname: string;
    posterUrl: string | null;
    videoUrl: string | null;
    durationMs: number | null;
    totalView: number | null;
    tags: string[];
  } | null>(null);
  const [detailSchedules, setDetailSchedules] = useState<ScheduleSearchResponse[]>([]);
  const [detailReviews, setDetailReviews] = useState<ReviewListResponse[]>([]);
  const [detailScheduleId, setDetailScheduleId] = useState<number | null>(null);

  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [reviewSubmitting, setReviewSubmitting] = useState(false);
  const [reviewError, setReviewError] = useState('');
  const [reviewFilter, setReviewFilter] = useState<'ALL' | '5' | '4' | '3' | '2' | '1'>('ALL');
  const [reviewSort, setReviewSort] = useState<'RECENT' | 'RATING_DESC' | 'RATING_ASC'>('RECENT');

  const [editingReviewId, setEditingReviewId] = useState<number | null>(null);
  const [editingRating, setEditingRating] = useState(5);
  const [editingComment, setEditingComment] = useState('');

  const formatKst = (value: string) =>
    new Intl.DateTimeFormat('ko-KR', {
      timeZone: 'Asia/Seoul',
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));

  const normalizeUrl = (value: string | null) => {
    if (!value) return null;
    if (value.startsWith('https://https://')) return value.replace('https://https://', 'https://');
    if (value.startsWith('http://http://')) return value.replace('http://http://', 'http://');
    if (value.startsWith('http://') || value.startsWith('https://')) return value;
    return `https://${value}`;
  };

  const fetchContents = async () => {
    const { data } = await api.get<ApiResponse<PageResponse<ContentSearchResponse>>>('/contents', {
      params: { page: 0, size: 200, status: 'PUBLISHED' },
    });
    return data.data?.content ?? [];
  };

  const fetchSchedules = async () => {
    const { data } = await api.get<ApiResponse<PageResponse<ScheduleSearchResponse>>>('/schedules', {
      params: { page: 0, size: 200 },
    });
    return data.data?.content ?? [];
  };

  const loadReviews = async (contentId: number) => {
    try {
      const reviews = await api.get<ApiResponse<PageResponse<ReviewListResponse>>>(
        `/contents/reviews/search/${contentId}`,
        { params: { page: 0, size: 20 } }
      );
      setDetailReviews(reviews.data.data?.content ?? []);
    } catch {
      setDetailReviews([]);
    }
  };

  const loadSchedulesForContent = async (contentId: number) => {
    try {
      const { data } = await api.get<ApiResponse<PageResponse<ScheduleSearchResponse>>>('/schedules', {
        params: { page: 0, size: 200 },
      });
      const list = data.data?.content ?? [];
      setDetailSchedules(list.filter((item) => item.contentId === contentId));
    } catch {
      setDetailSchedules([]);
    }
  };

  const loadViewerCounts = async (list: ScheduleSearchResponse[]) => {
    const counts = await Promise.all(
      list.map(async (schedule) => {
        try {
          const res = await api.get<ApiResponse<number>>(`/theaters/${schedule.scheduleItemId}/viewers`);
          return [schedule.scheduleItemId, res.data.data ?? 0] as const;
        } catch {
          return [schedule.scheduleItemId, 0] as const;
        }
      })
    );
    setViewerCounts(Object.fromEntries(counts));
  };

  useEffect(() => {
    let active = true;
    const load = async () => {
      try {
        setLoading(true);
        setError('');
        const [contentList, scheduleList] = await Promise.all([fetchContents(), fetchSchedules()]);
        if (!active) return;
        setContents(contentList);
        setSchedules(scheduleList);
        await loadViewerCounts(scheduleList);
      } catch {
        if (!active) return;
        setError('상영관 정보를 불러오지 못했습니다.');
      } finally {
        if (active) setLoading(false);
      }
    };
    load();
    const interval = window.setInterval(load, 10000);
    return () => {
      active = false;
      window.clearInterval(interval);
    };
  }, []);

  const openDetail = async (contentId: number, scheduleItemId?: number | null) => {
    try {
      setDetailOpen(true);
      setDetailLoading(true);
      setDetailError('');
      setReviewError('');
      setEditingReviewId(null);
      setDetailScheduleId(scheduleItemId ?? null);
      const { data } = await api.get<ApiResponse<{
        contentId: number;
        title: string;
        description: string;
        status: 'PUBLISHED' | 'DRAFT' | 'PRIVATE';
        ownerNickname: string;
        posterUrl: string | null;
        videoUrl: string | null;
        durationMs: number | null;
        totalView: number | null;
        tags: string[];
      }>>(`/contents/${contentId}`);
      const payload = data.data;
      setDetail(
        payload
          ? {
              contentId: payload.contentId,
              title: payload.title,
              description: payload.description,
              status: payload.status,
              ownerNickname: payload.ownerNickname,
              posterUrl: normalizeUrl(payload.posterUrl),
              videoUrl: normalizeUrl(payload.videoUrl),
              durationMs: payload.durationMs,
              totalView: payload.totalView ?? null,
              tags: payload.tags,
            }
          : null
      );
      await loadSchedulesForContent(contentId);
      await loadReviews(contentId);
    } catch (err: any) {
      setDetailError(err.response?.data?.message || '상세 정보를 불러오지 못했습니다.');
    } finally {
      setDetailLoading(false);
    }
  };

  const closeDetail = () => {
    setDetailOpen(false);
    setDetail(null);
    setDetailReviews([]);
    setDetailError('');
    setReviewError('');
    setReviewComment('');
    setReviewRating(5);
    setEditingReviewId(null);
    setDetailScheduleId(null);
    setDetailSchedules([]);
  };

  const handleCreateReview = async () => {
    if (!detail || !user) {
      setReviewError('로그인이 필요합니다.');
      return;
    }
    if (detailReviews.some((review) => review.writerNickname === user.nickname)) {
      setReviewError('이미 리뷰를 작성했습니다. 리뷰는 1개만 작성할 수 있습니다.');
      return;
    }

    try {
      setReviewSubmitting(true);
      setReviewError('');
      await api.post('/contents/reviews', {
        contentId: detail.contentId,
        rating: reviewRating,
        comment: reviewComment.trim() || null,
      });
      setReviewComment('');
      setReviewRating(5);
      await loadReviews(detail.contentId);
    } catch (err: any) {
      setReviewError(err.response?.data?.message || '리뷰 작성에 실패했습니다.');
    } finally {
      setReviewSubmitting(false);
    }
  };

  const handleEditReview = (review: ReviewListResponse) => {
    setEditingReviewId(review.reviewId);
    setEditingRating(review.rating);
    setEditingComment(review.comment ?? '');
  };

  const handleUpdateReview = async () => {
    if (!editingReviewId || !detail) return;

    try {
      setReviewSubmitting(true);
      setReviewError('');
      await api.put(`/contents/reviews/${editingReviewId}`, {
        rating: editingRating,
        comment: editingComment.trim() || null,
      });
      setEditingReviewId(null);
      await loadReviews(detail.contentId);
    } catch (err: any) {
      setReviewError(err.response?.data?.message || '리뷰 수정에 실패했습니다.');
    } finally {
      setReviewSubmitting(false);
    }
  };

  const handleDeleteReview = async (reviewId: number) => {
    if (!detail) return;
    if (!window.confirm('리뷰를 삭제하시겠습니까?')) return;

    try {
      setReviewSubmitting(true);
      await api.delete(`/contents/reviews/${reviewId}`);
      await loadReviews(detail.contentId);
    } catch (err: any) {
      setReviewError(err.response?.data?.message || '리뷰 삭제에 실패했습니다.');
    } finally {
      setReviewSubmitting(false);
    }
  };

  const filteredReviews = useMemo(() => {
    let list = detailReviews;
    if (reviewFilter !== 'ALL') {
      const rating = Number(reviewFilter);
      list = list.filter((review) => review.rating === rating);
    }
    if (reviewSort === 'RATING_DESC') {
      list = [...list].sort((a, b) => b.rating - a.rating);
    } else if (reviewSort === 'RATING_ASC') {
      list = [...list].sort((a, b) => a.rating - b.rating);
    }
    return list;
  }, [detailReviews, reviewFilter, reviewSort]);

  const reviewStats = useMemo(() => {
    if (detailReviews.length === 0) return { average: 0, count: 0 };
    const total = detailReviews.reduce((sum, review) => sum + review.rating, 0);
    return { average: total / detailReviews.length, count: detailReviews.length };
  }, [detailReviews]);

  const [now, setNow] = useState(Date.now());
  useEffect(() => {
    const interval = window.setInterval(() => setNow(Date.now()), 30000);
    return () => window.clearInterval(interval);
  }, []);

  const playingSchedules = useMemo(() => schedules.filter((s) => s.status === 'PLAYING'), [schedules]);
  const waitingSchedules = useMemo(() => schedules.filter((s) => s.status === 'WAITING'), [schedules]);
  const upcomingSchedules = useMemo(
    () =>
      schedules.filter(
        (s) => s.status === 'CLOSED' && new Date(s.startAt).getTime() > now
      ),
    [now, schedules]
  );
  const activeSchedules = useMemo(
    () => [...waitingSchedules, ...playingSchedules],
    [playingSchedules, waitingSchedules]
  );

  const getScheduleLabel = (schedule: ScheduleSearchResponse) => {
    if (schedule.status === 'PLAYING') return '상영 중';
    if (schedule.status === 'WAITING') return '대기 중';
    return '상영 예정';
  };
  const topSchedules = useMemo(() => {
    const scored = activeSchedules.map((schedule) => ({
      schedule,
      viewers: viewerCounts[schedule.scheduleItemId] ?? 0,
    }));
    return scored.sort((a, b) => b.viewers - a.viewers).slice(0, 3);
  }, [activeSchedules, viewerCounts]);

  const formatRemain = (target: string) => {
    const diff = new Date(target).getTime() - now;
    if (Number.isNaN(diff)) return '시간 정보 없음';
    const minutes = Math.max(0, Math.floor(diff / 60000));
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) return `${hours}시간 ${mins}분`;
    return `${mins}분`;
  };

  const renderScheduleCards = (list: ScheduleSearchResponse[], label: string) => (
    <div className="mt-10">
      <h2 className="text-lg font-semibold section-title">{label}</h2>
      <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {list.map((schedule) => {
          const content = contents.find((item) => item.contentId === schedule.contentId);
          return (
            <button
              key={schedule.scheduleItemId}
              type="button"
              onClick={() => openDetail(schedule.contentId, schedule.scheduleItemId)}
              className="rounded-xl border border-white/10 bg-white/5 p-4 text-left transition-colors hover-soft"
            >
              <div className="aspect-video w-full overflow-hidden rounded-lg border border-white/10 bg-black/40">
                {content?.posterImage ? (
                  <img
                    src={content.posterImage}
                    alt={schedule.contentTitle}
                    className="h-full w-full object-cover"
                  />
                ) : (
                  <div className="flex h-full w-full items-center justify-center text-xs text-white/50">
                    포스터 없음
                  </div>
                )}
              </div>
              <div className="mt-3 flex items-center justify-between text-xs text-white/60">
                <Badge>{getScheduleLabel(schedule)}</Badge>
                <span>동접 {viewerCounts[schedule.scheduleItemId] ?? 0}명</span>
              </div>
              <div className="mt-1 text-xs text-white/50">
                {schedule.status === 'PLAYING'
                  ? `종료까지 ${formatRemain(schedule.endAt)}`
                  : `${formatKst(schedule.startAt)} · ${formatRemain(schedule.startAt)} 후 시작`}
              </div>
              <h3 className="mt-2 text-lg font-semibold">{schedule.contentTitle}</h3>
              <p className="mt-1 text-xs text-white/60">감독 {schedule.creatorNickname}</p>
            </button>
          );
        })}
      </div>
    </div>
  );

  return (
    <div className="mx-auto max-w-6xl px-6 py-12 text-white">
      <SectionHeader
        title="상영관"
        subtitle="현재 상영 중이거나 대기 중인 영화 목록입니다."
      />

      {loading && (
        <div className="mt-10 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/60">
          상영관 정보를 불러오는 중입니다...
        </div>
      )}

      {!loading && error && (
        <div className="mt-10 rounded-lg border border-red-500/30 bg-red-500/10 p-6 text-sm text-red-200">
          {error}
        </div>
      )}

      {!loading && !error && topSchedules.length > 0 && (
        <Card className="mt-8 p-6">
          <h2 className="text-lg font-semibold section-title">인기 상영관 TOP</h2>
          <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {topSchedules.map(({ schedule, viewers }) => {
              const content = contents.find((item) => item.contentId === schedule.contentId);
              return (
                <button
                  key={schedule.scheduleItemId}
                  type="button"
                  onClick={() => openDetail(schedule.contentId, schedule.scheduleItemId)}
                  className="rounded-xl border border-white/10 bg-black/40 p-4 text-left transition-colors hover-soft"
                >
                  <div className="aspect-video w-full overflow-hidden rounded-lg border border-white/10 bg-black/40">
                    {content?.posterImage ? (
                      <img
                        src={content.posterImage}
                        alt={schedule.contentTitle}
                        className="h-full w-full object-cover"
                      />
                    ) : (
                      <div className="flex h-full w-full items-center justify-center text-xs text-white/50">
                        포스터 없음
                      </div>
                    )}
                  </div>
                  <div className="mt-3 flex items-center justify-between text-xs text-white/60">
                    <Badge>{schedule.status === 'PLAYING' ? '상영 중' : '대기 중'}</Badge>
                    <span>동접 {viewers}명</span>
                  </div>
                  <h3 className="mt-2 text-lg font-semibold">{schedule.contentTitle}</h3>
                  <p className="mt-1 text-xs text-white/60">감독 {schedule.creatorNickname}</p>
                </button>
              );
            })}
          </div>
        </Card>
      )}

      {!loading && !error && playingSchedules.length > 0 && renderScheduleCards(playingSchedules, '상영 중')}
      {!loading && !error && waitingSchedules.length > 0 && renderScheduleCards(waitingSchedules, '대기 중')}
      {!loading && !error && upcomingSchedules.length > 0 && renderScheduleCards(upcomingSchedules, '상영 예정')}
      {!loading &&
        !error &&
        playingSchedules.length === 0 &&
        waitingSchedules.length === 0 &&
        upcomingSchedules.length === 0 && (
        <div className="mt-10 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/70">
          현재 상영 중인 콘텐츠가 없습니다.
        </div>
      )}

      {/* 상세 모달 */}
      {detailOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 px-4"
          onClick={(e) => { if (e.target === e.currentTarget) closeDetail(); }}
          onKeyDown={(e) => { if (e.key === 'Escape') closeDetail(); }}
        >
          <div className="w-full max-w-3xl max-h-[90vh] overflow-y-auto rounded-2xl border border-white/10 bg-black p-6 text-white card">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-semibold section-title">영화 상세</h2>
              <Button variant="ghost" size="sm" onClick={closeDetail}>
                닫기 ✕
              </Button>
            </div>

            {detailLoading && (
              <div className="mt-6 rounded-lg border border-white/10 bg-white/5 p-4 text-sm text-white/60">
                상세 정보를 불러오는 중입니다...
              </div>
            )}

            {!detailLoading && detailError && (
              <div className="mt-6 rounded-lg border border-red-500/30 bg-red-500/10 p-4 text-sm text-red-200">
                {detailError}
              </div>
            )}

            {!detailLoading && !detailError && detail && (
              <>
                <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_2fr]">
                  <div className="rounded-xl border border-white/10 bg-white/5 p-3 card">
                    {detail.posterUrl ? (
                      <img
                        src={detail.posterUrl}
                        alt={detail.title}
                        className="w-full rounded-lg border border-white/10 object-cover"
                      />
                    ) : (
                      <div className="flex h-72 items-center justify-center rounded-lg border border-white/10 bg-black/40 text-sm text-white/50">
                        포스터 없음
                      </div>
                    )}
                  </div>
                  <div className="space-y-4">
                    <div>
                      <h3 className="text-2xl font-bold">{detail.title}</h3>
                      <button
                        type="button"
                        onClick={() => router.push(`/filmography/${encodeURIComponent(detail.ownerNickname)}`)}
                        className="mt-1 text-sm text-white/60 hover:text-white hover:underline"
                      >
                        감독 {detail.ownerNickname}
                      </button>
                    </div>
                    <p className="text-sm text-white/70">{detail.description}</p>
                    {detail.tags.length > 0 && (
                      <div className="flex flex-wrap gap-2">
                        {detail.tags.map((tag) => (
                          <Badge key={tag}>#{tag}</Badge>
                        ))}
                      </div>
                    )}
                    <div className="text-xs text-white/60">
                      {detail.durationMs
                        ? `영상 길이 ${Math.ceil(detail.durationMs / 60000)}분`
                        : '영상 길이 정보 없음'}
                    </div>
                    <div className="text-xs text-white/60">
                      총 조회수 {detail.totalView ?? contents.find((item) => item.contentId === detail.contentId)?.totalView ?? 0}
                    </div>
                    <div className="text-sm text-white/80">
                      평균 평점 {reviewStats.average ? reviewStats.average.toFixed(1) : '0.0'} · 리뷰 {reviewStats.count}개
                    </div>
                  </div>
                </div>

                <div className="mt-6 rounded-lg border border-white/10 bg-white/5 p-4 card">
                  <h3 className="text-sm font-semibold section-title">상영 일정</h3>
                  {detailSchedules.filter(
                    (s) =>
                      s.status === 'WAITING' ||
                      s.status === 'PLAYING' ||
                      (s.status === 'CLOSED' && new Date(s.startAt).getTime() > now)
                  ).length === 0 ? (
                    <p className="mt-2 text-sm text-white/60">상영 예정이 없습니다.</p>
                  ) : (
                    <div className="mt-3 space-y-2 text-sm text-white/70">
                      {detailSchedules
                        .filter(
                          (s) =>
                            s.status === 'WAITING' ||
                            s.status === 'PLAYING' ||
                            (s.status === 'CLOSED' && new Date(s.startAt).getTime() > now)
                        )
                        .map((schedule) => (
                          <div key={schedule.scheduleItemId} className="flex items-center justify-between gap-3">
                            <span>
                              {getScheduleLabel(schedule)} · {formatKst(schedule.startAt)}
                            </span>
                            <Button
                              variant="primary"
                              size="sm"
                              onClick={() => router.push(`/watch/${schedule.scheduleItemId}`)}
                            >
                              입장하기
                            </Button>
                          </div>
                        ))}
                    </div>
                  )}
                </div>

                {/* 리뷰 섹션 */}
                <div className="mt-6">
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <h3 className="text-lg font-semibold section-title">리뷰</h3>
                    <div className="flex flex-wrap items-center gap-2 text-xs text-white/70">
                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => setReviewFilter('ALL')}
                        className={
                          reviewFilter === 'ALL'
                            ? 'border-red-500/60 bg-red-500/20 text-red-100'
                            : 'border-white/10 text-white/60'
                        }
                      >
                        전체
                      </Button>
                      {(['5', '4', '3', '2', '1'] as const).map((rating) => (
                        <Button
                          key={rating}
                          size="sm"
                          variant="secondary"
                          onClick={() => setReviewFilter(rating)}
                          className={
                            reviewFilter === rating
                              ? 'border-red-500/60 bg-red-500/20 text-red-100'
                              : 'border-white/10 text-white/60'
                          }
                        >
                          {rating}점
                        </Button>
                      ))}
                      <div className="flex items-center gap-2 rounded-full border border-white/10 bg-black/50 px-2 py-1">
                        {[
                          { value: 'RECENT', label: '최신순' },
                          { value: 'RATING_DESC', label: '별점 높은순' },
                          { value: 'RATING_ASC', label: '별점 낮은순' },
                        ].map((option) => (
                          <Button
                            key={option.value}
                            size="sm"
                            variant="secondary"
                            onClick={() => setReviewSort(option.value as typeof reviewSort)}
                            className={
                              reviewSort === option.value
                                ? 'bg-white/10 text-white border-white/40'
                                : 'text-white/60 hover:text-white'
                            }
                          >
                            {option.label}
                          </Button>
                        ))}
                      </div>
                    </div>
                  </div>

                  {user && (
                    <Card className="mt-4 p-4">
                      <div className="flex items-center gap-3 mb-3">
                        <label className="text-sm text-white/70">별점</label>
                        <select
                          value={reviewRating}
                          onChange={(e) => setReviewRating(Number(e.target.value))}
                          className="rounded-md border border-white/10 bg-black px-3 py-1 text-sm text-white"
                        >
                          {[5, 4, 3, 2, 1].map((v) => (
                            <option key={v} value={v} className="bg-black">{v}점</option>
                          ))}
                        </select>
                      </div>
                      <textarea
                        value={reviewComment}
                        onChange={(e) => setReviewComment(e.target.value)}
                        rows={2}
                        placeholder="리뷰를 작성해주세요."
                        className="w-full rounded-md border border-white/10 bg-black/60 px-3 py-2 text-sm text-white placeholder:text-white/40"
                      />
                      {reviewError && (
                        <p className="mt-2 text-sm text-red-300">{reviewError}</p>
                      )}
                      <Button
                        variant="primary"
                        onClick={handleCreateReview}
                        disabled={reviewSubmitting}
                        className="mt-3"
                      >
                        리뷰 작성
                      </Button>
                    </Card>
                  )}

                  {!user && (
                    <p className="mt-4 text-sm text-white/60">
                      리뷰를 작성하려면 <a href="/login" className="text-red-400 hover:underline">로그인</a>하세요.
                    </p>
                  )}

                  <div className="mt-4 space-y-3">
                    {filteredReviews.length === 0 && (
                      <p className="text-sm text-white/60">아직 리뷰가 없습니다.</p>
                    )}
                    {filteredReviews.map((review) => (
                      <Card key={review.reviewId} className="p-3 text-sm">
                        <div className="flex items-center justify-between text-white/60">
                          <span>{review.writerNickname}</span>
                          <span>{new Date(review.createdAt).toLocaleString()}</span>
                        </div>

                        {editingReviewId === review.reviewId ? (
                          <div className="mt-3 space-y-3">
                            <select
                              value={editingRating}
                              onChange={(e) => setEditingRating(Number(e.target.value))}
                              className="rounded-md border border-white/10 bg-black px-3 py-1 text-sm text-white"
                            >
                              {[5, 4, 3, 2, 1].map((v) => (
                                <option key={v} value={v} className="bg-black">{v}점</option>
                              ))}
                            </select>
                            <textarea
                              value={editingComment}
                              onChange={(e) => setEditingComment(e.target.value)}
                              rows={2}
                              className="w-full rounded-md border border-white/10 bg-black/60 px-3 py-2 text-sm text-white"
                            />
                            <div className="flex gap-2">
                              <Button
                                size="sm"
                                variant="primary"
                                onClick={handleUpdateReview}
                                disabled={reviewSubmitting}
                              >
                                저장
                              </Button>
                              <Button
                                size="sm"
                                variant="secondary"
                                onClick={() => setEditingReviewId(null)}
                              >
                                취소
                              </Button>
                            </div>
                          </div>
                        ) : (
                          <>
                            <p className="mt-2 text-white/80">⭐ {review.rating}점</p>
                            <p className="mt-1 text-white/70">{review.comment || '코멘트 없음'}</p>
                            {user?.nickname === review.writerNickname && (
                              <div className="mt-3 flex gap-2">
                                <Button
                                  size="sm"
                                  variant="secondary"
                                  onClick={() => handleEditReview(review)}
                                >
                                  수정
                                </Button>
                                <Button
                                  size="sm"
                                  variant="secondary"
                                  onClick={() => handleDeleteReview(review.reviewId)}
                                  disabled={reviewSubmitting}
                                  className="border-red-500/40 text-red-200 hover:border-red-400"
                                >
                                  삭제
                                </Button>
                              </div>
                            )}
                          </>
                        )}
                      </Card>
                    ))}
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

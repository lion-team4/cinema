'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { api } from '@/lib/api';
import { type ApiResponse, type ContentSearchResponse, type PageResponse, type ReviewListResponse } from '@/types';
import { useAuthStore } from '@/lib/store';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import SectionHeader from '@/components/ui/SectionHeader';

type SortField = 'CREATED' | 'UPDATED' | 'VIEW';
type ScheduleStatus = 'WAITING' | 'PLAYING' | 'CLOSED' | 'ENDED';

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

export default function SearchPage() {
  const { user } = useAuthStore();
  const router = useRouter();
  const searchParams = useSearchParams();
  const keyword = searchParams.get('keyword') || '';
  const contentIdParam = searchParams.get('contentId');
  const reviewParam = searchParams.get('review') === '1';
  const titleOnly = searchParams.get('title') !== 'false';
  const sort = (searchParams.get('sort') as SortField) || 'CREATED';
  const asc = searchParams.get('asc') === 'true';
  const pageParam = Number(searchParams.get('page') || 0);
  const sizeParam = Number(searchParams.get('size') || 20);
  const tagParam = searchParams.get('tags') || '';

  const [query, setQuery] = useState(keyword);
  const [sortField, setSortField] = useState<SortField>(sort);
  const [isAsc, setIsAsc] = useState(asc);
  const [searchTitleOnly, setSearchTitleOnly] = useState(titleOnly);
  const [page, setPage] = useState(Number.isNaN(pageParam) ? 0 : pageParam);
  const [size, setSize] = useState(Number.isNaN(sizeParam) ? 20 : sizeParam);
  const [tagsInput, setTagsInput] = useState(tagParam);

  const [loading, setLoading] = useState(true);
  const [items, setItems] = useState<ContentSearchResponse[]>([]);
  const [error, setError] = useState('');
  const [totalPages, setTotalPages] = useState(0);
  const [detailSchedules, setDetailSchedules] = useState<ScheduleSearchResponse[]>([]);
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
  const [detailReviews, setDetailReviews] = useState<ReviewListResponse[]>([]);

  // 리뷰 작성 상태
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [reviewSubmitting, setReviewSubmitting] = useState(false);
  const [reviewError, setReviewError] = useState('');
  const [reviewFilter, setReviewFilter] = useState<'ALL' | '5' | '4' | '3' | '2' | '1'>('ALL');
  const [reviewSort, setReviewSort] = useState<'RECENT' | 'RATING_DESC' | 'RATING_ASC'>('RECENT');
  const [autoFocusReview, setAutoFocusReview] = useState(false);
  const reviewTextareaRef = useRef<HTMLTextAreaElement | null>(null);
  const openedFromParamRef = useRef(false);
  const [recentKeywords, setRecentKeywords] = useState<string[]>([]);
  const [recentTags, setRecentTags] = useState<string[]>([]);

  // 리뷰 수정 상태
  const [editingReviewId, setEditingReviewId] = useState<number | null>(null);
  const [editingRating, setEditingRating] = useState(5);
  const [editingComment, setEditingComment] = useState('');

  const formatKst = (value: string) =>
    new Intl.DateTimeFormat('ko-KR', {
      timeZone: 'Asia/Seoul',
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));

  useEffect(() => {
    setQuery(keyword);
    setSortField(sort);
    setIsAsc(asc);
    setSearchTitleOnly(titleOnly);
    setPage(Number.isNaN(pageParam) ? 0 : pageParam);
    setSize(Number.isNaN(sizeParam) ? 20 : sizeParam);
    setTagsInput(tagParam);
  }, [asc, keyword, pageParam, sizeParam, sort, tagParam, titleOnly]);

  useEffect(() => {
    if (keyword || tagParam || searchParams.get('sort') || searchParams.get('asc') || searchParams.get('title')) {
      return;
    }
    const stored = localStorage.getItem('search-preferences');
    if (!stored) return;
    try {
      const parsed = JSON.parse(stored) as {
        sortField?: SortField;
        isAsc?: boolean;
        searchTitleOnly?: boolean;
      };
      if (parsed.sortField) setSortField(parsed.sortField);
      if (typeof parsed.isAsc === 'boolean') setIsAsc(parsed.isAsc);
      if (typeof parsed.searchTitleOnly === 'boolean') setSearchTitleOnly(parsed.searchTitleOnly);
    } catch {
      // ignore
    }
  }, [keyword, searchParams, tagParam]);

  useEffect(() => {
    localStorage.setItem(
      'search-preferences',
      JSON.stringify({ sortField, isAsc, searchTitleOnly })
    );
  }, [isAsc, searchTitleOnly, sortField]);

  useEffect(() => {
    const storedKeywords = localStorage.getItem('search-history');
    const storedTags = localStorage.getItem('search-tags');
    if (storedKeywords) {
      try {
        setRecentKeywords(JSON.parse(storedKeywords));
      } catch {
        // ignore
      }
    }
    if (storedTags) {
      try {
        setRecentTags(JSON.parse(storedTags));
      } catch {
        // ignore
      }
    }
  }, []);

  useEffect(() => {
    if (openedFromParamRef.current) return;
    const raw = sessionStorage.getItem('search-detail-intent');
    if (!raw) return;
    try {
      const parsed = JSON.parse(raw) as { contentId?: number; focusReview?: boolean };
      if (!parsed.contentId) return;
      openedFromParamRef.current = true;
      sessionStorage.removeItem('search-detail-intent');
      setAutoFocusReview(!!parsed.focusReview);
      void openDetail(parsed.contentId);
    } catch {
      sessionStorage.removeItem('search-detail-intent');
    }
  }, []);

  useEffect(() => {
    if (!contentIdParam) return;
    if (openedFromParamRef.current) return;
    const id = Number(contentIdParam);
    if (Number.isNaN(id)) return;
    openedFromParamRef.current = true;
    setAutoFocusReview(reviewParam);
    (async () => {
      await openDetail(id);
      const cleaned = new URLSearchParams(searchParams.toString());
      cleaned.delete('contentId');
      cleaned.delete('review');
      const nextQuery = cleaned.toString();
      router.replace(nextQuery ? `/search?${nextQuery}` : '/search');
    })();
  }, [contentIdParam, reviewParam, router, searchParams]);

  const params = useMemo(() => {
    const base: Record<string, string | number | boolean | string[]> = {
      page,
      size,
      sort: sortField,
      asc: isAsc,
      title: searchTitleOnly,
      status: 'PUBLISHED',
    };
    if (keyword) base.keyword = keyword;

    const tagList = tagsInput
      .split(',')
      .map((tag) => tag.trim())
      .filter(Boolean);
    if (tagList.length > 0) {
      base.tags = tagList;
      base.filter = true;
      base.or = true;
    }

    return base;
  }, [isAsc, keyword, page, searchTitleOnly, size, sortField, tagsInput]);

  useEffect(() => {
    const fetchContents = async () => {
      try {
        setLoading(true);
        setError('');
        const { data } = await api.get<ApiResponse<PageResponse<ContentSearchResponse>>>('/contents', {
          params,
        });
        const list = data.data?.content ?? [];
        const filtered = list.filter((item) => item.status === 'PUBLISHED')
          .filter((item) => (user ? item.ownerNickname !== user.nickname : true));
        setItems(filtered);
        setTotalPages(data.data?.totalPages ?? 0);
      } catch (err: any) {
        setError(err.response?.data?.message || '콘텐츠를 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchContents();
  }, [params]);

  const loadSchedules = async (contentId: number) => {
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

  const loadReviews = async (contentId: number) => {
    try {
      const reviews = await api.get<ApiResponse<PageResponse<ReviewListResponse>>>(
        `/contents/reviews/search/${contentId}`,
        { params: { page: 0, size: 20 } }
      );
      setDetailReviews(reviews.data.data?.content ?? []);
      } catch {
      // ignore
    }
  };

  const openDetail = async (contentId: number, scheduleItemId?: number | null) => {
    try {
      setDetailOpen(true);
      setDetailLoading(true);
      setDetailError('');
      setReviewError('');
      setEditingReviewId(null);
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
      const normalizeUrl = (value: string | null) => {
        if (!value) return null;
        if (value.startsWith('https://https://')) return value.replace('https://https://', 'https://');
        if (value.startsWith('http://http://')) return value.replace('http://http://', 'http://');
        if (value.startsWith('http://') || value.startsWith('https://')) return value;
        return `https://${value}`;
      };
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
      await loadSchedules(contentId);
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
    setDetailSchedules([]);
    setAutoFocusReview(false);
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

  useEffect(() => {
    if (!detailOpen || detailLoading || !autoFocusReview) return;
    if (!reviewTextareaRef.current) return;
    reviewTextareaRef.current.focus();
  }, [autoFocusReview, detailLoading, detailOpen]);

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

  const handleSearch = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const next = new URLSearchParams();
    if (query.trim()) next.set('keyword', query.trim());
    if (!searchTitleOnly) next.set('title', 'false');
    if (sortField !== 'CREATED') next.set('sort', sortField);
    if (isAsc) next.set('asc', 'true');
    if (tagsInput.trim()) next.set('tags', tagsInput.trim());
    if (page !== 0) next.set('page', String(page));
    if (size !== 20) next.set('size', String(size));
    router.replace(`/search?${next.toString()}`);

    const normalizedQuery = query.trim();
    if (normalizedQuery) {
      const updated = [normalizedQuery, ...recentKeywords.filter((item) => item !== normalizedQuery)].slice(0, 8);
      setRecentKeywords(updated);
      localStorage.setItem('search-history', JSON.stringify(updated));
    }

    const tagList = tagsInput
      .split(',')
      .map((tag) => tag.trim())
      .filter(Boolean);
    if (tagList.length > 0) {
      const merged = Array.from(new Set([...tagList, ...recentTags])).slice(0, 12);
      setRecentTags(merged);
      localStorage.setItem('search-tags', JSON.stringify(merged));
    }
  };

  const handlePageChange = (nextPage: number) => {
    const safePage = Math.max(0, Math.min(nextPage, Math.max(totalPages - 1, 0)));
    setPage(safePage);
    const next = new URLSearchParams(searchParams.toString());
    next.set('page', String(safePage));
    router.replace(`/search?${next.toString()}`);
  };

  return (
    <div className="mx-auto max-w-6xl px-6 py-12 text-white">
      <SectionHeader
        title="검색"
        subtitle={keyword ? `"${keyword}" 검색 결과` : '전체 콘텐츠'}
      />

      <Card className="mt-6">
        <form onSubmit={handleSearch}>
        <div className="grid gap-4 lg:grid-cols-[2fr_2fr_auto] lg:items-center">
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="제목 또는 닉네임으로 검색"
            className="rounded-md border border-white/10 bg-white/5 px-4 py-2 text-white placeholder:text-white/40 focus:ring-red-500 focus:border-red-500"
          />
          <input
            value={tagsInput}
            onChange={(e) => setTagsInput(e.target.value)}
            placeholder="태그 검색 (쉼표로 구분)"
            className="rounded-md border border-white/10 bg-white/5 px-4 py-2 text-white placeholder:text-white/40 focus:ring-red-500 focus:border-red-500"
          />
          <Button type="submit" variant="primary" className="px-5 py-2">
            검색
          </Button>
        </div>

        {(recentKeywords.length > 0 || recentTags.length > 0) && (
          <div className="mt-4 grid gap-3 text-xs text-white/70">
            {recentKeywords.length > 0 && (
              <div className="flex flex-wrap items-center gap-2">
                <span className="text-white/50 section-subtitle">최근 검색</span>
                {recentKeywords.map((item) => (
                  <Button
                    key={item}
                    size="sm"
                    variant="secondary"
                    onClick={() => {
                      setQuery(item);
                      const next = new URLSearchParams(searchParams.toString());
                      next.set('keyword', item);
                      router.replace(`/search?${next.toString()}`);
                    }}
                    className="badge"
                  >
                    {item}
                  </Button>
                ))}
              </div>
            )}
            {recentTags.length > 0 && (
              <div className="flex flex-wrap items-center gap-2">
                <span className="text-white/50 section-subtitle">추천 태그</span>
                {recentTags.map((tag) => (
                  <Button
                    key={tag}
                    size="sm"
                    variant="secondary"
                    onClick={() => {
                      const nextValue = tagsInput ? `${tagsInput}, ${tag}` : tag;
                      setTagsInput(nextValue);
                    }}
                    className="badge"
                  >
                    #{tag}
                  </Button>
                ))}
              </div>
            )}
          </div>
        )}

        <div className="mt-4 flex flex-wrap items-center gap-3 text-sm text-white/70">
          <div className="flex items-center gap-2 rounded-full border border-white/10 bg-black/50 px-2 py-1">
            {[
              { value: 'CREATED', label: '최신순' },
              { value: 'VIEW', label: '조회수순' },
              { value: 'UPDATED', label: '업데이트순' },
            ].map((option) => (
              <Button
                key={option.value}
                size="sm"
                variant="secondary"
                onClick={() => setSortField(option.value as SortField)}
                className={
                  sortField === option.value
                    ? 'bg-white/10 text-white border-white/40'
                    : 'text-white/60 hover:text-white'
                }
              >
                {option.label}
              </Button>
            ))}
          </div>
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={isAsc}
              onChange={(e) => setIsAsc(e.target.checked)}
              className="h-4 w-4 rounded border-white/20 bg-white/10 text-red-500"
            />
            오름차순
          </label>
          <div className="flex items-center gap-2 rounded-full border border-white/10 bg-black/40 px-2 py-1">
            <Button
              size="sm"
              variant="secondary"
              onClick={() => setSearchTitleOnly(true)}
              className={searchTitleOnly ? 'bg-red-600 text-white' : 'text-white/60 hover:text-white'}
            >
              제목
            </Button>
            <Button
              size="sm"
              variant="secondary"
              onClick={() => setSearchTitleOnly(false)}
              className={!searchTitleOnly ? 'bg-red-600 text-white' : 'text-white/60 hover:text-white'}
            >
              감독명
            </Button>
          </div>
        </div>
      </form>
      </Card>


      {loading && (
        <div className="mt-10 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/60">
          콘텐츠를 불러오는 중입니다...
        </div>
      )}

      {!loading && error && (
        <div className="mt-10 rounded-lg border border-red-500/30 bg-red-500/10 p-6 text-sm text-red-200">
          {error}
        </div>
      )}

      {!loading && !error && items.length === 0 && (
        <div className="mt-10 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/70">
          콘텐츠가 없습니다. 새로운 작품을 업로드하거나 곧 공개될 콘텐츠를 기다려주세요.
        </div>
      )}

      {!loading && !error && items.length > 0 && (
        <div className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {items.map((item) => (
            <Card
              key={item.contentId}
              variant="compact"
              className="text-left cursor-pointer hover-soft"
              role="button"
              tabIndex={0}
              onClick={() => openDetail(item.contentId)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') openDetail(item.contentId);
              }}
            >
              <div className="aspect-video w-full overflow-hidden rounded-lg border border-white/10 bg-black/40">
                {item.posterImage ? (
                  <img
                    src={item.posterImage}
                    alt={item.title}
                    className="h-full w-full object-cover"
                  />
                ) : (
                  <div className="flex h-full w-full items-center justify-center text-xs text-white/50">
                    포스터 없음
                  </div>
                )}
              </div>
              <h3 className="mt-3 text-lg font-semibold">{item.title}</h3>
              <button
                type="button"
                onClick={(event) => {
                  event.stopPropagation();
                  router.push(`/filmography/${encodeURIComponent(item.ownerNickname)}`);
                }}
                className="mt-1 text-xs text-white/60 hover:text-white hover:underline"
              >
                감독 {item.ownerNickname}
              </button>
              <p className="mt-2 text-sm text-white/70 line-clamp-3">{item.description}</p>
              <div className="mt-3 text-xs text-white/50">
                조회수 {item.totalView} · {item.durationMs ? `${Math.ceil(item.durationMs / 60000)}분` : '길이 정보 없음'}
              </div>
            </Card>
          ))}
        </div>
      )}

      {!loading && !error && totalPages > 1 && (
        <div className="mt-10 flex items-center justify-center gap-4 text-sm text-white/70">
          <Button
            size="sm"
            variant="secondary"
            onClick={() => handlePageChange(page - 1)}
            disabled={page <= 0}
          >
            이전
          </Button>
          <span>
            {page + 1} / {totalPages}
          </span>
          <Button
            size="sm"
            variant="secondary"
            onClick={() => handlePageChange(page + 1)}
            disabled={page >= totalPages - 1}
          >
            다음
          </Button>
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
                  <Card variant="compact">
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
                  </Card>
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
                      총 조회수 {detail.totalView ?? items.find((item) => item.contentId === detail.contentId)?.totalView ?? 0}
                    </div>
                    <div className="text-sm text-white/80">
                      평균 평점 {reviewStats.average ? reviewStats.average.toFixed(1) : '0.0'} · 리뷰 {reviewStats.count}개
                    </div>
                  </div>
                </div>

                <Card className="mt-6 p-4" variant="solid">
                  <h3 className="text-sm font-semibold section-title">상영 일정</h3>
                  {detailSchedules.filter((s) => s.status === 'WAITING' || s.status === 'PLAYING').length === 0 ? (
                    <p className="mt-2 text-sm text-white/60">상영 예정이 없습니다.</p>
                  ) : (
                    <div className="mt-3 space-y-2 text-sm text-white/70">
                      {detailSchedules
                        .filter((s) => s.status === 'WAITING' || s.status === 'PLAYING')
                        .map((schedule) => (
                          <div key={schedule.scheduleItemId} className="flex items-center justify-between gap-3">
                            <span>
                              {schedule.status === 'PLAYING' ? '상영 중' : '대기 중'} · {formatKst(schedule.startAt)}
                            </span>
                            <Button
                              size="sm"
                              variant="primary"
                              onClick={() => router.push(`/watch/${schedule.scheduleItemId}`)}
                            >
                              입장하기
                            </Button>
                          </div>
                        ))}
                    </div>
                  )}
                </Card>

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

                  {/* 리뷰 작성 폼 */}
                  {user && (
                    <Card className="mt-4 p-4" variant="solid">
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
                        ref={reviewTextareaRef}
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
                      <Card key={review.reviewId} className="p-3 text-sm" variant="compact">
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

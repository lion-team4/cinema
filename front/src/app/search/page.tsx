'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { api } from '@/lib/api';
import { type ApiResponse, type ContentSearchResponse, type PageResponse } from '@/types';
import { useAuthStore } from '@/lib/store';

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
  const [liveSchedules, setLiveSchedules] = useState<ScheduleSearchResponse[]>([]);

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

  useEffect(() => {
    const fetchSchedules = async () => {
      try {
        const { data } = await api.get<ApiResponse<PageResponse<ScheduleSearchResponse>>>('/schedules', {
          params: { page: 0, size: 10 },
        });
        const list = data.data?.content ?? [];
        const filtered = list.filter((item) => item.status === 'WAITING' || item.status === 'PLAYING');
        setLiveSchedules(filtered);
      } catch {
        setLiveSchedules([]);
      }
    };

    fetchSchedules();
  }, []);

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
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-bold">검색</h1>
        <p className="text-sm text-white/60">
          {keyword ? `"${keyword}" 검색 결과` : '전체 콘텐츠'}
        </p>
      </div>

      <form onSubmit={handleSearch} className="mt-6 rounded-2xl border border-white/10 bg-white/5 p-5">
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
          <button
            type="submit"
            className="rounded-md bg-red-600 px-5 py-2 text-sm font-semibold text-white hover:bg-red-500 transition-colors"
          >
            검색
          </button>
        </div>

        <div className="mt-4 flex flex-wrap items-center gap-3 text-sm text-white/70">
          <select
            value={sortField}
            onChange={(e) => setSortField(e.target.value as SortField)}
            className="rounded-md border border-white/10 bg-black/60 px-3 py-2 text-sm text-white"
          >
            <option value="CREATED" className="bg-black text-white">최신순</option>
            <option value="UPDATED" className="bg-black text-white">업데이트순</option>
            <option value="VIEW" className="bg-black text-white">조회수순</option>
          </select>
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
            <button
              type="button"
              onClick={() => setSearchTitleOnly(true)}
              className={`rounded-full px-3 py-1 text-xs font-semibold transition-colors ${
                searchTitleOnly ? 'bg-red-600 text-white' : 'text-white/60 hover:text-white'
              }`}
            >
              제목
            </button>
            <button
              type="button"
              onClick={() => setSearchTitleOnly(false)}
              className={`rounded-full px-3 py-1 text-xs font-semibold transition-colors ${
                !searchTitleOnly ? 'bg-red-600 text-white' : 'text-white/60 hover:text-white'
              }`}
            >
              감독명
            </button>
          </div>
        </div>
      </form>

      {liveSchedules.length > 0 && (
        <div className="mt-10">
          <h2 className="text-lg font-semibold">지금 상영 중인 영화</h2>
          <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {liveSchedules.map((schedule) => (
              <div key={schedule.scheduleItemId} className="rounded-xl border border-white/10 bg-white/5 p-5">
                <div className="flex items-center justify-between text-xs text-white/60">
                  <span>{schedule.status === 'PLAYING' ? '상영 중' : '대기 중'}</span>
                  <span>{formatKst(schedule.startAt)}</span>
                </div>
                <h3 className="mt-3 text-lg font-semibold">{schedule.contentTitle}</h3>
                <p className="mt-2 text-xs text-white/60">감독 {schedule.creatorNickname}</p>
                <button
                  type="button"
                  onClick={() => router.push(`/watch/${schedule.scheduleItemId}`)}
                  className="mt-4 w-full rounded-md bg-red-600 py-2 text-sm font-semibold text-white hover:bg-red-500 transition-colors"
                >
                  상영하기
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

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
            <div key={item.contentId} className="rounded-lg border border-white/10 bg-white/5 p-4">
              <h3 className="text-lg font-semibold">{item.title}</h3>
              <p className="mt-2 text-sm text-white/70 line-clamp-3">{item.description}</p>
              <div className="mt-4 text-xs text-white/50">
                조회수 {item.totalView} · {item.ownerNickname}
              </div>
            </div>
          ))}
        </div>
      )}

      {!loading && !error && totalPages > 1 && (
        <div className="mt-10 flex items-center justify-center gap-4 text-sm text-white/70">
          <button
            type="button"
            onClick={() => handlePageChange(page - 1)}
            disabled={page <= 0}
            className="rounded-md border border-white/20 px-3 py-1.5 disabled:opacity-50"
          >
            이전
          </button>
          <span>
            {page + 1} / {totalPages}
          </span>
          <button
            type="button"
            onClick={() => handlePageChange(page + 1)}
            disabled={page >= totalPages - 1}
            className="rounded-md border border-white/20 px-3 py-1.5 disabled:opacity-50"
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
}


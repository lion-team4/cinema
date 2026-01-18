'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import { type ApiResponse, type ContentSearchResponse, type PageResponse } from '@/types';
import SubscriptionGate from '@/components/guards/SubscriptionGate';

const formatLocalDateTime = (date: Date) => {
  const pad = (value: number) => value.toString().padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}`;
};

export default function ScheduleManagePage() {
  const router = useRouter();
  const { user, hasHydrated } = useAuthStore();
  const [items, setItems] = useState<ContentSearchResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [startAt, setStartAt] = useState('');
  const [durationMinutes, setDurationMinutes] = useState<number>(90);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState('');

  const endAt = useMemo(() => {
    if (!startAt || !durationMinutes) return '';
    const startDate = new Date(startAt);
    if (Number.isNaN(startDate.getTime())) return '';
    const endDate = new Date(startDate.getTime() + durationMinutes * 60 * 1000);
    return formatLocalDateTime(endDate);
  }, [durationMinutes, startAt]);

  useEffect(() => {
    if (!hasHydrated) return;
    if (!user) {
      router.push('/login?redirect=/schedules/manage');
      return;
    }

    const fetchContents = async () => {
      try {
        setLoading(true);
        setError('');
        const { data } = await api.get<ApiResponse<PageResponse<ContentSearchResponse>>>('/contents', {
          params: {
            nickname: user.nickname,
            page: 0,
            size: 30,
          },
        });
        const list = data.data?.content ?? [];
        setItems(list);
        if (list.length > 0 && selectedId === null) {
          setSelectedId(list[0].contentId);
        }
      } catch (err: any) {
        setError(err.response?.data?.message || '콘텐츠 목록을 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchContents();
  }, [hasHydrated, router, selectedId, user]);

  const handleCreateSchedule = async () => {
    if (!selectedId) {
      setError('상영할 콘텐츠를 선택해주세요.');
      return;
    }
    if (!startAt) {
      setError('상영 시작 시간을 입력해주세요.');
      return;
    }
    if (!endAt) {
      setError('상영 종료 시간을 계산할 수 없습니다.');
      return;
    }

    try {
      setSaving(true);
      setError('');
      setNotice('');
      const scheduleDate = startAt.split('T')[0];
      await api.post('/schedules', {
        contentId: selectedId,
        scheduleDate,
        startAt,
        endAt,
      });
      setNotice('상영 일정이 등록되었습니다.');
    } catch (err: any) {
      setError(err.response?.data?.message || '상영 일정 등록에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <SubscriptionGate>
      <div className="mx-auto max-w-6xl px-6 py-12 text-white">
        <div className="flex flex-col gap-2">
          <h1 className="text-3xl font-bold">상영 스케줄 관리</h1>
          <p className="text-sm text-white/60">
            내 콘텐츠를 선택하고 상영 시간을 등록하세요.
          </p>
        </div>

      <div className="mt-8 grid gap-6 lg:grid-cols-[2fr_1fr]">
        <div className="rounded-2xl border border-white/10 bg-white/5 p-6">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">내 콘텐츠</h2>
            <a
              href="/contents/create"
              className="rounded-md border border-white/20 px-3 py-1.5 text-xs font-semibold text-white/80 hover:border-white/60 hover:text-white transition-colors"
            >
              새 영상 업로드
            </a>
          </div>

          {loading && (
            <div className="mt-6 rounded-lg border border-white/10 bg-white/5 p-4 text-sm text-white/60">
              콘텐츠를 불러오는 중입니다...
            </div>
          )}

          {!loading && items.length === 0 && (
            <div className="mt-6 rounded-lg border border-white/10 bg-white/5 p-4 text-sm text-white/70">
              아직 업로드한 콘텐츠가 없습니다. 업로드 후 스케줄을 등록해주세요.
            </div>
          )}

          {!loading && items.length > 0 && (
            <div className="mt-6 grid gap-4 sm:grid-cols-2">
              {items.map((item) => (
                <button
                  key={item.contentId}
                  type="button"
                  onClick={() => setSelectedId(item.contentId)}
                  className={`rounded-xl border px-4 py-4 text-left transition-colors ${
                    selectedId === item.contentId
                      ? 'border-red-500/60 bg-red-500/10'
                      : 'border-white/10 bg-white/5 hover:border-white/30'
                  }`}
                >
                  <h3 className="text-sm font-semibold">{item.title}</h3>
                  <p className="mt-2 text-xs text-white/60 line-clamp-2">{item.description}</p>
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="rounded-2xl border border-white/10 bg-white/5 p-6">
          <h2 className="text-lg font-semibold">상영 일정 등록</h2>
          <p className="mt-2 text-xs text-white/60">
            영상 길이를 입력하면 종료 시간이 자동으로 계산됩니다.
          </p>

          <div className="mt-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-white/80 mb-1">상영 시작 시간</label>
              <input
                type="datetime-local"
                value={startAt}
                onChange={(e) => setStartAt(e.target.value)}
                step={3600}
                className="w-full rounded-md border border-white/10 bg-white/5 px-4 py-2 text-white placeholder:text-white/40 focus:ring-red-500 focus:border-red-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-white/80 mb-1">영상 길이(분)</label>
              <input
                type="number"
                min={1}
                value={durationMinutes}
                onChange={(e) => setDurationMinutes(Number(e.target.value))}
                className="w-full rounded-md border border-white/10 bg-white/5 px-4 py-2 text-white placeholder:text-white/40 focus:ring-red-500 focus:border-red-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-white/80 mb-1">상영 종료 시간</label>
              <input
                type="datetime-local"
                value={endAt}
                readOnly
                className="w-full rounded-md border border-white/10 bg-white/5 px-4 py-2 text-white/70"
              />
            </div>
          </div>

          {error && (
            <div className="mt-4 rounded-md border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
              {error}
            </div>
          )}
          {notice && (
            <div className="mt-4 rounded-md border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-200">
              {notice}
            </div>
          )}

          <button
            type="button"
            disabled={saving}
            onClick={handleCreateSchedule}
            className="mt-6 w-full rounded-md bg-red-600 py-3 text-sm font-semibold text-white hover:bg-red-500 disabled:bg-white/20 transition-colors"
          >
            {saving ? '저장 중...' : '상영 일정 저장'}
          </button>
        </div>
      </div>
    </div>
    </SubscriptionGate>
  );
}


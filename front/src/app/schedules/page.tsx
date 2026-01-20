'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import type { ApiResponse, PageResponse } from '@/types';
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

const pad = (value: number) => value.toString().padStart(2, '0');
const toDateKey = (date: Date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
const toLocalInput = (date: Date) =>
  `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(
    date.getMinutes()
  )}`;

export default function ScheduleCalendarPage() {
  const router = useRouter();
  const { user, hasHydrated } = useAuthStore();

  const [monthAnchor, setMonthAnchor] = useState(() => {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1);
  });
  const [items, setItems] = useState<ScheduleSearchResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState<ScheduleSearchResponse | null>(null);
  const [editStartAt, setEditStartAt] = useState('');
  const [editEndAt, setEditEndAt] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!hasHydrated) return;
    if (!user) {
      router.push('/login?redirect=/schedules');
      return;
    }
  }, [hasHydrated, router, user]);

  useEffect(() => {
    if (!hasHydrated || !user) return;
    const fetchSchedules = async () => {
      try {
        setLoading(true);
        setError('');
        const { data } = await api.get<ApiResponse<PageResponse<ScheduleSearchResponse>>>('/schedules', {
          params: { page: 0, size: 500, nickname: user.nickname },
        });
        setItems(data.data?.content ?? []);
      } catch (err: any) {
        setError(err.response?.data?.message || '상영 일정을 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };
    fetchSchedules();
  }, [hasHydrated, user]);

  const monthDays = useMemo(() => {
    const firstDay = new Date(monthAnchor.getFullYear(), monthAnchor.getMonth(), 1);
    const startOfGrid = new Date(firstDay);
    startOfGrid.setDate(firstDay.getDate() - firstDay.getDay());
    const days: Date[] = [];
    for (let i = 0; i < 42; i += 1) {
      const day = new Date(startOfGrid);
      day.setDate(startOfGrid.getDate() + i);
      days.push(day);
    }
    return days;
  }, [monthAnchor]);

  const grouped = useMemo(() => {
    const map = new Map<string, ScheduleSearchResponse[]>();
    items.forEach((item) => {
      const dateKey = toDateKey(new Date(item.startAt));
      const list = map.get(dateKey) ?? [];
      list.push(item);
      map.set(dateKey, list);
    });
    return map;
  }, [items]);

  const monthLabel = `${monthAnchor.getFullYear()}년 ${monthAnchor.getMonth() + 1}월`;

  const openEdit = (item: ScheduleSearchResponse) => {
    setEditing(item);
    setEditStartAt(toLocalInput(new Date(item.startAt)));
    setEditEndAt(toLocalInput(new Date(item.endAt)));
  };

  const closeEdit = () => {
    setEditing(null);
    setEditStartAt('');
    setEditEndAt('');
  };

  const handleUpdate = async () => {
    if (!editing) return;
    try {
      setSaving(true);
      await api.put(`/schedules/${editing.scheduleItemId}`, {
        startAt: editStartAt,
        endAt: editEndAt,
      });
      setItems((prev) =>
        prev.map((item) =>
          item.scheduleItemId === editing.scheduleItemId
            ? { ...item, startAt: editStartAt, endAt: editEndAt }
            : item
        )
      );
      closeEdit();
    } catch (err: any) {
      setError(err.response?.data?.message || '일정 수정에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (item: ScheduleSearchResponse) => {
    if (!window.confirm('이 상영 일정을 삭제할까요?')) return;
    try {
      setSaving(true);
      await api.delete(`/schedules/${item.scheduleItemId}`);
      setItems((prev) => prev.filter((schedule) => schedule.scheduleItemId !== item.scheduleItemId));
    } catch (err: any) {
      setError(err.response?.data?.message || '일정 삭제에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="mx-auto max-w-6xl px-6 py-12 text-white">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <SectionHeader
          title="상영 일정 캘린더"
          subtitle="캘린더에서 상영 시간을 수정/삭제하세요."
          className="max-w-xl"
        />
        <div className="flex items-center gap-2">
          <Button
            size="sm"
            variant="secondary"
            onClick={() => setMonthAnchor(new Date(monthAnchor.getFullYear(), monthAnchor.getMonth() - 1, 1))}
          >
            이전달
          </Button>
          <span className="text-sm font-semibold">{monthLabel}</span>
          <Button
            size="sm"
            variant="secondary"
            onClick={() => setMonthAnchor(new Date(monthAnchor.getFullYear(), monthAnchor.getMonth() + 1, 1))}
          >
            다음달
          </Button>
        </div>
      </div>

      {loading && (
        <div className="mt-6 rounded-lg border border-white/10 bg-white/5 p-4 text-sm text-white/60">
          일정을 불러오는 중입니다...
        </div>
      )}
      {error && (
        <div className="mt-6 rounded-lg border border-red-500/30 bg-red-500/10 p-4 text-sm text-red-200">
          {error}
        </div>
      )}

      {!loading && !error && (
        <div className="mt-6 grid grid-cols-7 gap-2">
          {['일', '월', '화', '수', '목', '금', '토'].map((label) => (
            <div key={label} className="text-center text-xs text-white/60">
              {label}
            </div>
          ))}
          {monthDays.map((day) => {
            const dateKey = toDateKey(day);
            const list = grouped.get(dateKey) ?? [];
            const isCurrentMonth = day.getMonth() === monthAnchor.getMonth();
            return (
              <Card
                key={dateKey}
                className={`min-h-[120px] p-2 text-xs ${isCurrentMonth ? 'text-white' : 'text-white/40'}`}
                hover={false}
              >
                <div className="text-right text-xs">{day.getDate()}</div>
                <div className="mt-2 space-y-1">
                  {list.map((item) => (
                    <Card key={item.scheduleItemId} className="bg-black/40 p-2" hover>
                      <div className="text-[11px] text-white/70">
                        {new Date(item.startAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                        {' ~ '}
                        {new Date(item.endAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                      </div>
                      <div className="mt-1 font-semibold">{item.contentTitle}</div>
                      <div className="mt-1 flex gap-1">
                        <Button
                          size="sm"
                          variant="secondary"
                          onClick={() => openEdit(item)}
                          className="px-2 py-0.5 text-[10px]"
                        >
                          수정
                        </Button>
                        <Button
                          size="sm"
                          variant="secondary"
                          onClick={() => handleDelete(item)}
                          className="border-red-500/40 text-red-200 px-2 py-0.5 text-[10px]"
                        >
                          삭제
                        </Button>
                      </div>
                    </Card>
                  ))}
                  {list.length === 0 && (
                    <div className="text-[10px] text-white/40">일정 없음</div>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {editing && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 px-4">
          <div className="w-full max-w-lg rounded-xl border border-white/10 bg-black p-6 text-white card">
            <h2 className="text-lg font-semibold section-title">일정 수정</h2>
            <div className="mt-4 space-y-3">
              <label className="block text-xs text-white/60">시작 시간</label>
              <input
                type="datetime-local"
                value={editStartAt}
                onChange={(e) => setEditStartAt(e.target.value)}
                className="w-full rounded-md border border-white/10 bg-white/5 px-3 py-2 text-sm text-white"
              />
              <label className="block text-xs text-white/60">종료 시간</label>
              <input
                type="datetime-local"
                value={editEndAt}
                onChange={(e) => setEditEndAt(e.target.value)}
                className="w-full rounded-md border border-white/10 bg-white/5 px-3 py-2 text-sm text-white"
              />
            </div>
            <div className="mt-6 flex gap-2">
              <Button
                variant="primary"
                onClick={handleUpdate}
                disabled={saving}
                className="flex-1 py-2"
              >
                저장
              </Button>
              <Button
                variant="secondary"
                onClick={closeEdit}
                className="flex-1 py-2"
              >
                취소
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

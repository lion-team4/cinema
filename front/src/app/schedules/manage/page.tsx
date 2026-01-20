'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import { type ApiResponse, type ContentSearchResponse, type PageResponse } from '@/types';
import SubscriptionGate from '@/components/guards/SubscriptionGate';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import SectionHeader from '@/components/ui/SectionHeader';

type ScheduleSearchResponse = {
  scheduleItemId: number;
  contentId: number;
  contentTitle: string;
  creatorNickname: string;
  startAt: string;
  endAt: string;
  status: 'WAITING' | 'PLAYING' | 'ENDING' | 'CLOSED';
  isLocked: boolean;
  scheduleDayId?: number | null;
};

type EncodingStatusResponse = {
  contentId: number;
  encodingStatus: 'ENCODING' | 'READY' | 'FAILED' | null;
  encodingError: string | null;
};

const statusLabel: Record<ContentSearchResponse['status'], string> = {
  PUBLISHED: '공개',
  DRAFT: '임시 저장',
  PRIVATE: '비공개',
};

const statusColor: Record<ContentSearchResponse['status'], string> = {
  PUBLISHED: 'bg-emerald-500/20 text-emerald-200',
  DRAFT: 'bg-amber-500/20 text-amber-200',
  PRIVATE: 'bg-slate-500/20 text-slate-200',
};

const formatLocalDateTime = (date: Date) => {
  const pad = (value: number) => value.toString().padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}`;
};

const pad = (value: number) => value.toString().padStart(2, '0');
const toDateKey = (date: Date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;

export default function ScheduleManagePage() {
  const router = useRouter();
  const { user, hasHydrated } = useAuthStore();
  const [items, setItems] = useState<ContentSearchResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState<number | null>(null);
  const [startAt, setStartAt] = useState('');
  const [durationMinutes, setDurationMinutes] = useState<number>(90);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState('');
  const [encodingStatusMap, setEncodingStatusMap] = useState<Record<number, EncodingStatusResponse['encodingStatus']>>({});
  const startAtInputRef = useRef<HTMLInputElement | null>(null);
  const [schedules, setSchedules] = useState<ScheduleSearchResponse[]>([]);
  const [scheduleLoading, setScheduleLoading] = useState(false);
  const [scheduleError, setScheduleError] = useState('');
  const [lockingDayId, setLockingDayId] = useState<number | null>(null);
  const [editingScheduleId, setEditingScheduleId] = useState<number | null>(null);
  const [editingStartAt, setEditingStartAt] = useState('');
  const [editingEndAt, setEditingEndAt] = useState('');
  const [showCalendar, setShowCalendar] = useState(false);
  const [calendarMonth, setCalendarMonth] = useState(() => {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1);
  });
  const [selectedDate, setSelectedDate] = useState(() => toDateKey(new Date()));

  const endAt = useMemo(() => {
    if (!startAt || !durationMinutes) return '';
    const startDate = new Date(startAt);
    if (Number.isNaN(startDate.getTime())) return '';
    const endDate = new Date(startDate.getTime() + durationMinutes * 60 * 1000);
    return formatLocalDateTime(endDate);
  }, [durationMinutes, startAt]);

  const toMinutes = (durationMs?: number | null) => {
    if (!durationMs || durationMs <= 0) return null;
    return Math.max(1, Math.ceil(durationMs / (60 * 1000)));
  };


  const monthDays = useMemo(() => {
    const firstDay = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1);
    const startOfGrid = new Date(firstDay);
    startOfGrid.setDate(firstDay.getDate() - firstDay.getDay());
    const days: Date[] = [];
    for (let i = 0; i < 42; i += 1) {
      const day = new Date(startOfGrid);
      day.setDate(startOfGrid.getDate() + i);
      days.push(day);
    }
    return days;
  }, [calendarMonth]);

  const scheduleByDate = useMemo(() => {
    const map = new Map<string, ScheduleSearchResponse[]>();
    schedules.forEach((item) => {
      const key = toDateKey(new Date(item.startAt));
      const list = map.get(key) ?? [];
      list.push(item);
      map.set(key, list);
    });
    return map;
  }, [schedules]);

  const selectedSchedules = scheduleByDate.get(selectedDate) ?? [];
  const sortedSelectedSchedules = useMemo(() => {
    return [...selectedSchedules].sort((a, b) => {
      const titleCompare = a.contentTitle.localeCompare(b.contentTitle);
      if (titleCompare !== 0) return titleCompare;
      return new Date(a.startAt).getTime() - new Date(b.startAt).getTime();
    });
  }, [selectedSchedules]);

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
  }, [hasHydrated, router, user]);

  const handleDeleteContent = async (contentId: number, title: string) => {
    if (!window.confirm(`"${title}" 콘텐츠를 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`)) {
      return;
    }
    try {
      setDeleting(contentId);
      await api.delete(`/contents/${contentId}`);
      const nextItems = items.filter((item) => item.contentId !== contentId);
      setItems(nextItems);
      if (selectedId === contentId) {
        setSelectedId(nextItems[0]?.contentId ?? null);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || '콘텐츠 삭제에 실패했습니다.');
    } finally {
      setDeleting(null);
    }
  };

  useEffect(() => {
    if (!selectedId) return;
    const selected = items.find((item) => item.contentId === selectedId);
    const minutes = toMinutes(selected?.durationMs);
    if (minutes) {
      setDurationMinutes(minutes);
    }
  }, [items, selectedId]);

  useEffect(() => {
    if (!user || items.length === 0) return;
    let active = true;
    const loadEncodingStatuses = async () => {
      const entries = await Promise.all(
        items.map(async (item) => {
          try {
            const { data } = await api.get<ApiResponse<EncodingStatusResponse>>(`/contents/${item.contentId}/encoding-status`);
            return [item.contentId, data.data?.encodingStatus ?? null] as const;
          } catch {
            return [item.contentId, null] as const;
          }
        })
      );
      if (!active) return;
      setEncodingStatusMap(Object.fromEntries(entries));
    };
    loadEncodingStatuses();
    return () => {
      active = false;
    };
  }, [items, user]);

  const fetchSchedules = async () => {
    if (!user) return;
    try {
      setScheduleLoading(true);
      setScheduleError('');
      const { data } = await api.get<ApiResponse<PageResponse<ScheduleSearchResponse>>>('/schedules', {
        params: { page: 0, size: 200, nickname: user.nickname, lockedOnly: false },
      });
      const list = data.data?.content ?? [];
      setSchedules(list);
    } catch (err: any) {
      setScheduleError(err.response?.data?.message || '상영 일정 목록을 불러오지 못했습니다.');
      setSchedules([]);
    } finally {
      setScheduleLoading(false);
    }
  };

  useEffect(() => {
    if (!user) return;
    fetchSchedules();
  }, [user]);

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
      const { data } = await api.post<ApiResponse<{ scheduleItemId: number; scheduleDayId: number }>>('/schedules', {
        contentId: selectedId,
        scheduleDate,
        startAt,
        endAt,
      });
      setNotice('상영 일정이 등록되었습니다.');
      await fetchSchedules();
    } catch (err: any) {
      setError(err.response?.data?.message || '상영 일정 등록에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const selectedEncodingStatus = selectedId ? encodingStatusMap[selectedId] ?? null : null;
  const isEncodingReady = selectedEncodingStatus === 'READY';

  const handleEditSchedule = (schedule: ScheduleSearchResponse) => {
    setEditingScheduleId(schedule.scheduleItemId);
    setEditingStartAt(formatLocalDateTime(new Date(schedule.startAt)));
    setEditingEndAt(formatLocalDateTime(new Date(schedule.endAt)));
  };

  const handleUpdateSchedule = async () => {
    if (!editingScheduleId) return;
    try {
      setScheduleLoading(true);
      await api.put(`/schedules/${editingScheduleId}`, {
        startAt: editingStartAt,
        endAt: editingEndAt,
      });
      setEditingScheduleId(null);
      await fetchSchedules();
    } catch (err: any) {
      setScheduleError(err.response?.data?.message || '상영 일정 수정에 실패했습니다.');
    } finally {
      setScheduleLoading(false);
    }
  };

  const handleDeleteSchedule = async (scheduleItemId: number) => {
    if (!window.confirm('해당 상영 일정을 삭제할까요?')) return;
    try {
      setScheduleLoading(true);
      await api.delete(`/schedules/${scheduleItemId}`);
      await fetchSchedules();
    } catch (err: any) {
      setScheduleError(err.response?.data?.message || '상영 일정 삭제에 실패했습니다.');
    } finally {
      setScheduleLoading(false);
    }
  };

  const handleConfirmDay = async (scheduleDayId: number | null | undefined) => {
    if (!scheduleDayId) {
      setScheduleError('scheduleDayId를 찾을 수 없어 확정할 수 없습니다.');
      return;
    }
    try {
      setLockingDayId(scheduleDayId);
      setScheduleError('');
      await api.put(`/schedules/${scheduleDayId}/confirm`, { isLock: true });
      setSchedules((prev) => prev.map((item) => (
        item.scheduleDayId === scheduleDayId ? { ...item, isLocked: true } : item
      )));
    } catch (err: any) {
      setScheduleError(err.response?.data?.message || '상영 확정에 실패했습니다.');
    } finally {
      setLockingDayId(null);
    }
  };

  return (
    <SubscriptionGate>
      <div className="mx-auto max-w-6xl px-6 py-12 text-white">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <SectionHeader
            title="감독 스튜디오"
            subtitle="내 콘텐츠 관리와 상영 일정 편성을 한 곳에서 처리합니다."
          />
          <Button
            variant="primary"
            className="px-6 py-3 text-base"
            onClick={() => router.push('/contents/create')}
          >
            새 영상 업로드
          </Button>
        </div>

      <Card className="mt-8 p-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-semibold section-title">내 영상 관리</h2>
            <p className="mt-1 text-sm text-white/60 section-subtitle">
              업로드된 콘텐츠를 관리하고 상영 편성을 준비하세요.
            </p>
          </div>
          <div className="flex items-center gap-2">
            <Button size="sm" variant="secondary" onClick={() => setShowCalendar((prev) => !prev)}>
              {showCalendar ? '내 영상 보기' : '캘린더 보기'}
            </Button>
          </div>
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

        {!loading && !showCalendar && items.length > 0 && (
          <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {items.map((item) => {
              const isSelected = selectedId === item.contentId;
              return (
                <Card
                  key={item.contentId}
                  hover
                  className={`hover-soft ${isSelected ? 'border-red-500/80 bg-red-500/15 ring-2 ring-red-500/50 shadow-[0_0_0_1px_rgba(239,68,68,0.45)]' : ''}`}
                >
                  <div className="flex items-center justify-between">
                    <Badge className={statusColor[item.status]}>{statusLabel[item.status]}</Badge>
                    <span className="text-xs text-white/50">조회수 {item.totalView}</span>
                  </div>
                  <div className="mt-3 flex items-center gap-3">
                    <div className="h-12 w-20 overflow-hidden rounded-md border border-white/10 bg-black/40">
                      {item.posterImage ? (
                        <img src={item.posterImage} alt={item.title} className="h-full w-full object-cover" />
                      ) : (
                        <div className="flex h-full w-full items-center justify-center text-[10px] text-white/40">
                          포스터 없음
                        </div>
                      )}
                    </div>
                    <div className="min-w-0">
                      <h3 className="text-base font-semibold truncate">{item.title}</h3>
                      <p className="mt-1 text-xs text-white/60 line-clamp-2">{item.description}</p>
                    </div>
                  </div>
                  <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-white/50">
                    <span>길이 {item.durationMs ? `${Math.ceil(item.durationMs / 60000)}분` : '-'}</span>
                    <span>작성자 {item.ownerNickname}</span>
                    {isSelected && <Badge className="bg-red-500/20 text-red-100">편성 선택됨</Badge>}
                  </div>
                  <div className="mt-4 flex flex-wrap items-center gap-2">
                    <Button size="sm" variant="secondary" onClick={() => setSelectedId(item.contentId)}>
                      {isSelected ? '편성 선택됨' : '편성 선택'}
                    </Button>
                    <Button size="sm" variant="secondary" onClick={() => router.push(`/studio/contents/${item.contentId}/edit`)}>
                      수정
                    </Button>
                    <Button
                      size="sm"
                      variant="secondary"
                      onClick={() => handleDeleteContent(item.contentId, item.title)}
                      disabled={deleting === item.contentId}
                      className="border-red-500/40 text-red-200 hover:border-red-400"
                    >
                      {deleting === item.contentId ? '삭제 중...' : '삭제'}
                    </Button>
                  </div>
                </Card>
              );
            })}
          </div>
        )}

        {!loading && showCalendar && (
          <div className="mt-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="text-sm text-white/60">내 상영 일정 캘린더</div>
              <div className="flex items-center gap-2">
                <Button size="sm" variant="secondary" onClick={() => setCalendarMonth(new Date(calendarMonth.getFullYear(), calendarMonth.getMonth() - 1, 1))}>
                  이전달
                </Button>
                <span className="text-sm font-semibold">
                  {calendarMonth.getFullYear()}년 {calendarMonth.getMonth() + 1}월
                </span>
                <Button size="sm" variant="secondary" onClick={() => setCalendarMonth(new Date(calendarMonth.getFullYear(), calendarMonth.getMonth() + 1, 1))}>
                  다음달
                </Button>
              </div>
            </div>
            <div className="mt-4 grid grid-cols-7 gap-2 text-xs text-white/60">
              {['일', '월', '화', '수', '목', '금', '토'].map((label) => (
                <div key={label} className="text-center">{label}</div>
              ))}
            </div>
            <div className="mt-2 grid grid-cols-7 gap-2">
              {monthDays.map((day) => {
                const dateKey = toDateKey(day);
                const list = scheduleByDate.get(dateKey) ?? [];
                const isCurrentMonth = day.getMonth() === calendarMonth.getMonth();
                const isSelected = dateKey === selectedDate;
                return (
                  <Card
                    key={dateKey}
                    variant="compact"
                    className={`min-h-[120px] cursor-pointer hover-soft ${isCurrentMonth ? 'text-white' : 'text-white/40'} ${isSelected ? 'border-red-500/70' : ''}`}
                    role="button"
                    tabIndex={0}
                    onClick={() => {
                      setSelectedDate(dateKey);
                      setCalendarMonth(new Date(day.getFullYear(), day.getMonth(), 1));
                    }}
                    onKeyDown={(event) => {
                      if (event.key === 'Enter' || event.key === ' ') {
                        setSelectedDate(dateKey);
                        setCalendarMonth(new Date(day.getFullYear(), day.getMonth(), 1));
                      }
                    }}
                  >
                    <div className="text-right text-xs">{day.getDate()}</div>
                    <div className="mt-2 space-y-1">
                      {list.slice(0, 3).map((item) => (
                        <div key={item.scheduleItemId} className="rounded-md border border-white/10 bg-black/40 px-2 py-1 text-[10px] text-white/70">
                          {new Date(item.startAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })} · {item.contentTitle}
                        </div>
                      ))}
                      {list.length === 0 && <div className="text-[10px] text-white/40">일정 없음</div>}
                      {list.length > 3 && <div className="text-[10px] text-white/40">+{list.length - 3}개 더</div>}
                    </div>
                  </Card>
                );
              })}
            </div>
          </div>
        )}
      </Card>

      <div className="mt-8 grid gap-6 lg:grid-cols-[2fr_1fr]">
        <Card className="p-6">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold section-title">편성할 콘텐츠 선택</h2>
            <Badge>{items.length}개</Badge>
          </div>

          {!loading && items.length === 0 && (
            <p className="mt-4 text-sm text-white/60">편성할 콘텐츠가 없습니다.</p>
          )}

          {!loading && items.length > 0 && (
            <div className="mt-6 grid gap-4 sm:grid-cols-2">
              {items.map((item) => {
                const isSelected = selectedId === item.contentId;
                const encodingStatus = encodingStatusMap[item.contentId] ?? null;
                const encodingLabel =
                  encodingStatus === 'READY'
                    ? '인코딩 완료'
                    : encodingStatus === 'FAILED'
                      ? '인코딩 실패'
                      : encodingStatus === 'ENCODING'
                        ? '인코딩 중'
                        : '상태 확인 중';
                const encodingTone =
                  encodingStatus === 'READY'
                    ? 'success'
                    : encodingStatus === 'FAILED'
                      ? 'danger'
                      : 'warning';
                return (
                  <Card
                    key={item.contentId}
                    variant="compact"
                    className={`text-left cursor-pointer hover-soft ${
                      isSelected
                        ? 'border-red-500/80 bg-red-500/15 ring-2 ring-red-500/50 shadow-[0_0_0_1px_rgba(239,68,68,0.45)]'
                        : ''
                    }`}
                    role="button"
                    tabIndex={0}
                    onClick={() => setSelectedId(item.contentId)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') setSelectedId(item.contentId);
                    }}
                  >
                    <div className="flex items-center justify-between">
                      <h3 className="text-sm font-semibold">{item.title}</h3>
                      {isSelected && <Badge className="bg-red-500/20 text-red-100">선택됨</Badge>}
                    </div>
                    <div className="mt-2">
                      <Badge tone={encodingTone}>{encodingLabel}</Badge>
                    </div>
                    <p className="mt-2 text-xs text-white/60 line-clamp-2">{item.description}</p>
                  </Card>
                );
              })}
            </div>
          )}
        </Card>

        <Card className="p-6">
          <h2 className="text-lg font-semibold section-title">상영 일정 등록</h2>
          <p className="mt-2 text-xs text-white/60">
            영상 길이를 입력하면 종료 시간이 자동으로 계산됩니다.
          </p>

          <div className="mt-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-white/80 mb-1">상영 시작 시간</label>
              <div className="flex items-center gap-2">
                <input
                  ref={startAtInputRef}
                  type="datetime-local"
                  value={startAt}
                  onChange={(e) => setStartAt(e.target.value)}
                  step={60}
                  className="w-full rounded-md border border-white/10 bg-white/5 px-4 py-2 text-white placeholder:text-white/40 focus:ring-red-500 focus:border-red-500"
                />
                <Button
                  size="sm"
                  variant="primary"
                  onClick={() => {
                    if (startAtInputRef.current?.showPicker) {
                      startAtInputRef.current.showPicker();
                    } else {
                      startAtInputRef.current?.focus();
                    }
                  }}
                >
                  날짜 선택
                </Button>
              </div>
              <p className="mt-1 text-xs text-white/50">예: 2025-02-01 19:00</p>
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
            <p className="text-xs text-white/50">
              상영 일정 저장 시 자동으로 편성 확정됩니다.
            </p>
            {selectedId && selectedEncodingStatus !== 'READY' && (
              <div className="rounded-md border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-xs text-amber-200">
                인코딩이 완료된 콘텐츠만 상영 일정을 등록할 수 있습니다.
              </div>
            )}
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

          <Button
            variant="primary"
            onClick={handleCreateSchedule}
            disabled={saving || !isEncodingReady}
            className="mt-6 w-full py-3"
          >
            {saving ? '저장 중...' : '상영 일정 저장'}
          </Button>
        </Card>
      </div>

      <Card className="mt-10 p-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-semibold section-title">상영 일정 목록</h2>
            <p className="mt-2 text-xs text-white/60">{selectedDate} 기준</p>
          </div>
          <div className="flex items-center gap-2">
            <Button
              size="sm"
              variant="secondary"
              onClick={() => {
                const current = new Date(selectedDate);
                current.setDate(current.getDate() - 1);
                setSelectedDate(toDateKey(current));
              }}
            >
              ‹
            </Button>
            <Button
              size="sm"
              variant="secondary"
              onClick={() => setSelectedDate(toDateKey(new Date()))}
            >
              오늘
            </Button>
            <Button
              size="sm"
              variant="secondary"
              onClick={() => {
                const current = new Date(selectedDate);
                current.setDate(current.getDate() + 1);
                setSelectedDate(toDateKey(current));
              }}
            >
              ›
            </Button>
          </div>
        </div>

        {scheduleLoading && (
          <div className="mt-4 rounded-lg border border-white/10 bg-white/5 p-4 text-sm text-white/60">
            상영 일정을 불러오는 중입니다...
          </div>
        )}
        {!scheduleLoading && scheduleError && (
          <div className="mt-4 rounded-lg border border-red-500/30 bg-red-500/10 p-4 text-sm text-red-200">
            {scheduleError}
          </div>
        )}

        {!scheduleLoading && !scheduleError && (
          <div className="mt-4 space-y-4">
            {selectedSchedules.length === 0 && (
              <div className="rounded-lg border border-white/10 bg-white/5 p-4 text-sm text-white/60">
                선택한 날짜에 상영 일정이 없습니다.
              </div>
            )}
            {sortedSelectedSchedules.map((item) => (
              <Card key={item.scheduleItemId} className="p-4" variant="compact">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <div className="text-xs text-white/60">
                      {new Date(item.startAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                      {' ~ '}
                      {new Date(item.endAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                    </div>
                    <h3 className="mt-1 text-base font-semibold">{item.contentTitle}</h3>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge tone={item.isLocked ? 'success' : 'default'}>
                      {item.isLocked ? '확정됨' : '미확정'}
                    </Badge>
                    <Button
                      size="sm"
                      variant="secondary"
                      onClick={() => handleConfirmDay(item.scheduleDayId)}
                      disabled={item.isLocked || lockingDayId === item.scheduleDayId}
                    >
                      {lockingDayId === item.scheduleDayId ? '확정 중...' : '해당 날짜 확정'}
                    </Button>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </Card>
    </div>
    </SubscriptionGate>
  );
}


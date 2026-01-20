'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import axios from 'axios';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import type { ApiResponse, ContentSearchResponse, PageResponse } from '@/types';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';

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

type UserProfileLite = {
  nickname: string;
  profileImageUrl: string | null;
};

const pad = (value: number) => value.toString().padStart(2, '0');
const toDateKey = (date: Date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;

const normalizeUrl = (value: string | null) => {
  if (!value) return null;
  if (value.startsWith('https://https://')) return value.replace('https://https://', 'https://');
  if (value.startsWith('http://http://')) return value.replace('http://http://', 'http://');
  if (value.startsWith('http://') || value.startsWith('https://')) return value;
  return `https://${value}`;
};

export default function FilmographyPage() {
  const router = useRouter();
  const params = useParams();
  const nicknameParam = decodeURIComponent(String(params?.nickname ?? ''));
  const { user, setUser } = useAuthStore();
  const isOwner = user?.nickname === nicknameParam;
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const [profile, setProfile] = useState<UserProfileLite | null>(null);
  const [profileImageOverride, setProfileImageOverride] = useState<string | null>(null);
  const [profileUploading, setProfileUploading] = useState(false);
  const [profileError, setProfileError] = useState('');

  const [contents, setContents] = useState<ContentSearchResponse[]>([]);
  const [contentsLoading, setContentsLoading] = useState(true);
  const [contentsError, setContentsError] = useState('');

  const [schedules, setSchedules] = useState<ScheduleSearchResponse[]>([]);
  const [scheduleLoading, setScheduleLoading] = useState(true);
  const [scheduleError, setScheduleError] = useState('');
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState('');
  const [detail, setDetail] = useState<{
    contentId: number;
    title: string;
    description: string;
    ownerNickname: string;
    posterUrl: string | null;
    durationMs: number | null;
    tags: string[];
  } | null>(null);

  const [monthAnchor, setMonthAnchor] = useState(() => {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1);
  });
  const [selectedDate, setSelectedDate] = useState(() => toDateKey(new Date()));

  useEffect(() => {
    const loadProfile = async () => {
      if (!nicknameParam) return;
      if (isOwner && user) {
        setProfile({ nickname: user.nickname, profileImageUrl: user.profileImage ?? null });
        return;
      }
      try {
        const { data } = await api.get<ApiResponse<{ nickname: string; profileImage: string | null }>>(
          `/users/search/${encodeURIComponent(nicknameParam)}/info`
        );
        const payload = data.data;
        if (payload) {
          setProfile({
            nickname: payload.nickname,
            profileImageUrl: payload.profileImage ?? null,
          });
        } else {
          setProfile({ nickname: nicknameParam, profileImageUrl: null });
        }
      } catch {
        setProfile({ nickname: nicknameParam, profileImageUrl: null });
      }
    };
    loadProfile();
  }, [isOwner, nicknameParam, user]);

  useEffect(() => {
    const fetchContents = async () => {
      try {
        setContentsLoading(true);
        setContentsError('');
        const { data } = await api.get<ApiResponse<PageResponse<ContentSearchResponse>>>('/contents', {
          params: { page: 0, size: 100, nickname: nicknameParam },
        });
        const list = data.data?.content ?? [];
        setContents(list);
      } catch (err: any) {
        setContentsError(err.response?.data?.message || '콘텐츠를 불러오지 못했습니다.');
      } finally {
        setContentsLoading(false);
      }
    };
    if (nicknameParam) fetchContents();
  }, [nicknameParam]);

  useEffect(() => {
    const fetchSchedules = async () => {
      try {
        setScheduleLoading(true);
        setScheduleError('');
        const { data } = await api.get<ApiResponse<PageResponse<ScheduleSearchResponse>>>('/schedules', {
          params: { page: 0, size: 300 },
        });
        const list = data.data?.content ?? [];
        setSchedules(list.filter((item) => item.creatorNickname === nicknameParam));
      } catch (err: any) {
        setScheduleError(err.response?.data?.message || '상영 일정을 불러오지 못했습니다.');
      } finally {
        setScheduleLoading(false);
      }
    };
    if (nicknameParam) fetchSchedules();
  }, [nicknameParam]);

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

  const schedulesByDate = useMemo(() => {
    const map = new Map<string, ScheduleSearchResponse[]>();
    schedules.forEach((item) => {
      const dateKey = toDateKey(new Date(item.startAt));
      const list = map.get(dateKey) ?? [];
      list.push(item);
      map.set(dateKey, list);
    });
    return map;
  }, [schedules]);

  const selectedSchedules = schedulesByDate.get(selectedDate) ?? [];
  const ownerProfileImage = normalizeUrl(profileImageOverride || user?.profileImage || null);
  const profileImage = isOwner
    ? ownerProfileImage
    : normalizeUrl(profile?.profileImageUrl || null);

  const handleProfileImageChange = async (file: File) => {
    if (!user) return;
    setProfileUploading(true);
    setProfileError('');
    try {
      const presign = await api.post<ApiResponse<{ uploadUrl: string; objectKey: string }>>('/api/assets/presign', {
        fileName: file.name,
        contentType: file.type,
        assetType: 'PROFILE_IMAGE',
        ownerUserId: user.userId,
      });
      const { uploadUrl, objectKey } = presign.data.data;
      await axios.put(uploadUrl, file, { headers: { 'Content-Type': file.type } });
      const complete = await api.post<ApiResponse<{ cdnUrl: string | null; assetId: number | null }>>('/api/assets/complete', {
        assetType: 'PROFILE_IMAGE',
        ownerUserId: user.userId,
        objectKey,
        contentType: file.type,
      });
      const cdnUrl = complete.data.data?.cdnUrl ?? null;
      const assetId = complete.data.data?.assetId ?? null;
      setProfileImageOverride(cdnUrl);
      if (assetId) {
        await api.patch('/users/me', {
          profileImageAssetId: assetId,
        });
        const refreshed = await api.get<ApiResponse<{ userId: number; email: string; nickname: string; profileImageUrl: string | null; seller: boolean }>>(
          '/users/me'
        );
        const profile = refreshed.data.data;
        if (profile) {
          setUser({
            userId: profile.userId,
            email: profile.email,
            nickname: profile.nickname,
            profileImage: profile.profileImageUrl ?? null,
            seller: profile.seller,
          });
        }
      } else if (cdnUrl) {
        setUser({ ...user, profileImage: cdnUrl });
      }
    } catch (err: any) {
      setProfileError(err.response?.data?.message || '프로필 이미지 업로드에 실패했습니다.');
    } finally {
      setProfileUploading(false);
    }
  };

  const handleDateMove = (direction: 'prev' | 'next') => {
    const current = new Date(selectedDate);
    current.setDate(current.getDate() + (direction === 'prev' ? -1 : 1));
    const nextKey = toDateKey(current);
    setSelectedDate(nextKey);
    if (current.getMonth() !== monthAnchor.getMonth() || current.getFullYear() !== monthAnchor.getFullYear()) {
      setMonthAnchor(new Date(current.getFullYear(), current.getMonth(), 1));
    }
  };

  const openDetail = async (contentId: number) => {
    try {
      setDetailOpen(true);
      setDetailLoading(true);
      setDetailError('');
      const { data } = await api.get<ApiResponse<{
        contentId: number;
        title: string;
        description: string;
        ownerNickname: string;
        posterUrl: string | null;
        durationMs: number | null;
        tags: string[];
      }>>(`/contents/${contentId}`);
      const payload = data.data;
      setDetail(
        payload
          ? {
              contentId: payload.contentId,
              title: payload.title,
              description: payload.description,
              ownerNickname: payload.ownerNickname,
              posterUrl: normalizeUrl(payload.posterUrl),
              durationMs: payload.durationMs,
              tags: payload.tags ?? [],
            }
          : null
      );
    } catch (err: any) {
      setDetailError(err.response?.data?.message || '상세 정보를 불러오지 못했습니다.');
    } finally {
      setDetailLoading(false);
    }
  };

  const closeDetail = () => {
    setDetailOpen(false);
    setDetail(null);
    setDetailError('');
  };

  return (
    <div className="mx-auto w-full max-w-screen-2xl px-4 sm:px-6 lg:px-8 py-10 text-white overflow-x-hidden">
      <div className="relative h-44 w-full overflow-hidden rounded-2xl bg-black/90">
        {profileImage ? (
          <img src={profileImage} alt={profile?.nickname ?? 'profile'} className="h-full w-full object-cover" />
        ) : (
          <div className="h-full w-full" />
        )}
        <div className="absolute inset-0 flex items-center bg-gradient-to-r from-black/70 via-black/40 to-transparent px-6">
          <span className="brand-title text-2xl sm:text-3xl text-white/90 tracking-[0.18em] uppercase">
            {profile?.nickname ?? nicknameParam}'s Filmography
          </span>
        </div>
        {isOwner && (
          <div className="absolute right-4 top-4 flex items-center gap-2">
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={(event) => {
                const file = event.target.files?.[0];
                if (file) handleProfileImageChange(file);
              }}
            />
            <Button
              size="sm"
              variant="secondary"
              onClick={() => fileInputRef.current?.click()}
              disabled={profileUploading}
            >
              {profileUploading ? '업로드 중...' : '프로필 변경'}
            </Button>
          </div>
        )}
      </div>
      {profileError && (
        <div className="mt-3 rounded-md border border-red-500/30 bg-red-500/10 px-4 py-2 text-sm text-red-200">
          {profileError}
        </div>
      )}

      <div className="mt-8 grid gap-6 lg:grid-cols-[240px_minmax(0,1fr)] xl:grid-cols-[260px_minmax(0,1fr)]">
        <Card className="p-4 w-full" hover={false}>
          <div className="flex items-center justify-between">
            <span className="text-sm font-semibold">캘린더</span>
            <div className="flex items-center gap-1 text-xs">
              <Button
                size="sm"
                variant="secondary"
                onClick={() => setMonthAnchor(new Date(monthAnchor.getFullYear(), monthAnchor.getMonth() - 1, 1))}
              >
                ‹
              </Button>
              <Button
                size="sm"
                variant="secondary"
                onClick={() => setMonthAnchor(new Date(monthAnchor.getFullYear(), monthAnchor.getMonth() + 1, 1))}
              >
                ›
              </Button>
            </div>
          </div>
          <div className="mt-3 text-xs text-white/60">
            {monthAnchor.getFullYear()}년 {monthAnchor.getMonth() + 1}월
          </div>
          <div className="mt-3 grid grid-cols-7 gap-1 text-[11px] text-white/60">
            {['일', '월', '화', '수', '목', '금', '토'].map((label) => (
              <div key={label} className="text-center">
                {label}
              </div>
            ))}
          </div>
          <div className="mt-2 grid grid-cols-7 gap-1 text-xs">
            {monthDays.map((day) => {
              const dateKey = toDateKey(day);
              const hasSchedule = (schedulesByDate.get(dateKey) ?? []).length > 0;
              const isCurrentMonth = day.getMonth() === monthAnchor.getMonth();
              const isSelected = dateKey === selectedDate;
              return (
                <button
                  key={dateKey}
                  type="button"
                  onClick={() => setSelectedDate(dateKey)}
                  className={`rounded-md px-1.5 py-1 text-center transition-colors ${
                    isSelected ? 'bg-red-600 text-white' : 'hover:bg-white/10'
                  } ${isCurrentMonth ? 'text-white/80' : 'text-white/30'}`}
                >
                  {day.getDate()}
                  {hasSchedule && <span className="ml-1 text-[10px] text-emerald-300">•</span>}
                </button>
              );
            })}
          </div>
        </Card>

        <div className="space-y-6 w-full min-w-0">
          <Card className="p-5 w-full" hover={false}>
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold section-title">업로드한 영화</h3>
              <Badge>{contents.length}편</Badge>
            </div>
            {contentsLoading && (
              <div className="mt-4 rounded-lg border border-white/10 bg-white/5 p-4 text-sm text-white/60">
                콘텐츠를 불러오는 중입니다...
              </div>
            )}
            {!contentsLoading && contentsError && (
              <div className="mt-4 rounded-lg border border-red-500/30 bg-red-500/10 p-4 text-sm text-red-200">
                {contentsError}
              </div>
            )}
            {!contentsLoading && !contentsError && (
              <div className="mt-4 max-w-full overflow-x-scroll pb-2 scrollbar-theme scrollbar-always">
                <div className="grid h-[360px] sm:h-[400px] grid-rows-2 grid-flow-col auto-cols-[160px] sm:auto-cols-[200px] lg:auto-cols-[220px] xl:auto-cols-[240px] auto-rows-[160px] sm:auto-rows-[180px] gap-3 sm:gap-4 overflow-y-hidden">
                {contents.length === 0 && (
                  <div className="col-span-2 text-sm text-white/60">등록된 콘텐츠가 없습니다.</div>
                )}
                {contents.map((item) => (
                  <Card
                    key={item.contentId}
                    variant="compact"
                      className="cursor-pointer h-full hover-soft"
                      onClick={() => openDetail(item.contentId)}
                  >
                      <div className="flex h-full flex-col">
                        <div className="aspect-video w-full overflow-hidden rounded-lg border border-white/10 bg-black/40">
                          {item.posterImage ? (
                            <img src={item.posterImage} alt={item.title} className="h-full w-full object-cover" />
                          ) : (
                            <div className="flex h-full w-full items-center justify-center text-xs text-white/50">
                              포스터 없음
                            </div>
                          )}
                        </div>
                        <h4 className="mt-3 text-sm font-semibold">{item.title}</h4>
                        <p className="mt-1 text-xs text-white/50 line-clamp-2">{item.description}</p>
                    </div>
                  </Card>
                ))}
                </div>
              </div>
            )}
          </Card>

          <Card className="p-5 w-full" hover={false}>
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h3 className="text-lg font-semibold section-title">상영 일정</h3>
                <p className="text-xs text-white/60">{selectedDate} 기준</p>
              </div>
              <div className="flex items-center gap-2">
                <Button size="sm" variant="secondary" onClick={() => handleDateMove('prev')}>
                  ‹
                </Button>
                <Button size="sm" variant="secondary" onClick={() => handleDateMove('next')}>
                  ›
                </Button>
                {isOwner && (
                  <Button size="sm" variant="secondary" onClick={() => router.push('/schedules/manage')}>
                    스튜디오 관리
                  </Button>
                )}
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
              <div className="mt-4 space-y-3">
                {selectedSchedules.length === 0 && (
                  <div className="text-sm text-white/60">선택한 날짜에 상영 일정이 없습니다.</div>
                )}
                {selectedSchedules.map((item) => (
                  <Card key={item.scheduleItemId} className="p-4" variant="compact">
                    <div className="flex flex-wrap items-center justify-between gap-3">
                      <div>
                        <div className="text-xs text-white/60">
                          {new Date(item.startAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                          {' ~ '}
                          {new Date(item.endAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                        </div>
                        <h4 className="mt-1 text-base font-semibold">{item.contentTitle}</h4>
                      </div>
                      <div className="flex items-center gap-2">
                        <Badge tone={item.status === 'PLAYING' ? 'success' : 'default'}>
                          {item.status === 'PLAYING' ? '상영 중' : '대기 중'}
                        </Badge>
                        <Button size="sm" variant="primary" onClick={() => router.push(`/watch/${item.scheduleItemId}`)}>
                          입장
                        </Button>
                      </div>
                    </div>
                  </Card>
                ))}
              </div>
            )}
          </Card>
        </div>
      </div>

      {detailOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 px-4"
          onClick={(event) => {
            if (event.target === event.currentTarget) closeDetail();
          }}
          onKeyDown={(event) => {
            if (event.key === 'Escape') closeDetail();
          }}
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
                    <p className="mt-1 text-sm text-white/60">감독 {detail.ownerNickname}</p>
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
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

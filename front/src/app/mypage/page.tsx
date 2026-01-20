'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import { type ApiResponse, type SubscriptionResponse } from '@/types';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';
import Card from '@/components/ui/Card';
import SectionHeader from '@/components/ui/SectionHeader';

type UserProfileResponse = {
  userId: number;
  email: string;
  nickname: string;
  profileImageUrl: string | null;
  seller: boolean;
};

type WatchHistoryResponse = {
  watchId: number;
  contentTitle: string;
  viewedAt: string;
};

type ContentDetailResponse = {
  contentId: number;
  title: string;
  description: string;
  status: 'PUBLISHED' | 'DRAFT' | 'PRIVATE';
  ownerNickname: string;
  posterUrl: string | null;
  videoUrl: string | null;
  durationMs: number | null;
  tags: string[];
};

export default function MyPage() {
  const router = useRouter();
  const { user, setUser, logout, hasHydrated } = useAuthStore();
  const [nickname, setNickname] = useState(user?.nickname ?? '');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // 시청 기록
  const [watchHistory, setWatchHistory] = useState<WatchHistoryResponse[]>([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyError, setHistoryError] = useState('');
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState('');
  const [detailInfo, setDetailInfo] = useState<ContentDetailResponse | null>(null);
  const [detailTotalView, setDetailTotalView] = useState<number | null>(null);
  const [subscription, setSubscription] = useState<SubscriptionResponse | null>(null);

  // 회원 탈퇴
  const [showWithdraw, setShowWithdraw] = useState(false);
  const [withdrawPassword, setWithdrawPassword] = useState('');
  const [withdrawing, setWithdrawing] = useState(false);
  const [withdrawError, setWithdrawError] = useState('');

  useEffect(() => {
    if (!hasHydrated) return;
    if (!user) {
      router.push('/login?redirect=/mypage');
      return;
    }
    setNickname(user.nickname);
  }, [hasHydrated, router, user]);

  useEffect(() => {
    if (!hasHydrated) return;
    if (!user) return;

    const fetchProfile = async () => {
      try {
        const { data } = await api.get<ApiResponse<UserProfileResponse>>('/users/me');
        const profile = data.data;
        if (profile) {
          setUser({
            userId: profile.userId,
            email: profile.email,
            nickname: profile.nickname,
            profileImage: profile.profileImageUrl ?? null,
            seller: profile.seller,
          });
          setNickname(profile.nickname);
        }
      } catch (err: any) {
        setError(err.response?.data?.message || '사용자 정보를 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    const fetchWatchHistory = async () => {
      try {
        setHistoryLoading(true);
        setHistoryError('');
        const { data } = await api.get<ApiResponse<any>>('/users/me/watch-history');
        const payload = data.data;
        const list: WatchHistoryResponse[] = Array.isArray(payload)
          ? payload
          : (payload?.content ?? []);
        const deduped = Array.from(new Map(list.map((item) => [item.watchId, item])).values());
        setWatchHistory(deduped);
      } catch {
        setHistoryError('시청 기록을 불러오지 못했습니다.');
      } finally {
        setHistoryLoading(false);
      }
    };

    const fetchSubscription = async () => {
      try {
        const { data } = await api.get<ApiResponse<SubscriptionResponse>>('/users/subscriptions');
        setSubscription(data.data ?? null);
      } catch {
        setSubscription(null);
      }
    };

    fetchProfile();
    fetchWatchHistory();
    fetchSubscription();
  }, [hasHydrated, setUser, user?.userId]);

  const handleSave = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    setSuccess('');

    if (!nickname.trim()) {
      setError('닉네임을 입력해주세요.');
      return;
    }

    try {
      setSaving(true);
      const { data } = await api.patch<ApiResponse<UserProfileResponse>>('/users/me', {
        nickname: nickname.trim(),
      });
      const updated = data.data;
      if (updated) {
        setUser({
          userId: updated.userId,
          email: updated.email,
          nickname: updated.nickname,
          profileImage: updated.profileImageUrl ?? null,
          seller: updated.seller,
        });
      } else if (user) {
        setUser({ ...user, nickname: nickname.trim() });
      }
      setSuccess('정보가 저장되었습니다.');
    } catch (err: any) {
      setError(err.response?.data?.message || '정보 저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleWithdraw = async () => {
    if (!withdrawPassword.trim()) {
      setWithdrawError('비밀번호를 입력해주세요.');
      return;
    }

    if (!window.confirm('정말로 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      return;
    }

    try {
      setWithdrawing(true);
      setWithdrawError('');
      await api.delete('/users/me', {
        data: { password: withdrawPassword },
      });
      // 백엔드 로그아웃 호출
      try {
        await api.post('/auth/logout');
      } catch {
        // 로그아웃 실패 무시
      }
      logout();
      router.push('/?message=withdrawal_complete');
    } catch (err: any) {
      setWithdrawError(err.response?.data?.message || '회원 탈퇴에 실패했습니다.');
    } finally {
      setWithdrawing(false);
    }
  };

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleString('ko-KR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });

  const resolveContentIdByTitle = async (title: string) => {
    const { data } = await api.get<ApiResponse<{ content: { contentId: number; title: string; totalView: number }[] }>>('/contents', {
      params: {
        keyword: title,
        title: true,
        page: 0,
        size: 1,
      },
    });
    const list = data.data?.content ?? [];
    return list.length > 0 ? { contentId: list[0].contentId, totalView: list[0].totalView } : null;
  };

  const normalizeUrl = (value: string | null) => {
    if (!value) return null;
    if (value.startsWith('https://https://')) return value.replace('https://https://', 'https://');
    if (value.startsWith('http://http://')) return value.replace('http://http://', 'http://');
    if (value.startsWith('http://') || value.startsWith('https://')) return value;
    return `https://${value}`;
  };

  const openDetailModal = async (title: string) => {
    try {
      setDetailOpen(true);
      setDetailLoading(true);
      setDetailError('');
      const resolved = await resolveContentIdByTitle(title);
      if (!resolved) {
        setDetailError('콘텐츠 정보를 찾을 수 없습니다.');
        return;
      }
      setDetailTotalView(resolved.totalView ?? null);
      const { data } = await api.get<ApiResponse<ContentDetailResponse>>(`/contents/${resolved.contentId}`);
      const payload = data.data;
      setDetailInfo(
        payload
          ? {
              ...payload,
              posterUrl: normalizeUrl(payload.posterUrl),
              videoUrl: normalizeUrl(payload.videoUrl),
            }
          : null
      );
    } catch (err: any) {
      setDetailError(err.response?.data?.message || '상세 정보를 불러오지 못했습니다.');
    } finally {
      setDetailLoading(false);
    }
  };

  const handleOpenDetail = async (title: string, focusReview: boolean) => {
    try {
      const resolved = await resolveContentIdByTitle(title);
      if (!resolved) {
        router.push(`/search?keyword=${encodeURIComponent(title)}`);
        return;
      }
      sessionStorage.setItem(
        'search-detail-intent',
        JSON.stringify({ contentId: resolved.contentId, focusReview })
      );
      router.push('/search');
    } catch {
      router.push(`/search?keyword=${encodeURIComponent(title)}`);
    }
  };

  return (
    <div className="mx-auto max-w-3xl px-6 py-12 text-white">
      <SectionHeader
        title="마이페이지"
        subtitle="개인 정보를 확인하고 수정할 수 있습니다."
      />

      {loading && (
        <div className="mt-8 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/60">
          정보를 불러오는 중입니다...
        </div>
      )}

      {!loading && (
        <>
          {subscription?.status === 'ACTIVE' && (
            <Card className="mt-6 p-6" hover>
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h2 className="text-lg font-semibold section-title">내 구독</h2>
                  <p className="mt-1 text-sm text-white/60 section-subtitle">
                    구독 상태와 다음 결제일을 확인하세요.
                  </p>
                </div>
                <Badge className="bg-emerald-500/20 text-emerald-200">ACTIVE</Badge>
              </div>
              <div className="mt-4 grid gap-2 text-sm text-white/70 sm:grid-cols-2">
                <div>
                  <span className="text-white/50">결제 수단</span>
                  <p className="mt-1">{subscription.billingProvider}</p>
                </div>
                <div>
                  <span className="text-white/50">다음 결제일</span>
                  <p className="mt-1">
                    {subscription.nextPaymentDate
                      ? new Date(subscription.nextPaymentDate).toLocaleDateString('ko-KR')
                      : '-'}
                  </p>
                </div>
              </div>
            </Card>
          )}
          {/* 시청 기록 */}
          <div className="mt-8 rounded-lg border border-white/10 bg-white/5 p-6 card">
            <h2 className="text-lg font-semibold section-title">시청 기록</h2>
            <p className="mt-1 text-sm text-white/60 section-subtitle">최근 시청한 콘텐츠 목록입니다.</p>

            {historyLoading ? (
              <p className="mt-4 text-sm text-white/60">시청 기록을 불러오는 중...</p>
            ) : historyError ? (
              <p className="mt-4 text-sm text-red-200">{historyError}</p>
            ) : watchHistory.length === 0 ? (
              <p className="mt-4 text-sm text-white/60">시청 기록이 없습니다.</p>
            ) : (
              <div className="mt-4 grid gap-3 max-h-96 overflow-y-auto md:grid-cols-2">
                {watchHistory.map((item) => (
                  <div
                    key={item.watchId}
                    role="button"
                    tabIndex={0}
                    onClick={() => openDetailModal(item.contentTitle)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') openDetailModal(item.contentTitle);
                    }}
                    className="rounded-lg border border-white/10 bg-black/40 p-4 hover-soft cursor-pointer"
                  >
                    <div className="flex items-center justify-between text-xs text-white/60">
                      <Badge>시청 완료</Badge>
                      <span>{formatDate(item.viewedAt)}</span>
                    </div>
                    <h3 className="mt-2 text-sm font-semibold text-white/90">{item.contentTitle}</h3>
                    <div className="mt-3 flex gap-2">
                      <Button
                        size="sm"
                        variant="primary"
                        onClick={(event) => {
                          event.stopPropagation();
                          handleOpenDetail(item.contentTitle, true);
                        }}
                      >
                        리뷰 작성
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* 정산 관리 */}
          <div className="mt-8">
            <Card className="p-6" hover>
              <h2 className="text-lg font-semibold section-title">정산 관리</h2>
              <p className="mt-2 text-sm text-white/60 section-subtitle">크리에이터 정산 내역을 확인할 수 있습니다.</p>
              <Button
                variant="secondary"
                className="mt-4"
                onClick={() => router.push('/settlement')}
              >
                정산 내역 보기
              </Button>
            </Card>
          </div>

          {/* 프로필 수정 */}
          <form onSubmit={handleSave} className="mt-8 rounded-lg border border-white/10 bg-white/5 p-6 space-y-6 card">
            <div>
              <label className="block text-sm font-medium text-white/80 mb-1">이메일</label>
              <input
                value={user?.email ?? ''}
                readOnly
                className="w-full rounded-md border border-white/10 bg-white/5 px-4 py-2 text-white/70"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-white/80 mb-1">닉네임</label>
              <input
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                className="w-full rounded-md border border-white/10 bg-white/5 px-4 py-2 text-white placeholder:text-white/40 focus:ring-red-500 focus:border-red-500"
                placeholder="닉네임을 입력하세요"
              />
            </div>
            {error && (
              <div className="rounded-md border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
                {error}
              </div>
            )}
            {success && (
              <div className="rounded-md border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-200">
                {success}
              </div>
            )}
            <Button
              type="submit"
              disabled={saving}
              className="w-full py-3"
              variant="primary"
            >
              {saving ? '저장 중...' : '변경사항 저장'}
            </Button>
          </form>

          {/* 회원 탈퇴 */}
          <div className="mt-8 rounded-lg border border-red-500/20 bg-red-500/5 p-6 card">
            <h2 className="text-lg font-semibold text-red-200 section-title">회원 탈퇴</h2>
        <p className="mt-2 text-sm text-white/60">
              탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.
            </p>

            {!showWithdraw ? (
              <Button
                onClick={() => setShowWithdraw(true)}
                className="mt-4"
                variant="secondary"
              >
                회원 탈퇴하기
              </Button>
            ) : (
              <div className="mt-4 space-y-3">
                <div>
                  <label className="block text-sm font-medium text-white/80 mb-1">비밀번호 확인</label>
                  <input
                    type="password"
                    value={withdrawPassword}
                    onChange={(e) => setWithdrawPassword(e.target.value)}
                    className="w-full rounded-md border border-white/10 bg-black/60 px-4 py-2 text-white placeholder:text-white/40"
                    placeholder="비밀번호를 입력하세요"
                  />
                </div>
                {withdrawError && (
                  <div className="rounded-md border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
                    {withdrawError}
                  </div>
                )}
                <div className="flex gap-3">
                  <Button
                    onClick={handleWithdraw}
                    disabled={withdrawing}
                    variant="primary"
                  >
                    {withdrawing ? '처리 중...' : '탈퇴 확인'}
                  </Button>
                  <Button
                    variant="secondary"
                    onClick={() => {
                      setShowWithdraw(false);
                      setWithdrawPassword('');
                      setWithdrawError('');
                    }}
                  >
                    취소
                  </Button>
                </div>
              </div>
            )}
          </div>
        </>
      )}

      {detailOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 px-4"
          onClick={(e) => { if (e.target === e.currentTarget) setDetailOpen(false); }}
          onKeyDown={(e) => { if (e.key === 'Escape') setDetailOpen(false); }}
        >
          <div className="w-full max-w-3xl max-h-[90vh] overflow-y-auto rounded-2xl border border-white/10 bg-black p-6 text-white card">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-semibold section-title">콘텐츠 상세</h2>
              <Button variant="ghost" onClick={() => setDetailOpen(false)} size="sm">
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

            {!detailLoading && !detailError && detailInfo && (
              <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_2fr]">
                <div className="rounded-xl border border-white/10 bg-white/5 p-3 card">
                  {detailInfo.posterUrl ? (
                    <img
                      src={detailInfo.posterUrl}
                      alt={detailInfo.title}
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
                    <h3 className="text-2xl font-bold">{detailInfo.title}</h3>
                    <p className="mt-1 text-sm text-white/60">감독 {detailInfo.ownerNickname}</p>
                  </div>
                  <p className="text-sm text-white/70">{detailInfo.description}</p>
                  {detailInfo.tags.length > 0 && (
                    <div className="flex flex-wrap gap-2">
                      {detailInfo.tags.map((tag) => (
                        <Badge key={tag}>#{tag}</Badge>
                      ))}
                    </div>
                  )}
                  <div className="text-xs text-white/60">
                    {detailInfo.durationMs
                      ? `영상 길이 ${Math.ceil(detailInfo.durationMs / 60000)}분`
                      : '영상 길이 정보 없음'}
                  </div>
                  <div className="text-xs text-white/60">
                    총 조회수 {detailTotalView ?? 0}
                  </div>
                  <div className="flex gap-2">
                    <Button
                      size="sm"
                      variant="secondary"
                      onClick={() => handleOpenDetail(detailInfo.title, false)}
                    >
                      검색 상세로 이동
                    </Button>
                    <Button
                      size="sm"
                      variant="primary"
                      onClick={() => handleOpenDetail(detailInfo.title, true)}
                    >
                      리뷰 작성
                    </Button>
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

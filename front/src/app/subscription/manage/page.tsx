'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { type ApiResponse, type SubscriptionResponse } from '@/types';
import { useAuthStore } from '@/lib/store';

const statusLabel: Record<SubscriptionResponse['status'], string> = {
  ACTIVE: '이용 중',
  EXPIRED: '만료',
  CANCELED: '해지',
};

export default function SubscriptionManagePage() {
  const router = useRouter();
  const { user, hasHydrated } = useAuthStore();
  const [loading, setLoading] = useState(true);
  const [subscription, setSubscription] = useState<SubscriptionResponse | null>(null);
  const [error, setError] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const formatKst = (value?: string | null) => {
    if (!value) return '정보 없음';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return new Intl.DateTimeFormat('ko-KR', {
      timeZone: 'Asia/Seoul',
      dateStyle: 'long',
    }).format(date);
  };

  useEffect(() => {
    if (!hasHydrated) return;
    if (!user) {
      router.push('/login?redirect=/subscription/manage');
      return;
    }

    const fetchSubscription = async () => {
      try {
        const { data } = await api.get<ApiResponse<SubscriptionResponse>>('/users/subscriptions');
        setSubscription(data.data ?? null);
      } catch (err: any) {
        setError(err.response?.data?.message || '구독 정보를 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchSubscription();
  }, [hasHydrated, router, user]);

  const handleCancel = async () => {
    if (!subscription) return;
    const confirmed = window.confirm('정말 구독을 해지하시겠어요?');
    if (!confirmed) return;

    try {
      setActionLoading(true);
      await api.delete('/users/subscriptions');
      setSubscription({ ...subscription, status: 'CANCELED' });
    } catch (err: any) {
      setError(err.response?.data?.message || '구독 해지에 실패했습니다.');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-3xl px-6 py-12 text-white">
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-bold">내 구독 관리</h1>
        <p className="text-sm text-white/60">
          구독 상태와 결제 예정일을 확인하고, 필요한 설정을 변경할 수 있어요.
        </p>
      </div>

      {loading && (
        <div className="mt-10 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/60">
          구독 정보를 불러오는 중입니다...
        </div>
      )}

      {!loading && error && (
        <div className="mt-10 rounded-lg border border-red-500/30 bg-red-500/10 p-6 text-sm text-red-200">
          {error}
        </div>
      )}

      {!loading && !error && !subscription && (
        <div className="mt-10 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/70">
          아직 구독 정보가 없습니다. 프리미엄 구독을 시작해보세요.
          <div className="mt-4">
            <a
              href="/subscription"
              className="inline-flex items-center justify-center rounded-md bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-500 transition-colors"
            >
              구독하러 가기
            </a>
          </div>
        </div>
      )}

      {!loading && subscription && (
        <div className="mt-10 rounded-lg border border-white/10 bg-white/5 p-6">
          <div className="grid gap-6 text-center sm:grid-cols-2">
            <div>
              <p className="text-sm text-white/60">현재 상태</p>
              <p className="mt-1 text-lg font-semibold">{statusLabel[subscription.status]}</p>
            </div>
            <div>
              <p className="text-sm text-white/60">다음 결제 예정일</p>
              <p className="mt-1 text-lg font-semibold">
                {formatKst(subscription.nextPaymentDate)}
              </p>
            </div>
          </div>

          <div className="mt-8 flex flex-col gap-3 sm:flex-row">
            <a
              href="/subscription"
              className="flex-1 rounded-md border border-white/20 py-3 text-center text-sm font-semibold text-white/80 hover:border-white/60 hover:text-white transition-colors"
            >
              결제 수단 변경
            </a>
            <button
              type="button"
              onClick={handleCancel}
              disabled={actionLoading || subscription.status !== 'ACTIVE'}
              className="flex-1 rounded-md bg-white/10 py-3 text-sm font-semibold text-white hover:bg-white/20 disabled:cursor-not-allowed disabled:bg-white/5 transition-colors"
            >
              {actionLoading ? '해지 처리 중...' : '구독 해지'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}


'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import { type ApiResponse, type SubscriptionResponse } from '@/types';

export default function SubscriptionGate({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { user, hasHydrated } = useAuthStore();
  const [checking, setChecking] = useState(true);
  const [allowed, setAllowed] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!hasHydrated) return;
    if (!user) {
      router.push('/login');
      return;
    }

    const checkSubscription = async () => {
      try {
        const { data } = await api.get<ApiResponse<SubscriptionResponse>>('/users/subscriptions');
        const sub = data.data;
        if (sub?.status === 'ACTIVE') {
          setAllowed(true);
          return;
        }
        setMessage('구독자만 이용할 수 있습니다.');
      } catch (err: any) {
        const status = err?.response?.status;
        if (status === 404) {
          setMessage('구독자만 이용할 수 있습니다.');
        } else {
          setMessage('구독 정보를 확인할 수 없습니다. 잠시 후 다시 시도해주세요.');
        }
      } finally {
        setChecking(false);
      }
    };

    checkSubscription();
  }, [hasHydrated, router, user]);

  if (checking) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-20 text-center text-white">
        <p className="text-sm text-white/60">구독 정보를 확인하는 중입니다...</p>
      </div>
    );
  }

  if (!allowed) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-20 text-center text-white">
        <h1 className="text-2xl font-bold">구독자 전용</h1>
        <p className="mt-3 text-sm text-white/70">{message}</p>
        <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <button
            type="button"
            onClick={() => router.push('/subscription')}
            className="rounded-md bg-red-600 px-6 py-2 text-sm font-semibold text-white hover:bg-red-500 transition-colors"
          >
            구독하러 가기
          </button>
          <button
            type="button"
            onClick={() => router.push('/')}
            className="rounded-md border border-white/20 px-6 py-2 text-sm font-semibold text-white/80 hover:border-white/60 hover:text-white transition-colors"
          >
            홈으로 이동
          </button>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}


'use client';

import { useEffect, useRef, useState } from 'react';
import { useAuthStore } from '@/lib/store';
import { api } from '@/lib/api';
import { type ApiResponse, type SubscriptionResponse } from '@/types';
import { useRouter } from 'next/navigation';

const clientKey = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY;
const tossScriptUrl = 'https://js.tosspayments.com/v1';

type TossPaymentsInstance = {
  requestBillingAuth: (
    method: string,
    options: {
      customerKey: string;
      customerEmail?: string;
      customerName?: string;
      successUrl: string;
      failUrl: string;
    }
  ) => Promise<unknown>;
};

declare global {
  interface Window {
    TossPayments?: (key: string) => TossPaymentsInstance;
  }
}

const loadTossPayments = (key: string): Promise<TossPaymentsInstance> =>
  new Promise((resolve, reject) => {
    if (typeof window === 'undefined') {
      reject(new Error('Window is not available.'));
      return;
    }

    if (window.TossPayments) {
      resolve(window.TossPayments(key));
      return;
    }

    const existing = document.querySelector<HTMLScriptElement>(
      `script[src="${tossScriptUrl}"]`
    );
    if (existing) {
      existing.addEventListener('load', () => resolve(window.TossPayments!(key)));
      existing.addEventListener('error', () =>
        reject(new Error('Failed to load TossPayments script.'))
      );
      return;
    }

    const script = document.createElement('script');
    script.src = tossScriptUrl;
    script.async = true;
    script.onload = () => resolve(window.TossPayments!(key));
    script.onerror = () => reject(new Error('Failed to load TossPayments script.'));
    document.head.appendChild(script);
  });

export default function SubscriptionPage() {
  const { user, hasHydrated } = useAuthStore();
  const router = useRouter();
  const tossPaymentsRef = useRef<TossPaymentsInstance | null>(null);
  const autoStartRef = useRef(false);
  const [loading, setLoading] = useState(true);
  const [widgetError, setWidgetError] = useState('');
  const [notice, setNotice] = useState('');

  useEffect(() => {
    if (!hasHydrated) {
      return;
    }

    const initWidget = async () => {
      try {
        if (!clientKey) {
          setWidgetError('결제 위젯을 시작할 수 없습니다. 관리자에게 문의해주세요.');
          return;
        }
        if (!user) {
          return;
        }
        try {
          const { data } = await api.get<ApiResponse<SubscriptionResponse>>('/users/subscriptions');
          const existing = data.data;
          if (existing?.status === 'ACTIVE') {
            router.replace('/search');
            return;
          }
        } catch (err: any) {
          const status = err?.response?.status;
          if (status !== 403 && status !== 404) {
            throw err;
          }
        }
        const tossPayments = await loadTossPayments(clientKey);
        tossPaymentsRef.current = tossPayments;
      } catch (err) {
        console.error('Failed to load Toss Payments widget', err);
        setWidgetError('결제 위젯을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
      } finally {
        setLoading(false);
      }
    };

    initWidget();
  }, [user, hasHydrated, router]);

  useEffect(() => {
    if (!hasHydrated || !user) return;
    if (autoStartRef.current) return;
    const autoStart = sessionStorage.getItem('auto-subscribe');
    if (!autoStart) return;
    autoStartRef.current = true;
    sessionStorage.removeItem('auto-subscribe');
    void handleSubscription();
  }, [hasHydrated, user]);

  const handleSubscription = async () => {
    setNotice('');
    if (!clientKey) {
      setWidgetError('결제 위젯을 시작할 수 없습니다. 관리자에게 문의해주세요.');
      return;
    }
    if (!user) {
      sessionStorage.setItem('auto-subscribe', '1');
      router.push('/login?redirect=/subscription');
      return;
    }

    try {
      setLoading(true);
      try {
        const { data } = await api.get<ApiResponse<SubscriptionResponse>>('/users/subscriptions');
        const existing = data.data;
        if (existing?.status === 'ACTIVE') {
          router.push('/search');
          return;
        }
      } catch (err: any) {
        const status = err?.response?.status;
        // 403/404는 "구독 없음"으로 간주하고 결제 플로우 계속 진행
        if (status !== 403 && status !== 404) {
          throw err;
        }
      }

      const tossPayments =
        tossPaymentsRef.current ?? (await loadTossPayments(clientKey));
      tossPaymentsRef.current = tossPayments;

      await tossPayments.requestBillingAuth('카드', {
        customerKey: `user-${user.userId}`,
        customerEmail: user.email,
        customerName: user.nickname,
        successUrl: `${window.location.origin}/subscription/success`,
        failUrl: `${window.location.origin}/subscription/fail`,
      });
    } catch (error) {
      console.error('Payment request failed', error);
      setWidgetError('결제 요청에 실패했습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-6xl px-6 py-12 text-white">
      <div className="text-center">
        <h1 className="text-4xl font-bold leading-tight sm:text-5xl">
          영화관을 집으로, 친구들과 함께
        </h1>
        <p className="mt-4 text-sm text-white/60 sm:text-base">
          구독하고 지금 바로 상영 중인 영화에 입장하세요.
          끊김 없는 고화질 스트리밍과 실시간 채팅을 무제한으로 즐기세요.
        </p>
      </div>

      <div className="mt-10 flex justify-center">
        <div className="w-full max-w-2xl rounded-2xl border border-white/10 bg-white/5 p-8">
          <h2 className="text-sm font-semibold text-white/60">MONTHLY PASS</h2>
          <div className="mt-4 flex items-end gap-2">
            <span className="text-4xl font-bold">₩9,900</span>
            <span className="text-sm text-white/50">/ month</span>
          </div>
          <p className="mt-2 text-sm text-white/50">언제든지 해지 가능</p>

          <button
            onClick={handleSubscription}
            disabled={loading}
            className="mt-6 w-full rounded-xl bg-red-600 py-3 text-sm font-semibold text-white hover:bg-red-500 disabled:bg-white/20 transition-colors"
          >
            {loading ? '결제 준비 중...' : '월간 구독 시작하기'}
          </button>

          <ul className="mt-6 space-y-2 text-sm text-white/70">
            <li>✓ 모든 상영관 무제한 입장</li>
            <li>✓ 실시간 채팅 참여</li>
            <li>✓ 광고 없는 재생</li>
          </ul>

        </div>
      </div>

      {widgetError && (
        <div className="mt-6 rounded-md border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
          {widgetError}
        </div>
      )}

      {notice && (
        <div className="mt-6 rounded-md border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-200">
          {notice}
        </div>
      )}

      <div className="mt-12 rounded-2xl border border-white/10 bg-white/5 p-8">
        <h3 className="text-lg font-semibold">왜 구독해야 하나요?</h3>
        <p className="mt-2 text-sm text-white/60">
          Watch Party Cinema만의 특별한 경험을 놓치지 마세요.
        </p>
        <div className="mt-6 grid gap-4 sm:grid-cols-3">
          <div className="rounded-xl border border-white/10 bg-white/5 p-4">
            <h4 className="text-sm font-semibold">실시간 소셜 시네마</h4>
            <p className="mt-2 text-xs text-white/60">
              친구들과 동시에 감상하며 채팅으로 소통해요.
            </p>
          </div>
          <div className="rounded-xl border border-white/10 bg-white/5 p-4">
            <h4 className="text-sm font-semibold">4K 초고화질 스트리밍</h4>
            <p className="mt-2 text-xs text-white/60">
              끊김 없는 스트리밍으로 몰입도를 높여요.
            </p>
          </div>
          <div className="rounded-xl border border-white/10 bg-white/5 p-4">
            <h4 className="text-sm font-semibold">나만의 상영관 호스팅</h4>
            <p className="mt-2 text-xs text-white/60">
              취향에 맞는 상영 일정을 직접 만들 수 있어요.
            </p>
          </div>
        </div>
      </div>

      <p className="mt-10 text-center text-xs text-white/50">
        안전한 결제를 위해 Toss Payments를 사용합니다.
      </p>
    </div>
  );
}

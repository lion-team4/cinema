'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/store';
import { api } from '@/lib/api';
import type { ApiResponse, SubscriptionResponse } from '@/types';

export default function SiteHeader() {
  const router = useRouter();
  const { user, accessToken, logout } = useAuthStore();
  const [subscription, setSubscription] = useState<SubscriptionResponse | null>(null);

  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
    } catch {
      // ignore network/logout errors
    } finally {
      logout();
      router.push('/login');
    }
  };

  const showLoading = !user && !!accessToken;

  useEffect(() => {
    if (!user) {
      setSubscription(null);
      return;
    }
    const loadSubscription = async () => {
      try {
        const { data } = await api.get<ApiResponse<SubscriptionResponse>>('/users/subscriptions');
        setSubscription(data.data ?? null);
      } catch {
        setSubscription(null);
      }
    };
    loadSubscription();
  }, [user]);

  return (
    <header className="sticky top-0 z-50 border-b border-white/10 bg-black/80 backdrop-blur">
      <nav className="container mx-auto flex h-16 items-center justify-between px-6">
        <Link href="/" className="brand-title text-xl font-semibold tracking-[0.18em] text-white/90">
          Your private cinema
        </Link>
        <div className="flex items-center gap-6 text-sm font-medium text-white/80">
          <Link href="/search" className="hover:text-white transition-colors">검색</Link>
          <Link href="/theaters" className="hover:text-white transition-colors">상영관</Link>
          <Link href="/schedules/manage" className="hover:text-white transition-colors">스튜디오</Link>
          {user && (
            <Link href={`/filmography/${encodeURIComponent(user.nickname)}`} className="hover:text-white transition-colors">
              필모그래피
            </Link>
          )}
          {(!subscription || subscription.status !== 'ACTIVE') && (
            <Link href="/subscription" className="hover:text-white transition-colors">구독</Link>
          )}
          {showLoading ? (
            <span className="text-white/50">로그인 확인 중...</span>
          ) : user ? (
            <>
              <Link href="/mypage" className="hover:text-white transition-colors">
                {user.nickname}
              </Link>
              <button
                type="button"
                onClick={handleLogout}
                className="rounded-md border border-white/20 px-3 py-1.5 text-xs font-semibold text-white/80 hover:border-white/60 hover:text-white transition-colors"
              >
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link href="/signup" className="hover:text-white transition-colors">회원가입</Link>
              <Link
                href="/login"
                className="rounded-md bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-500 transition-colors"
              >
                로그인
              </Link>
            </>
          )}
        </div>
      </nav>
    </header>
  );
}


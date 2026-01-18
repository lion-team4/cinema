'use client';

import { useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { api, publicApi } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import { type ApiResponse, type TokenResponse } from '@/types';

type UserProfileResponse = {
  userId: number;
  email: string;
  nickname: string;
  profileImageUrl: string | null;
  seller: boolean;
};

export default function LoginPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const redirectTo = useMemo(
    () => searchParams.get('redirect') || '/',
    [searchParams]
  );
  const presetEmail = searchParams.get('email') || '';

  const { setAuth, setUser, logout } = useAuthStore();
  const [email, setEmail] = useState(presetEmail);
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      const { data } = await publicApi.post<ApiResponse<TokenResponse>>('/auth/login', {
        email,
        password,
      });

      const { accessToken, refreshToken } = data.data ?? {};
      if (!accessToken || !refreshToken) {
        setError('로그인에 실패했습니다. 다시 시도해주세요.');
        return;
      }
      setAuth(accessToken, refreshToken);

      try {
        const profile = await api.get<ApiResponse<UserProfileResponse>>('/users/me', {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        });
        const user = profile.data.data;
        if (!user) {
          throw new Error('Empty profile');
        }
        setUser({
          userId: user.userId,
          email: user.email,
          nickname: user.nickname,
          profileImage: user.profileImageUrl ?? null,
          seller: user.seller,
        });
      } catch {
        logout();
        setError('로그인에 실패했습니다. 다시 시도해주세요.');
        return;
      }

      router.push(redirectTo);
    } catch (err: any) {
      setError(err.response?.data?.message || '로그인에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-black text-white">
      <div className="mx-auto flex w-full max-w-md flex-col px-6 py-16">
        <h1 className="text-3xl font-bold tracking-tight">로그인</h1>
        <p className="mt-2 text-sm text-white/60">
          방구석 영화관에 오신 것을 환영합니다.
        </p>

        <form onSubmit={handleSubmit} className="mt-8 space-y-5">
          <div>
            <label className="text-sm font-medium text-white/80">이메일</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="mt-2 w-full rounded-md border border-white/10 bg-white/5 px-4 py-3 text-sm text-white placeholder:text-white/40 focus:border-red-500 focus:outline-none"
              placeholder="you@example.com"
            />
          </div>
          <div>
            <label className="text-sm font-medium text-white/80">비밀번호</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="mt-2 w-full rounded-md border border-white/10 bg-white/5 px-4 py-3 text-sm text-white placeholder:text-white/40 focus:border-red-500 focus:outline-none"
              placeholder="••••••••"
            />
          </div>

          {error && (
            <div className="rounded-md border border-red-500/40 bg-red-500/10 px-4 py-3 text-sm text-red-200">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={submitting}
            className="w-full rounded-md bg-red-600 py-3 text-sm font-semibold text-white hover:bg-red-500 disabled:cursor-not-allowed disabled:bg-white/20 transition-colors"
          >
            {submitting ? '로그인 중...' : '로그인'}
          </button>
        </form>

        <p className="mt-6 text-sm text-white/60">
          아직 계정이 없나요?{' '}
          <a href="/signup" className="font-semibold text-white hover:text-red-400">
            회원가입
          </a>
        </p>
      </div>
    </div>
  );
}


'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { publicApi } from '@/lib/api';
import { type ApiResponse } from '@/types';

type SignupResponse = {
  userId: number;
  email: string;
  nickname: string;
  profileImageUrl: string | null;
  seller: boolean;
};

export default function SignupPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [nickname, setNickname] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      await publicApi.post<ApiResponse<SignupResponse>>('/auth/signup', {
        email,
        password,
        nickname,
      });

      router.push(`/login?email=${encodeURIComponent(email)}`);
    } catch (err: any) {
      setError(err.response?.data?.message || '회원가입에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-black text-white">
      <div className="mx-auto flex w-full max-w-md flex-col px-6 py-16">
        <h1 className="text-3xl font-bold tracking-tight">회원가입</h1>
        <p className="mt-2 text-sm text-white/60">
          방구석 영화관에서 프리미엄 스트리밍을 시작하세요.
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
            <label className="text-sm font-medium text-white/80">닉네임</label>
            <input
              type="text"
              required
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              className="mt-2 w-full rounded-md border border-white/10 bg-white/5 px-4 py-3 text-sm text-white placeholder:text-white/40 focus:border-red-500 focus:outline-none"
              placeholder="닉네임"
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
              placeholder="최소 8자"
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
            {submitting ? '가입 중...' : '회원가입'}
          </button>
        </form>
      </div>
    </div>
  );
}


'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import { type ApiResponse } from '@/types';

type UserProfileResponse = {
  userId: number;
  email: string;
  nickname: string;
  profileImageUrl: string | null;
  seller: boolean;
};

export default function MyPage() {
  const router = useRouter();
  const { user, setUser, hasHydrated } = useAuthStore();
  const [nickname, setNickname] = useState(user?.nickname ?? '');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (!hasHydrated) return;
    if (!user) {
      router.push('/login?redirect=/mypage');
      return;
    }

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

    fetchProfile();
  }, [hasHydrated, router, setUser, user]);

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

  return (
    <div className="mx-auto max-w-3xl px-6 py-12 text-white">
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-bold">마이페이지</h1>
        <p className="text-sm text-white/60">
          개인 정보를 확인하고 수정할 수 있습니다.
        </p>
      </div>

      {loading && (
        <div className="mt-8 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/60">
          정보를 불러오는 중입니다...
        </div>
      )}

      {!loading && (
        <form onSubmit={handleSave} className="mt-8 rounded-lg border border-white/10 bg-white/5 p-6 space-y-6">
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

          <button
            type="submit"
            disabled={saving}
            className="w-full rounded-md bg-red-600 py-3 text-sm font-semibold text-white hover:bg-red-500 disabled:bg-white/20 transition-colors"
          >
            {saving ? '저장 중...' : '변경사항 저장'}
          </button>
        </form>
      )}

      <div className="mt-8 rounded-lg border border-white/10 bg-white/5 p-6">
        <h2 className="text-lg font-semibold">콘텐츠 관리</h2>
        <p className="mt-2 text-sm text-white/60">
          내 콘텐츠 목록을 확인하고 관리할 수 있습니다.
        </p>
        <a
          href="/contents/manage"
          className="mt-4 inline-flex items-center justify-center rounded-md border border-white/20 px-4 py-2 text-sm font-semibold text-white/80 hover:border-white/60 hover:text-white transition-colors"
        >
          내 콘텐츠 관리로 이동
        </a>
      </div>
    </div>
  );
}


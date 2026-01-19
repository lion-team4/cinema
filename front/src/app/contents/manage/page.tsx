'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import { type ApiResponse, type ContentSearchResponse, type PageResponse } from '@/types';
import SubscriptionGate from '@/components/guards/SubscriptionGate';

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

export default function ContentManagePage() {
  const router = useRouter();
  const { user, hasHydrated } = useAuthStore();
  const [items, setItems] = useState<ContentSearchResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!hasHydrated) return;
    if (!user) {
      router.push('/login?redirect=/contents/manage');
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
            size: 20,
          },
        });
        setItems(data.data?.content ?? []);
      } catch (err: any) {
        setError(err.response?.data?.message || '콘텐츠 목록을 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchContents();
  }, [hasHydrated, router, user]);

  return (
    <SubscriptionGate>
      <div className="mx-auto max-w-6xl px-6 py-12 text-white">
        <div className="flex flex-col gap-2">
          <h1 className="text-3xl font-bold">내 콘텐츠 관리</h1>
          <p className="text-sm text-white/60">
            내가 업로드한 콘텐츠를 한눈에 확인하고 관리하세요.
          </p>
        </div>

      <div className="mt-6 flex justify-end">
        <a
          href="/contents/create"
          className="inline-flex items-center justify-center rounded-md bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-500 transition-colors"
        >
          콘텐츠 업로드
        </a>
      </div>

      {loading && (
        <div className="mt-10 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/60">
          콘텐츠를 불러오는 중입니다...
        </div>
      )}

      {!loading && error && (
        <div className="mt-10 rounded-lg border border-red-500/30 bg-red-500/10 p-6 text-sm text-red-200">
          {error}
        </div>
      )}

      {!loading && !error && items.length === 0 && (
        <div className="mt-10 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/70">
          콘텐츠가 없습니다. 새로운 콘텐츠를 업로드해보세요.
        </div>
      )}

      {!loading && !error && items.length > 0 && (
        <div className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {items.map((item) => (
            <div key={item.contentId} className="rounded-xl border border-white/10 bg-white/5 p-5">
              <div className="flex items-center justify-between">
                <span className={`rounded-full px-2.5 py-1 text-xs ${statusColor[item.status]}`}>
                  {statusLabel[item.status]}
                </span>
                <span className="text-xs text-white/50">조회수 {item.totalView}</span>
              </div>
              <h3 className="mt-4 text-lg font-semibold">{item.title}</h3>
              <p className="mt-2 text-sm text-white/70 line-clamp-3">{item.description}</p>
              <div className="mt-4 text-xs text-white/40">
                작성자 {item.ownerNickname}
              </div>
              <a
                href={`/studio/contents/${item.contentId}/edit`}
                className="mt-4 inline-flex items-center text-xs font-semibold text-red-400 hover:text-red-300"
              >
                수정하기 →
              </a>
            </div>
          ))}
        </div>
      )}
    </div>
    </SubscriptionGate>
  );
}


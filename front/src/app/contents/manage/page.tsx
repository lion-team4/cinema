'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import { type ApiResponse, type ContentSearchResponse, type PageResponse } from '@/types';
import SubscriptionGate from '@/components/guards/SubscriptionGate';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';
import Card from '@/components/ui/Card';
import SectionHeader from '@/components/ui/SectionHeader';

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
  const [deleting, setDeleting] = useState<number | null>(null);

  const fetchContents = useCallback(async () => {
    if (!user) return;
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
  }, [user]);

  useEffect(() => {
    if (!hasHydrated) return;
    if (!user) {
      router.push('/login?redirect=/contents/manage');
      return;
    }

    fetchContents();
  }, [hasHydrated, router, user, fetchContents]);

  const handleDelete = async (contentId: number, title: string) => {
    if (!window.confirm(`"${title}" 콘텐츠를 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`)) {
      return;
    }

    try {
      setDeleting(contentId);
      await api.delete(`/contents/${contentId}`);
      // 목록에서 제거
      setItems((prev) => prev.filter((item) => item.contentId !== contentId));
    } catch (err: any) {
      alert(err.response?.data?.message || '콘텐츠 삭제에 실패했습니다.');
    } finally {
      setDeleting(null);
    }
  };

  return (
    <SubscriptionGate>
      <div className="mx-auto max-w-6xl px-6 py-12 text-white">
        <SectionHeader
          title="내 콘텐츠 관리"
          subtitle="내가 업로드한 콘텐츠를 한눈에 확인하고 관리하세요."
        />

      <div className="mt-6 flex justify-end">
          <Button variant="primary" onClick={() => router.push('/contents/create')}>
          콘텐츠 업로드
          </Button>
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
              <Card key={item.contentId} hover>
              <div className="flex items-center justify-between">
                  <Badge className={statusColor[item.status]}>{statusLabel[item.status]}</Badge>
                <span className="text-xs text-white/50">조회수 {item.totalView}</span>
              </div>
              <h3 className="mt-4 text-lg font-semibold">{item.title}</h3>
              <p className="mt-2 text-sm text-white/70 line-clamp-3">{item.description}</p>
              <div className="mt-4 text-xs text-white/40">
                작성자 {item.ownerNickname}
              </div>
                <div className="mt-4 flex items-center gap-3">
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={() => router.push(`/studio/contents/${item.contentId}/edit`)}
                  >
                    수정하기
                  </Button>
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={() => handleDelete(item.contentId, item.title)}
                    disabled={deleting === item.contentId}
                  >
                    {deleting === item.contentId ? '삭제 중...' : '삭제'}
                  </Button>
            </div>
              </Card>
          ))}
        </div>
      )}
    </div>
    </SubscriptionGate>
  );
}

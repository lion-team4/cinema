'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import type { ApiResponse, PageResponse } from '@/types';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import SectionHeader from '@/components/ui/SectionHeader';

type SettlementListResponse = {
  settlementId: number;
  periodStart: string;
  periodEnd: string;
  totalViews: number;
  amount: number;
  status: 'PROCESSING' | 'COMPLETED' | 'FAILED';
};

type SettlementStatsResponse = {
  totalSettlementAmount: number;
  totalSettlements: number;
  lastSettlementDate: string | null;
  lastSettlementAmount: number | null;
};

const statusLabel: Record<string, string> = {
  PROCESSING: '처리 중',
  COMPLETED: '완료',
  FAILED: '실패',
};

const statusColor: Record<string, string> = {
  PROCESSING: 'bg-amber-500/20 text-amber-200',
  COMPLETED: 'bg-emerald-500/20 text-emerald-200',
  FAILED: 'bg-red-500/20 text-red-200',
};

export default function SettlementPage() {
  const router = useRouter();
  const { user, hasHydrated } = useAuthStore();
  const [stats, setStats] = useState<SettlementStatsResponse | null>(null);
  const [settlements, setSettlements] = useState<SettlementListResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState<'ALL' | SettlementListResponse['status']>('ALL');

  useEffect(() => {
    if (!hasHydrated) return;
    if (!user) {
      router.push('/login?redirect=/settlement');
      return;
    }

    const fetchData = async () => {
      try {
        setLoading(true);
        setError('');

        const [statsRes, listRes] = await Promise.all([
          api.get<ApiResponse<SettlementStatsResponse>>('/settlements/stats'),
          api.get<ApiResponse<PageResponse<SettlementListResponse>>>('/settlements', {
            params: { page, size: 50 },
          }),
        ]);

        setStats(statsRes.data.data ?? null);
        setSettlements(listRes.data.data?.content ?? []);
        setTotalPages(listRes.data.data?.totalPages ?? 0);
      } catch (err: any) {
        setError(err.response?.data?.message || '정산 정보를 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [hasHydrated, router, user, page]);

  const formatCurrency = (amount: number) =>
    new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(amount);

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('ko-KR');
  };

  const formatCompact = (amount: number) =>
    new Intl.NumberFormat('ko-KR', { notation: 'compact', maximumFractionDigits: 1 }).format(amount);

  const filteredSettlements = useMemo(() => {
    if (statusFilter === 'ALL') return settlements;
    return settlements.filter((item) => item.status === statusFilter);
  }, [settlements, statusFilter]);

  const monthlySummary = useMemo(() => {
    const map = new Map<string, { amount: number; views: number }>();
    filteredSettlements.forEach((item) => {
      const month = new Date(item.periodStart).toISOString().slice(0, 7);
      const entry = map.get(month) ?? { amount: 0, views: 0 };
      entry.amount += item.amount;
      entry.views += item.totalViews;
      map.set(month, entry);
    });
    const entries = Array.from(map.entries()).sort(([a], [b]) => (a > b ? 1 : -1));
    let cumulative = 0;
    return entries.map(([month, data]) => {
      cumulative += data.amount;
      return { month, amount: data.amount, cumulative, views: data.views };
    });
  }, [filteredSettlements]);

  const maxAmount = monthlySummary.reduce((max, item) => Math.max(max, item.amount), 0);
  const maxCumulative = monthlySummary.reduce((max, item) => Math.max(max, item.cumulative), 0);

  return (
    <div className="mx-auto max-w-5xl px-6 py-12 text-white">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <SectionHeader
          title="정산 관리"
          subtitle="크리에이터 정산 내역을 확인하세요."
        />
        <Button variant="secondary" onClick={() => router.push('/mypage')}>
          마이페이지로 돌아가기
        </Button>
      </div>

      {loading && (
        <div className="mt-8 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/60">
          정산 정보를 불러오는 중입니다...
        </div>
      )}

      {error && (
        <div className="mt-8 rounded-lg border border-red-500/30 bg-red-500/10 p-6 text-sm text-red-200">
          {error}
        </div>
      )}

      {!loading && !error && stats && (
        <>
          {/* 통계 카드 */}
          <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Card hover>
              <p className="text-xs text-white/60">총 정산 금액</p>
              <p className="mt-2 text-2xl font-bold">{formatCurrency(stats.totalSettlementAmount)}</p>
            </Card>
            <Card hover>
              <p className="text-xs text-white/60">총 정산 건수</p>
              <p className="mt-2 text-2xl font-bold">{stats.totalSettlements}건</p>
            </Card>
            <Card hover>
              <p className="text-xs text-white/60">마지막 정산일</p>
              <p className="mt-2 text-2xl font-bold">{formatDate(stats.lastSettlementDate)}</p>
            </Card>
            <Card hover>
              <p className="text-xs text-white/60">마지막 정산 금액</p>
              <p className="mt-2 text-2xl font-bold">
                {stats.lastSettlementAmount ? formatCurrency(stats.lastSettlementAmount) : '-'}
              </p>
            </Card>
          </div>

          {/* 차트 */}
          <div className="mt-8 grid gap-6 lg:grid-cols-2">
            <Card className="p-6">
              <h2 className="text-lg font-semibold section-title">월별 정산액</h2>
              {monthlySummary.length === 0 ? (
                <p className="mt-4 text-sm text-white/60">표시할 데이터가 없습니다.</p>
              ) : (
                <div className="mt-4 grid grid-cols-6 gap-3 text-xs text-white/60">
                  {monthlySummary.map((item) => (
                    <div key={item.month} className="flex flex-col items-center gap-2">
                      <div
                        className="h-32 w-3 rounded-full bg-white/10 flex items-end"
                        title={`정산액 ${formatCurrency(item.amount)}`}
                      >
                        <div
                          className="w-3 rounded-full bg-red-500"
                          style={{ height: `${maxAmount ? (item.amount / maxAmount) * 100 : 0}%` }}
                        />
                      </div>
                      <span>{item.month}</span>
                      <span className="text-[10px] text-white/50">{formatCompact(item.amount)}</span>
                    </div>
                  ))}
                </div>
              )}
            </Card>
            <Card className="p-6">
              <h2 className="text-lg font-semibold section-title">누적 정산액</h2>
              {monthlySummary.length === 0 ? (
                <p className="mt-4 text-sm text-white/60">표시할 데이터가 없습니다.</p>
              ) : (
                <div className="mt-4 grid grid-cols-6 gap-3 text-xs text-white/60">
                  {monthlySummary.map((item) => (
                    <div key={item.month} className="flex flex-col items-center gap-2">
                      <div
                        className="h-32 w-3 rounded-full bg-white/10 flex items-end"
                        title={`누적 ${formatCurrency(item.cumulative)}`}
                      >
                        <div
                          className="w-3 rounded-full bg-emerald-400"
                          style={{ height: `${maxCumulative ? (item.cumulative / maxCumulative) * 100 : 0}%` }}
                        />
                      </div>
                      <span>{item.month}</span>
                      <span className="text-[10px] text-white/50">{formatCompact(item.cumulative)}</span>
                    </div>
                  ))}
                </div>
              )}
            </Card>
          </div>

          {/* 정산 내역 테이블 */}
          <Card className="mt-8 p-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <h2 className="text-lg font-semibold section-title">정산 내역</h2>
              <div className="flex flex-wrap items-center gap-2 text-xs text-white/70">
                {(['ALL', 'PROCESSING', 'COMPLETED', 'FAILED'] as const).map((status) => (
                  <Button
                    key={status}
                    size="sm"
                    variant="secondary"
                    onClick={() => setStatusFilter(status)}
                    className={`rounded-full border px-3 py-1 ${
                      statusFilter === status
                        ? 'border-red-500/60 bg-red-500/20 text-red-100'
                        : 'border-white/10 text-white/60'
                    } badge`}
                  >
                    {status === 'ALL' ? '전체' : statusLabel[status]}
                  </Button>
                ))}
              </div>
            </div>

            {filteredSettlements.length === 0 ? (
              <p className="mt-4 text-sm text-white/60">정산 내역이 없습니다.</p>
            ) : (
              <div className="mt-4 overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-white/10 text-left text-white/60">
                      <th className="pb-3 pr-4">정산 ID</th>
                      <th className="pb-3 pr-4">기간</th>
                      <th className="pb-3 pr-4">총 조회수</th>
                      <th className="pb-3 pr-4">정산 금액</th>
                      <th className="pb-3">상태</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredSettlements.map((item) => (
                      <tr key={item.settlementId} className="border-b border-white/5">
                        <td className="py-3 pr-4">#{item.settlementId}</td>
                        <td className="py-3 pr-4">
                          {formatDate(item.periodStart)} ~ {formatDate(item.periodEnd)}
                        </td>
                        <td className="py-3 pr-4">{item.totalViews.toLocaleString()}</td>
                        <td className="py-3 pr-4 font-semibold">{formatCurrency(item.amount)}</td>
                        <td className="py-3">
                          <Badge className={statusColor[item.status]}>
                            {statusLabel[item.status]}
                          </Badge>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {/* 페이지네이션 */}
            {totalPages > 1 && (
              <div className="mt-6 flex items-center justify-center gap-4 text-sm text-white/70">
                <Button
                  size="sm"
                  variant="secondary"
                  onClick={() => setPage((prev) => Math.max(0, prev - 1))}
                  disabled={page <= 0}
                >
                  이전
                </Button>
                <span>
                  {page + 1} / {totalPages}
                </span>
                <Button
                  size="sm"
                  variant="secondary"
                  onClick={() => setPage((prev) => Math.min(totalPages - 1, prev + 1))}
                  disabled={page >= totalPages - 1}
                >
                  다음
                </Button>
              </div>
            )}
          </Card>
        </>
      )}
    </div>
  );
}

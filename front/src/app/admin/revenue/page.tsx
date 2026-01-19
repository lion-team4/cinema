'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import type { ApiResponse, PageResponse } from '@/types';

type PlatformRevenueResponse = {
  periodStart: string;
  periodEnd: string;
  totalPaymentAmount: number;
  totalSettlementAmount: number;
  platformRevenue: number;
};

type PlatformRevenueStatsResponse = {
  totalPlatformRevenue: number;
  totalPaymentAmount: number;
  totalSettlementAmount: number;
  averageMonthlyRevenue: number;
  lastMonthRevenue: number;
  lastMonthDate: string | null;
};

type MonthlyPlatformRevenueResponse = {
  month: string;
  paymentAmount: number;
  settlementAmount: number;
  platformRevenue: number;
};

export default function AdminRevenuePage() {
  const router = useRouter();
  const { user, hasHydrated } = useAuthStore();
  const [stats, setStats] = useState<PlatformRevenueStatsResponse | null>(null);
  const [monthlyData, setMonthlyData] = useState<MonthlyPlatformRevenueResponse[]>([]);
  const [currentPeriod, setCurrentPeriod] = useState<PlatformRevenueResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!hasHydrated) return;
    if (!user) {
      router.push('/login?redirect=/admin/revenue');
      return;
    }

    const fetchData = async () => {
      try {
        setLoading(true);
        setError('');

        const [statsRes, periodRes, monthlyRes] = await Promise.all([
          api.get<ApiResponse<PlatformRevenueStatsResponse>>('/admin/platform-revenue/stats'),
          api.get<ApiResponse<PlatformRevenueResponse>>('/admin/platform-revenue'),
          api.get<ApiResponse<PageResponse<MonthlyPlatformRevenueResponse>>>('/admin/platform-revenue/monthly', {
            params: { page: 0, size: 12 },
          }),
        ]);

        setStats(statsRes.data.data ?? null);
        setCurrentPeriod(periodRes.data.data ?? null);
        setMonthlyData(monthlyRes.data.data?.content ?? []);
      } catch (err: any) {
        setError(err.response?.data?.message || '수익 정보를 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [hasHydrated, router, user]);

  const formatCurrency = (amount: number) =>
    new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(amount);

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('ko-KR');
  };

  return (
    <div className="mx-auto max-w-6xl px-6 py-12 text-white">
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-bold">플랫폼 수익 관리</h1>
        <p className="text-sm text-white/60">관리자 대시보드 - 플랫폼 수익 현황을 확인하세요.</p>
      </div>

      {loading && (
        <div className="mt-8 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/60">
          수익 정보를 불러오는 중입니다...
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
          <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <div className="rounded-xl border border-white/10 bg-white/5 p-5">
              <p className="text-xs text-white/60">총 플랫폼 수익</p>
              <p className="mt-2 text-2xl font-bold text-emerald-400">{formatCurrency(stats.totalPlatformRevenue)}</p>
            </div>
            <div className="rounded-xl border border-white/10 bg-white/5 p-5">
              <p className="text-xs text-white/60">총 결제 금액</p>
              <p className="mt-2 text-2xl font-bold">{formatCurrency(stats.totalPaymentAmount)}</p>
            </div>
            <div className="rounded-xl border border-white/10 bg-white/5 p-5">
              <p className="text-xs text-white/60">총 정산 금액</p>
              <p className="mt-2 text-2xl font-bold">{formatCurrency(stats.totalSettlementAmount)}</p>
            </div>
            <div className="rounded-xl border border-white/10 bg-white/5 p-5">
              <p className="text-xs text-white/60">월평균 플랫폼 수익</p>
              <p className="mt-2 text-2xl font-bold">{formatCurrency(stats.averageMonthlyRevenue)}</p>
            </div>
            <div className="rounded-xl border border-white/10 bg-white/5 p-5">
              <p className="text-xs text-white/60">전월 플랫폼 수익</p>
              <p className="mt-2 text-2xl font-bold">{formatCurrency(stats.lastMonthRevenue)}</p>
            </div>
            <div className="rounded-xl border border-white/10 bg-white/5 p-5">
              <p className="text-xs text-white/60">전월 기준일</p>
              <p className="mt-2 text-2xl font-bold">{formatDate(stats.lastMonthDate)}</p>
            </div>
          </div>

          {/* 현재 기간 수익 */}
          {currentPeriod && (
            <div className="mt-8 rounded-2xl border border-white/10 bg-white/5 p-6">
              <h2 className="text-lg font-semibold">전월 수익 상세</h2>
              <p className="mt-1 text-sm text-white/60">
                {formatDate(currentPeriod.periodStart)} ~ {formatDate(currentPeriod.periodEnd)}
              </p>
              <div className="mt-4 grid gap-4 sm:grid-cols-3">
                <div>
                  <p className="text-xs text-white/60">결제 금액</p>
                  <p className="mt-1 text-xl font-semibold">{formatCurrency(currentPeriod.totalPaymentAmount)}</p>
                </div>
                <div>
                  <p className="text-xs text-white/60">정산 금액</p>
                  <p className="mt-1 text-xl font-semibold">{formatCurrency(currentPeriod.totalSettlementAmount)}</p>
                </div>
                <div>
                  <p className="text-xs text-white/60">플랫폼 수익</p>
                  <p className="mt-1 text-xl font-semibold text-emerald-400">{formatCurrency(currentPeriod.platformRevenue)}</p>
                </div>
              </div>
            </div>
          )}

          {/* 월별 수익 테이블 */}
          <div className="mt-8 rounded-2xl border border-white/10 bg-white/5 p-6">
            <h2 className="text-lg font-semibold">월별 수익 내역</h2>

            {monthlyData.length === 0 ? (
              <p className="mt-4 text-sm text-white/60">월별 수익 내역이 없습니다.</p>
            ) : (
              <div className="mt-4 overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-white/10 text-left text-white/60">
                      <th className="pb-3 pr-4">월</th>
                      <th className="pb-3 pr-4">결제 금액</th>
                      <th className="pb-3 pr-4">정산 금액</th>
                      <th className="pb-3">플랫폼 수익</th>
                    </tr>
                  </thead>
                  <tbody>
                    {monthlyData.map((item) => (
                      <tr key={item.month} className="border-b border-white/5">
                        <td className="py-3 pr-4">{item.month}</td>
                        <td className="py-3 pr-4">{formatCurrency(item.paymentAmount)}</td>
                        <td className="py-3 pr-4">{formatCurrency(item.settlementAmount)}</td>
                        <td className="py-3 font-semibold text-emerald-400">{formatCurrency(item.platformRevenue)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}

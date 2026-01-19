'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Client } from '@stomp/stompjs';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import type { ApiResponse } from '@/types';

type PlaybackInfoResponse = {
  assetId: number;
  videoUrl: string;
  contentType: string;
  durationMs?: number | null;
};

type PlaybackStateResponse = {
  status: 'WAITING' | 'PLAYING' | 'ENDING' | 'CLOSED';
  playing: boolean;
  positionMs: number;
  playbackRate: number;
  serverTimeMs: number;
};

export default function WatchPage() {
  const router = useRouter();
  const params = useParams();
  const scheduleId = useMemo(() => Number(params?.scheduleId), [params]);
  const { accessToken } = useAuthStore();
  const clientRef = useRef<Client | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [playbackInfo, setPlaybackInfo] = useState<PlaybackInfoResponse | null>(null);
  const [state, setState] = useState<PlaybackStateResponse | null>(null);

  useEffect(() => {
    if (!scheduleId || Number.isNaN(scheduleId)) {
      setError('잘못된 상영 일정입니다.');
      setLoading(false);
      return;
    }

    let mounted = true;

    const enterAndLoad = async () => {
      try {
        setLoading(true);
        await api.post(`/theaters/${scheduleId}/enter`);
        const { data } = await api.get<ApiResponse<PlaybackInfoResponse>>(`/theaters/${scheduleId}/playback`);
        if (mounted) {
          setPlaybackInfo(data.data ?? null);
        }
      } catch (err: any) {
        if (mounted) {
          setError(err.response?.data?.message || '상영 정보를 불러오지 못했습니다.');
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    };

    const connectSocket = () => {
      const client = new Client({
        brokerURL: process.env.NEXT_PUBLIC_WS_URL ?? 'ws://localhost:8080/ws',
        reconnectDelay: 3000,
        connectHeaders: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
      });

      client.onConnect = () => {
        client.subscribe(`/app/theaters/${scheduleId}/state`, (message) => {
          try {
            const payload = JSON.parse(message.body) as PlaybackStateResponse;
            setState(payload);
          } catch {
            // ignore malformed payloads
          }
        });
      };

      client.onStompError = () => {
        setError('상영장 연결에 실패했습니다.');
      };

      client.activate();
      clientRef.current = client;
    };

    enterAndLoad();
    connectSocket();

    return () => {
      mounted = false;
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
      }
      api.post(`/theaters/${scheduleId}/leave`).catch(() => {});
    };
  }, [accessToken, scheduleId]);

  return (
    <div className="mx-auto max-w-5xl px-6 py-12 text-white">
      <button
        type="button"
        onClick={() => router.back()}
        className="text-sm text-white/60 hover:text-white transition-colors"
      >
        ← 검색으로 돌아가기
      </button>

      <div className="mt-6 rounded-2xl border border-white/10 bg-white/5 p-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h1 className="text-2xl font-bold">상영장</h1>
          {state && (
            <span className="rounded-full border border-white/20 px-3 py-1 text-xs text-white/70">
              {state.status === 'PLAYING' ? '상영 중' : '대기/종료'}
            </span>
          )}
        </div>

        {loading && (
          <div className="mt-6 rounded-lg border border-white/10 bg-white/5 p-4 text-sm text-white/60">
            상영 정보를 불러오는 중입니다...
          </div>
        )}

        {error && (
          <div className="mt-6 rounded-lg border border-red-500/30 bg-red-500/10 p-4 text-sm text-red-200">
            {error}
          </div>
        )}

        {!loading && !error && playbackInfo && (
          <div className="mt-6 space-y-4">
            <div className="rounded-lg border border-white/10 bg-black/40 p-4 text-sm text-white/70">
              현재 상태: {state?.status ?? '알 수 없음'} · 재생 위치 {state?.positionMs ?? 0}ms
            </div>
            <video
              src={playbackInfo.videoUrl}
              controls
              className="w-full rounded-xl border border-white/10 bg-black"
            />
          </div>
        )}
      </div>
    </div>
  );
}


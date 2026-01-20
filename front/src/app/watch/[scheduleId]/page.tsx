'use client';

import { useEffect, useMemo, useRef, useState, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Client } from '@stomp/stompjs';
import Hls from 'hls.js';
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

type ChatMessage = {
  scheduleId: number;
  message: string;
  nickname: string;
};

type ScheduleInfoResponse = {
  scheduleItemId: number;
  contentId: number;
  contentTitle: string;
  startAt: string;
  endAt: string;
  status: 'WAITING' | 'PLAYING' | 'ENDING' | 'CLOSED';
};

export default function WatchPage() {
  const router = useRouter();
  const params = useParams();
  const scheduleId = useMemo(() => Number(params?.scheduleId), [params]);
  const { accessToken, user } = useAuthStore();
  const clientRef = useRef<Client | null>(null);
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const hlsRef = useRef<Hls | null>(null);
  const enteredSuccessRef = useRef(false);
  const endTimerRef = useRef<number | null>(null);
  const chatContainerRef = useRef<HTMLDivElement | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [playbackInfo, setPlaybackInfo] = useState<PlaybackInfoResponse | null>(null);
  const [state, setState] = useState<PlaybackStateResponse | null>(null);
  const [chatInput, setChatInput] = useState('');
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [endNotice, setEndNotice] = useState(false);
  const [viewerCount, setViewerCount] = useState<number | null>(null);
  const [viewMode, setViewMode] = useState<'theater' | 'full'>('theater');
  const [volume, setVolume] = useState(0.7);
  const [scheduleInfo, setScheduleInfo] = useState<ScheduleInfoResponse | null>(null);
  const [needsUserPlay, setNeedsUserPlay] = useState(false);
  const prevStatusRef = useRef<PlaybackStateResponse['status'] | null>(null);

  // HLS 초기화
  const initHls = useCallback((videoUrl: string) => {
    if (!videoRef.current) {
      console.warn('[WatchPage] Video ref not available');
      return;
    }

    const video = videoRef.current;
    console.log('[WatchPage] Initializing video with URL:', videoUrl);

    // 기존 HLS 인스턴스 정리
    if (hlsRef.current) {
      hlsRef.current.destroy();
      hlsRef.current = null;
    }

    // HLS 지원 체크 (.m3u8 파일인 경우)
    if (videoUrl.includes('.m3u8')) {
      if (Hls.isSupported()) {
        console.log('[WatchPage] HLS.js is supported, creating HLS instance');
        const hls = new Hls({
          enableWorker: true,
          lowLatencyMode: true,
        });
        hls.loadSource(videoUrl);
        hls.attachMedia(video);
        hls.on(Hls.Events.MANIFEST_PARSED, () => {
          console.log('[WatchPage] HLS manifest parsed');
          // 상태에 따라 자동 재생
          if (state?.status === 'PLAYING') {
            video.play().catch((err) => console.error('[WatchPage] Play error:', err));
          }
        });
        hls.on(Hls.Events.ERROR, (_, data) => {
          console.error('[WatchPage] HLS error:', data);
          if (data.fatal) {
            switch (data.type) {
              case Hls.ErrorTypes.NETWORK_ERROR:
                console.log('[WatchPage] Network error, retrying...');
                hls.startLoad();
                break;
              case Hls.ErrorTypes.MEDIA_ERROR:
                console.log('[WatchPage] Media error, recovering...');
                hls.recoverMediaError();
                break;
              default:
                console.error('[WatchPage] Fatal error, destroying HLS');
                hls.destroy();
                break;
            }
          }
        });
        hlsRef.current = hls;
      } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
        // Safari 네이티브 HLS 지원
        console.log('[WatchPage] Using native HLS support');
        video.src = videoUrl;
      } else {
        console.error('[WatchPage] HLS not supported');
      }
    } else {
      // 일반 비디오 파일
      console.log('[WatchPage] Setting video src directly');
      video.src = videoUrl;
    }
  }, [state?.status]);

  // 영상 위치 동기화
  const syncVideoPosition = useCallback((positionMs: number) => {
    if (!videoRef.current) return;
    const video = videoRef.current;
    const targetTime = positionMs / 1000;
    const currentTime = video.currentTime;

    // 3초 이상 차이가 나면 동기화
    if (Math.abs(currentTime - targetTime) > 3) {
      video.currentTime = targetTime;
    }
  }, []);

  useEffect(() => {
    if (!scheduleId || Number.isNaN(scheduleId)) {
      setError('잘못된 상영 일정입니다.');
      setLoading(false);
      return;
    }

    let mounted = true;
    let localEntered = false;

    const enterAndLoad = async () => {
      try {
        setLoading(true);
        setError('');
        await api.post(`/theaters/${scheduleId}/enter`);
        localEntered = true;
        enteredSuccessRef.current = true;
        const response = await api.get<ApiResponse<PlaybackInfoResponse>>(`/theaters/${scheduleId}/playback`);
        if (mounted) {
          console.log('[WatchPage] Playback API response:', response.data);
          const playbackData = response.data.data;
          console.log('[WatchPage] Playback data:', playbackData);
          if (!playbackData || !playbackData.videoUrl) {
            const errorMsg = response.data.message || '재생할 영상이 없습니다.';
            console.error('[WatchPage] No video URL:', errorMsg);
            setError(errorMsg);
            setPlaybackInfo(null);
          } else {
            console.log('[WatchPage] Setting playback info with URL:', playbackData.videoUrl);
            setPlaybackInfo(playbackData);
          }
        }
      } catch (err: any) {
        console.error('[WatchPage] Error loading playback info:', err);
        if (mounted) {
          setError(err.response?.data?.message || '상영 정보를 불러오지 못했습니다.');
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    };

    const fetchScheduleInfo = async () => {
      try {
        const { data } = await api.get<ApiResponse<ScheduleInfoResponse>>(`/schedules/${scheduleId}`);
        if (mounted) {
          setScheduleInfo(data.data ?? null);
        }
      } catch {
        if (mounted) {
          setScheduleInfo(null);
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
        // 초기 상태 구독 (SubscribeMapping)
        client.subscribe(`/app/theaters/${scheduleId}/state`, (message) => {
          try {
            const payload = JSON.parse(message.body) as PlaybackStateResponse;
            setState(payload);
          } catch {
            // ignore
          }
        });

        // 실시간 상태 브로드캐스트 구독 (/topic/)
        client.subscribe(`/topic/theaters/${scheduleId}/state`, (message) => {
          try {
            const payload = JSON.parse(message.body) as PlaybackStateResponse;
            setState(payload);
          } catch {
            // ignore
          }
        });

        // 채팅 구독
        client.subscribe(`/topic/theaters/${scheduleId}/chat`, (message) => {
          try {
            const payload = JSON.parse(message.body) as ChatMessage;
            setChatMessages((prev) => [...prev, payload]);
          } catch {
            // ignore
          }
        });
      };

      client.onStompError = () => {
        if (mounted) {
        setError('상영장 연결에 실패했습니다.');
        }
      };

      client.activate();
      clientRef.current = client;
    };

    // 항상 enter 시도 (ref 제거)
    enterAndLoad();
    fetchScheduleInfo();
    connectSocket();

    return () => {
      mounted = false;
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
      }
      if (hlsRef.current) {
        hlsRef.current.destroy();
        hlsRef.current = null;
      }
      if (endTimerRef.current) {
        window.clearTimeout(endTimerRef.current);
        endTimerRef.current = null;
      }
      // 이 effect에서 성공한 경우에만 leave 호출
      if (localEntered) {
      api.post(`/theaters/${scheduleId}/leave`).catch(() => {});
      }
    };
  }, [accessToken, scheduleId]);

  // 시청자 수 폴링
  useEffect(() => {
    if (!scheduleId || Number.isNaN(scheduleId)) return;
    let isMounted = true;

    const fetchViewers = async () => {
      try {
        const { data } = await api.get<ApiResponse<number>>(`/theaters/${scheduleId}/viewers`);
        if (isMounted) {
          setViewerCount(data.data ?? 0);
        }
      } catch {
        if (isMounted) {
          setViewerCount(null);
        }
      }
    };

    fetchViewers();
    const intervalId = window.setInterval(fetchViewers, 5000);

    return () => {
      isMounted = false;
      window.clearInterval(intervalId);
    };
  }, [scheduleId]);

  // HLS 초기화 (playbackInfo + 상태 확인 후)
  useEffect(() => {
    if (!playbackInfo?.videoUrl) {
      console.warn('[WatchPage] No video URL available:', playbackInfo);
      return;
    }
    if (!state) {
      return;
    }
    if (state.status === 'ENDING' || state.status === 'CLOSED') {
      return;
    }
    console.log('[WatchPage] Initializing HLS with URL:', playbackInfo.videoUrl);
    initHls(playbackInfo.videoUrl);
  }, [playbackInfo?.videoUrl, initHls, state]);

  // 상태 변경 시 영상 동기화 및 재생 제어
  useEffect(() => {
    if (!state) return;

    // 영상 위치 동기화 (늦게 입장해도 현재 시점으로 이동)
    if (state.positionMs >= 0) {
      syncVideoPosition(state.positionMs);
    }

    // 재생/일시정지 동기화
    if (videoRef.current) {
      // 자동재생 정책 대응: 음소거 상태에서 재생 시도
      videoRef.current.muted = true;
      if (state.playing && state.status === 'PLAYING') {
        videoRef.current.play().catch(() => {
          setNeedsUserPlay(true);
        });
      } else if (!state.playing || state.status === 'WAITING') {
        videoRef.current.pause();
      }
      // 배속 강제 1.0
      if (videoRef.current.playbackRate !== 1) {
        videoRef.current.playbackRate = 1;
      }
    }

    // 상태 변화 메시지 및 종료 처리
    if (prevStatusRef.current && prevStatusRef.current !== state.status) {
      const prev = prevStatusRef.current;
      if (prev === 'WAITING' && state.status === 'PLAYING') {
        setChatMessages((msgs) => [...msgs, { scheduleId, nickname: 'SYSTEM', message: '영화가 시작됩니다.' }]);
      } else if (prev === 'PLAYING' && state.status === 'ENDING') {
        setChatMessages((msgs) => [...msgs, { scheduleId, nickname: 'SYSTEM', message: '영화가 끝이났습니다. 10분 뒤 상영관이 닫힙니다.' }]);
      } else if (prev === 'ENDING' && state.status === 'CLOSED') {
        setChatMessages((msgs) => [...msgs, { scheduleId, nickname: 'SYSTEM', message: '상영관이 종료되었습니다.' }]);
      }
    }
    prevStatusRef.current = state.status;

    if (state.status === 'ENDING') {
      setEndNotice(true);
      if (!endTimerRef.current) {
        endTimerRef.current = window.setTimeout(() => {
          api.post(`/theaters/${scheduleId}/leave`).finally(() => {
            if (typeof window !== 'undefined') {
              sessionStorage.setItem('theaterEndedMessage', '상영이 종료되었습니다.');
            }
            router.push('/');
          });
        }, 10 * 60 * 1000);
      }
    }

    if (state.status === 'CLOSED') {
      if (typeof window !== 'undefined') {
        sessionStorage.setItem('theaterEndedMessage', '상영이 종료되었습니다.');
      }
      api.post(`/theaters/${scheduleId}/leave`).finally(() => {
        router.push('/');
      });
    }
  }, [router, scheduleId, state, syncVideoPosition]);

  const formatStartMessage = () => {
    if (!scheduleInfo?.startAt) return null;
    const date = new Date(scheduleInfo.startAt);
    if (Number.isNaN(date.getTime())) return null;
    const hh = String(date.getHours()).padStart(2, '0');
    const mm = String(date.getMinutes()).padStart(2, '0');
    return `${hh}시 ${mm}분 시작 예정 ${scheduleInfo.contentTitle}`;
  };

  // 사용자 조작(시킹/배속/재생) 제한
  const handleSeeking = useCallback(() => {
    if (!videoRef.current || !state) return;
    const targetTime = (state.positionMs ?? 0) / 1000;
    if (Math.abs(videoRef.current.currentTime - targetTime) > 1) {
      videoRef.current.currentTime = targetTime;
    }
  }, [state]);

  const handleRateChange = useCallback(() => {
    if (!videoRef.current) return;
    if (videoRef.current.playbackRate !== 1) {
      videoRef.current.playbackRate = 1;
    }
  }, []);

  const handlePlay = useCallback(() => {
    if (!videoRef.current || !state) return;
    if (state.status !== 'PLAYING') {
      videoRef.current.pause();
    }
  }, [state]);

  const handleUserPlay = useCallback(() => {
    if (!videoRef.current) return;
    setNeedsUserPlay(false);
    videoRef.current.muted = false;
    videoRef.current.play().catch(() => {
      // keep overlay if still blocked
      setNeedsUserPlay(true);
    });
  }, []);

  // 채팅 스크롤 자동 이동
  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
    }
  }, [chatMessages]);

  // 볼륨 동기화
  useEffect(() => {
    if (videoRef.current) {
      videoRef.current.volume = volume;
    }
  }, [volume]);

  const handleSendChat = useCallback(() => {
    if (!clientRef.current) return;
    const message = chatInput.trim();
    if (!message) return;
    clientRef.current.publish({
      destination: `/app/chat/${scheduleId}`,
      body: JSON.stringify({ message }),
    });
    setChatInput('');
  }, [chatInput, scheduleId]);

  // 엔터 키로 채팅 전송
  const handleChatKeyDown = useCallback((e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendChat();
    }
  }, [handleSendChat]);

  const isFullView = viewMode === 'full';
  const statusMessage = (() => {
    if (!state) return null;
    if (state.status === 'WAITING') {
      return formatStartMessage();
    }
    if (state.status === 'PLAYING') {
      return '영상 시작';
    }
    if (state.status === 'ENDING') {
      return scheduleInfo?.contentTitle
        ? `${scheduleInfo.contentTitle} 상영이 끝났습니다.`
        : '상영이 끝났습니다.';
    }
    if (state.status === 'CLOSED') {
      return scheduleInfo?.contentTitle
        ? `${scheduleInfo.contentTitle} 상영이 종료되었습니다.`
        : '상영이 종료되었습니다.';
    }
    return null;
  })();

  return (
    <div
      className={
        isFullView
          ? 'fixed inset-0 z-50 bg-black text-white'
          : 'mx-auto w-full max-w-[1800px] px-4 py-6 text-white'
      }
    >
      {!isFullView ? (
      <button
        type="button"
        onClick={() => router.back()}
        className="text-sm text-white/60 hover:text-white transition-colors"
      >
        ← 검색으로 돌아가기
      </button>
      ) : (
        <div className="absolute left-4 right-4 top-4 z-10 flex items-center justify-between">
          <button
            type="button"
            onClick={() => setViewMode('theater')}
            className="rounded-full border border-white/20 bg-black/60 px-3 py-1 text-xs font-semibold text-white/90 hover:bg-black/80"
          >
            상영관 모드로
          </button>
          <button
            type="button"
            onClick={() => {
              api.post(`/theaters/${scheduleId}/leave`).finally(() => {
                router.push('/');
              });
            }}
            className="rounded-full border border-white/20 bg-black/60 px-3 py-1 text-xs font-semibold text-white/90 hover:bg-black/80"
          >
            나가기
          </button>
        </div>
      )}

      <div className={isFullView ? 'h-full' : 'mt-4 rounded-2xl border border-white/10 bg-white/5 p-6'}>
        {!isFullView && (
        <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <h1 className="text-2xl font-bold">
                {scheduleInfo?.contentTitle ?? '상영장'}
              </h1>
          {state && (
                <span className={`rounded-full border px-3 py-1 text-xs ${
                  state.status === 'PLAYING'
                    ? 'border-emerald-500/40 bg-emerald-500/20 text-emerald-200'
                    : state.status === 'WAITING'
                      ? 'border-amber-500/40 bg-amber-500/20 text-amber-200'
                      : 'border-white/20 text-white/70'
                }`}>
                  {state.status === 'PLAYING' ? '상영 중' : state.status === 'WAITING' ? '대기 중' : '종료'}
            </span>
          )}
        </div>
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setViewMode('theater')}
                className={`rounded-full px-3 py-1 text-xs font-semibold transition-colors ${
                  viewMode === 'theater'
                    ? 'bg-white text-black'
                    : 'border border-white/20 text-white/70 hover:text-white'
                }`}
              >
                상영관 모드
              </button>
              <button
                type="button"
                onClick={() => setViewMode('full')}
                className={`rounded-full px-3 py-1 text-xs font-semibold transition-colors ${
                  isFullView
                    ? 'bg-white text-black'
                    : 'border border-white/20 text-white/70 hover:text-white'
                }`}
              >
                전체보기
              </button>
            <button
              type="button"
              onClick={() => {
                api.post(`/theaters/${scheduleId}/leave`).finally(() => {
                  router.push('/');
                });
              }}
              className="rounded-full border border-white/20 px-3 py-1 text-xs font-semibold text-white/70 hover:text-white"
            >
              나가기
            </button>
            </div>
          </div>
        )}

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

        {endNotice && (
          <div className="mt-6 rounded-lg border border-amber-500/30 bg-amber-500/10 p-4 text-sm text-amber-200">
            영상이 종료되었습니다. 10분 후 상영관이 종료되고 홈으로 이동합니다.
          </div>
        )}

        {!loading && !error && playbackInfo && (
          <div
            className={`mt-6 grid gap-6 ${
              viewMode === 'theater' ? 'lg:grid-cols-[8fr_2fr]' : 'grid-cols-1'
            } ${isFullView ? 'h-screen' : 'min-h-[70vh]'}`}
          >
            <div className="flex h-full flex-col space-y-4">
              {!isFullView && (
                <div className="rounded-lg border border-white/10 bg-black/40 p-4 text-sm text-white/70 flex items-center justify-between gap-3">
                  <span>
                    현재 상태: {state?.status ?? '알 수 없음'} · 재생 위치 {Math.floor((state?.positionMs ?? 0) / 1000)}초
                  </span>
                </div>
              )}
              <div className="relative flex-1">
                {statusMessage && (
                  <div className="absolute left-3 top-3 z-10 rounded-lg border border-white/20 bg-black/70 px-3 py-2 text-xs text-white/90">
                    {statusMessage}
            </div>
                )}
                {needsUserPlay && (
                  <button
                    type="button"
                    onClick={handleUserPlay}
                    className="absolute inset-0 z-20 flex items-center justify-center bg-black/70 text-sm font-semibold text-white"
                  >
                    재생하려면 클릭하세요
                  </button>
                )}
            <video
                  ref={videoRef}
                  playsInline
                  controls={false}
                  controlsList="nodownload noplaybackrate noremoteplayback"
                  disablePictureInPicture
                  onSeeking={handleSeeking}
                  onRateChange={handleRateChange}
                  onPlay={handlePlay}
                  className={
                    isFullView
                      ? 'h-full w-full bg-black'
                      : 'w-full rounded-xl border border-white/10 bg-black aspect-video'
                  }
                />
                <div className="absolute left-3 bottom-3 rounded-full border border-white/20 bg-black/60 px-3 py-1 text-xs text-white/90 flex items-center gap-2">
                  <span>음량</span>
                  <input
                    type="range"
                    min={0}
                    max={1}
                    step={0.01}
                    value={volume}
                    onChange={(e) => setVolume(Number(e.target.value))}
                    className="h-1 w-24 cursor-pointer"
                    aria-label="음량 조절"
                  />
                </div>
              </div>
            </div>
            {viewMode === 'theater' && (
              <div className="rounded-2xl border border-white/10 bg-white/5 p-4 flex h-full flex-col">
                <div className="flex items-center justify-between">
                  <h2 className="text-lg font-semibold">채팅</h2>
                  {viewerCount !== null && (
                    <span className="rounded-full border border-white/20 px-2.5 py-1 text-xs text-white/80">
                      시청자 {viewerCount}명
                    </span>
                  )}
                </div>
                <div
                  ref={chatContainerRef}
                  className="mt-4 flex-1 min-h-[60vh] overflow-y-auto rounded-lg border border-white/10 bg-black/40 p-3 text-sm text-white/80"
                >
                  {chatMessages.length === 0 && (
                    <p className="text-white/50">아직 메시지가 없습니다.</p>
                  )}
                {chatMessages.map((msg, index) => {
                  const isMine = !!user?.nickname && msg.nickname === user.nickname;
                  return (
                    <div
                      key={`${msg.nickname}-${index}`}
                      className={`mb-2 flex w-full ${isMine ? 'justify-end' : 'justify-start'}`}
                    >
                      <div
                        className={`max-w-[80%] rounded-lg px-3 py-2 text-sm ${
                          isMine
                            ? 'bg-red-500/20 text-red-100 border border-red-500/30 text-right'
                            : 'bg-white/10 text-white border border-white/10 text-left'
                        }`}
                      >
                        <div className="text-xs font-semibold text-white/70">{msg.nickname}</div>
                        <div className="text-white/90">{msg.message}</div>
                      </div>
                    </div>
                  );
                })}
                </div>
                <div className="mt-4 flex gap-2">
                  <input
                    value={chatInput}
                    onChange={(e) => setChatInput(e.target.value)}
                    onKeyDown={handleChatKeyDown}
                    placeholder="메시지를 입력하세요 (Enter로 전송)"
                    className="flex-1 rounded-md border border-white/10 bg-black/60 px-3 py-2 text-sm text-white placeholder:text-white/40"
            />
                  <button
                    type="button"
                    onClick={handleSendChat}
                  className="rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-500 whitespace-nowrap"
                  >
                    전송
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

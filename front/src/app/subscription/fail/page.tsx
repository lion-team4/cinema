'use client';

import { useEffect, useState, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';

// Next.js 13+ App Router에서 useSearchParams()를 사용할 때는 dynamic 렌더링 필요
export const dynamic = 'force-dynamic';

const defaultMessage =
  '카드 등록에 실패했습니다. 다른 카드로 다시 시도해주세요.';

function SubscriptionFailContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [message, setMessage] = useState(defaultMessage);
  const [code, setCode] = useState<string | null>(null);

  useEffect(() => {
    const codeParam = searchParams.get('code');
    const messageParam = searchParams.get('message');
    setCode(codeParam);
    if (messageParam) {
      setMessage(messageParam);
    }

    const timer = setTimeout(() => {
      router.push('/');
    }, 3500);

    return () => clearTimeout(timer);
  }, [router, searchParams]);

  return (
    <div className="mx-auto flex min-h-[60vh] max-w-lg flex-col items-center justify-center px-6 py-16 text-center text-white">
      <div className="text-red-400 text-5xl mb-4">✕</div>
      <h1 className="text-2xl font-bold">카드 등록 실패</h1>
      <p className="mt-3 text-sm text-white/70">{message}</p>
      {code && (
        <p className="mt-2 text-xs text-white/50">
          오류 코드: {code}
        </p>
      )}
      <button
        type="button"
        onClick={() => router.push('/')}
        className="mt-8 rounded-md bg-red-600 px-6 py-2 text-sm font-semibold text-white hover:bg-red-500 transition-colors"
      >
        홈으로 이동
      </button>
      <p className="mt-3 text-xs text-white/50">
        잠시 후 자동으로 홈으로 이동합니다.
      </p>
    </div>
  );
}

export default function SubscriptionFailPage() {
  return (
    <Suspense fallback={
      <div className="mx-auto flex min-h-[60vh] max-w-lg flex-col items-center justify-center px-6 py-16 text-center text-white">
        <div className="text-white/60">로딩 중...</div>
      </div>
    }>
      <SubscriptionFailContent />
    </Suspense>
  );
}


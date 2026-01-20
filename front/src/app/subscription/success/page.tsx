'use client';

import { useEffect, useState, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { ApiResponse } from '@/types';

// Next.js 13+ App Router에서 useSearchParams()를 사용할 때는 dynamic 렌더링 필요
export const dynamic = 'force-dynamic';

function SubscriptionSuccessContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [status, setStatus] = useState<'processing' | 'success' | 'error'>('processing');
  const [message, setMessage] = useState('구독 정보를 확인하고 있습니다...');

  useEffect(() => {
    const authKey = searchParams.get('authKey');
    const customerKey = searchParams.get('customerKey');

    if (!authKey) {
      setStatus('error');
      setMessage('인증 키가 유효하지 않습니다.');
      return;
    }

    const finalizeSubscription = async () => {
      try {
        // Based on FRONTEND_GUIDE: "Call POST /users/subscriptions with the key to finalize"
        // The backend might expect authKey to issue a billing key
        await api.post<ApiResponse<any>>('/users/subscriptions', {
          authKey,
          customerKey, // Toss might return this
        });
        
        setStatus('success');
        setMessage('프리미엄 구독이 시작되었습니다!');
        
        // Redirect after 3 seconds
        setTimeout(() => {
          router.push('/search');
        }, 3000);
      } catch (err: any) {
        console.error('Finalization failed', err);
        setStatus('error');
        setMessage(err.response?.data?.message || '구독 생성에 실패했습니다. 잠시 후 다시 시도해주세요.');
      }
    };

    finalizeSubscription();
  }, [searchParams, router]);

  return (
    <div className="max-w-md mx-auto py-20 px-4 text-center text-white">
      {status === 'processing' && (
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-600 mx-auto mb-4" />
      )}
      {status === 'success' && (
        <div className="text-green-400 text-5xl mb-4">✓</div>
      )}
      {status === 'error' && (
        <div className="text-red-400 text-5xl mb-4">✕</div>
      )}
      
      <h1 className="text-2xl font-bold mb-2">
        {status === 'success' ? '구독 완료' : status === 'error' ? '문제가 발생했어요' : '처리 중'}
      </h1>
      <p className="text-white/70">{message}</p>
      
      {status === 'success' && (
        <button 
          onClick={() => router.push('/search')}
          className="mt-8 px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-500 transition-colors"
        >
          콘텐츠 보러 가기
        </button>
      )}
    </div>
  );
}

export default function SubscriptionSuccessPage() {
  return (
    <Suspense fallback={
      <div className="max-w-md mx-auto py-20 px-4 text-center text-white">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-600 mx-auto mb-4" />
        <p className="text-white/70">처리 중...</p>
      </div>
    }>
      <SubscriptionSuccessContent />
    </Suspense>
  );
}

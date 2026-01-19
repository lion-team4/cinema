'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { api } from '@/lib/api';
import { ApiResponse } from '@/types';
import { useAuthStore } from '@/lib/store';
import axios from 'axios';
import SubscriptionGate from '@/components/guards/SubscriptionGate';

const formSchema = z.object({
  title: z.string().min(2, '제목은 2자 이상 입력해주세요.'),
  description: z.string().min(10, '설명은 10자 이상 입력해주세요.'),
});

export default function ContentCreatePage() {
  const router = useRouter();
  const { user } = useAuthStore();
  const [file, setFile] = useState<File | null>(null);
  const [posterFile, setPosterFile] = useState<File | null>(null);
  const [step, setStep] = useState<1 | 2>(1);
  const [contentId, setContentId] = useState<number | null>(null);
  const [createdTitle, setCreatedTitle] = useState('');
  const [createdDescription, setCreatedDescription] = useState('');
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const { register, handleSubmit, formState: { errors } } = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
  });

  const handleCreateContent = async (values: z.infer<typeof formSchema>) => {
    if (!user) {
      setError('콘텐츠 업로드는 로그인 후 이용할 수 있어요.');
      return;
    }

    setUploading(true);
    setError('');
    setNotice('');
    
    try {
      // 1. Create Content Draft & Get Content ID
      const { data } = await api.post<ApiResponse<{ content_id: number }>>('/contents', {
        title: values.title,
        description: values.description,
      });
      const createdId = data.data.content_id;
      setContentId(createdId);
      setCreatedTitle(values.title);
      setCreatedDescription(values.description);
      setStep(2);
    } catch (err: any) {
      console.error('Create content failed', err);
      setError(err.response?.data?.message || '콘텐츠 생성에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setUploading(false);
    }
  };

  const handleUploadAssets = async () => {
    if (!file) {
      setError('업로드할 영상 파일을 선택해주세요.');
      return;
    }
    if (!user || !contentId) {
      setError('콘텐츠 정보를 확인할 수 없습니다. 다시 시도해주세요.');
      return;
    }

    setUploading(true);
    setError('');
    setNotice('');

    try {
      const fileName = file.name;
      const contentType = file.type;

      // 1. (Optional) Upload Poster Image
      if (posterFile) {
        const posterPresign = await api.post<ApiResponse<{ uploadUrl: string; objectKey: string }>>(
          '/api/assets/presign',
          {
            fileName: posterFile.name,
            contentType: posterFile.type,
            assetType: 'POSTER_IMAGE',
            ownerUserId: user.userId,
            contentId,
          }
        );

        const { uploadUrl: posterUploadUrl, objectKey: posterObjectKey } = posterPresign.data.data;

        await axios.put(posterUploadUrl, posterFile, {
          headers: { 'Content-Type': posterFile.type },
        });

        await api.post('/api/assets/complete', {
          assetType: 'POSTER_IMAGE',
          ownerUserId: user.userId,
          contentId,
          objectKey: posterObjectKey,
          contentType: posterFile.type,
        });
      }

      // 2. Get Presigned URL (Video)
      const { data: presignData } = await api.post<ApiResponse<{ uploadUrl: string; objectKey: string }>>(
        '/api/assets/presign',
        {
          fileName,
          contentType,
          assetType: 'VIDEO_SOURCE',
          ownerUserId: user.userId,
          contentId,
        }
      );

      const { uploadUrl, objectKey } = presignData.data;

      // 3. Upload to S3 directly
      await axios.put(uploadUrl, file, {
        headers: { 'Content-Type': contentType },
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / (progressEvent.total || 1));
          setProgress(percentCompleted);
        },
      });

      // 4. Notify completion to start encoding
      await api.post('/api/assets/complete', {
        assetType: 'VIDEO_SOURCE',
        ownerUserId: user.userId,
        contentId,
        objectKey,
        contentType,
      });

      alert('업로드가 완료되었습니다. 인코딩이 진행 중입니다.');
      router.push(`/studio/contents/${contentId}/edit`);
    } catch (err: any) {
      console.error('Upload failed', err);
      setError(err.response?.data?.message || '업로드에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setUploading(false);
    }
  };

  const handleSaveDraft = async () => {
    if (!contentId) {
      setError('콘텐츠 정보를 확인할 수 없습니다. 다시 시도해주세요.');
      return;
    }
    try {
      setUploading(true);
      setError('');
      setNotice('');
      await api.put(`/contents/${contentId}`, {
        title: createdTitle,
        description: createdDescription,
        status: 'DRAFT',
        posterAssetId: null,
        videoSourceAssetId: null,
        videoHlsMasterAssetId: null,
      });
      setNotice('임시 저장되었습니다.');
      router.push('/contents/manage');
    } catch (err: any) {
      setError(err.response?.data?.message || '임시 저장에 실패했습니다.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <SubscriptionGate>
      <div className="mx-auto max-w-2xl py-12 px-4">
        <h1 className="text-3xl font-bold mb-2">새 콘텐츠 업로드</h1>
        <p className="text-sm text-white/60 mb-8">
          영상 파일을 업로드하면 자동으로 인코딩이 시작됩니다.
        </p>

      {step === 1 && (
        <form onSubmit={handleSubmit(handleCreateContent)} className="space-y-6">
          <div className="rounded-lg border border-white/10 bg-white/5 p-4 text-sm text-white/70">
            1단계: 제목과 설명을 입력하면 콘텐츠가 생성됩니다.
          </div>
        <div>
          <label className="block text-sm font-medium text-white/80 mb-1">제목</label>
          <input
            {...register('title')}
            className="w-full px-4 py-2 rounded-md border border-white/10 bg-white/5 text-white placeholder:text-white/40 focus:ring-red-500 focus:border-red-500"
            placeholder="영화 제목"
          />
          {errors.title && <p className="text-red-500 text-xs mt-1">{errors.title.message}</p>}
        </div>

        <div>
          <label className="block text-sm font-medium text-white/80 mb-1">설명</label>
          <textarea
            {...register('description')}
            rows={4}
            className="w-full px-4 py-2 rounded-md border border-white/10 bg-white/5 text-white placeholder:text-white/40 focus:ring-red-500 focus:border-red-500"
            placeholder="콘텐츠 설명을 입력해주세요."
          />
          {errors.description && <p className="text-red-500 text-xs mt-1">{errors.description.message}</p>}
        </div>

        {error && <p className="text-red-200 text-sm p-3 bg-red-500/10 rounded-md border border-red-500/30">{error}</p>}
        {notice && <p className="text-emerald-200 text-sm p-3 bg-emerald-500/10 rounded-md border border-emerald-500/30">{notice}</p>}

        <button
          type="submit"
          disabled={uploading}
          className="w-full py-3 bg-red-600 text-white font-bold rounded-md hover:bg-red-500 disabled:bg-white/20 transition-colors"
        >
          {uploading ? '처리 중...' : '다음 단계로'}
        </button>
        </form>
      )}

      {step === 2 && (
        <div className="space-y-6">
          <div className="rounded-lg border border-white/10 bg-white/5 p-4 text-sm text-white/70">
            2단계: 포스터와 영상 파일을 업로드하세요.
          </div>

          <div>
            <label className="block text-sm font-medium text-white/80 mb-1">포스터 이미지</label>
            <input
              type="file"
              accept="image/*"
              onChange={(e) => setPosterFile(e.target.files?.[0] || null)}
              className="w-full px-4 py-2 border border-dashed rounded-md cursor-pointer border-white/20 bg-white/5 text-white"
            />
            <p className="text-xs text-white/50 mt-1">권장: 2MB 이하의 JPG/PNG.</p>
          </div>

          <div>
            <label className="block text-sm font-medium text-white/80 mb-1">영상 파일 (MP4)</label>
            <input
              type="file"
              accept="video/mp4"
              onChange={(e) => setFile(e.target.files?.[0] || null)}
              className="w-full px-4 py-2 border border-dashed rounded-md cursor-pointer border-white/20 bg-white/5 text-white"
            />
            <p className="text-xs text-white/50 mt-1">
              권장: H.264 MP4, 최대 2GB.
            </p>
          </div>

          {uploading && (
            <div className="space-y-2">
              <div className="w-full bg-white/10 rounded-full h-2.5">
                <div
                  className="bg-red-600 h-2.5 rounded-full transition-all duration-300"
                  style={{ width: `${progress}%` }}
                />
              </div>
              <p className="text-sm text-center font-medium text-red-400">{progress}% 업로드됨</p>
            </div>
          )}

          {error && <p className="text-red-200 text-sm p-3 bg-red-500/10 rounded-md border border-red-500/30">{error}</p>}
          {notice && <p className="text-emerald-200 text-sm p-3 bg-emerald-500/10 rounded-md border border-emerald-500/30">{notice}</p>}

          <div className="flex flex-col gap-3 sm:flex-row">
            <button
              type="button"
              onClick={handleSaveDraft}
              disabled={uploading}
              className="flex-1 py-3 rounded-md border border-white/20 text-white/80 hover:border-white/60 hover:text-white disabled:opacity-60 transition-colors"
            >
              임시저장
            </button>
            <button
              type="button"
              onClick={handleUploadAssets}
              disabled={uploading}
              className="flex-1 py-3 bg-red-600 text-white font-bold rounded-md hover:bg-red-500 disabled:bg-white/20 transition-colors"
            >
              {uploading ? '처리 중...' : '업로드 시작'}
            </button>
          </div>
        </div>
      )}
    </div>
    </SubscriptionGate>
  );
}

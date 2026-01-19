'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import { type ApiResponse } from '@/types';
import axios from 'axios';

type ContentEditResponse = {
  contentId: number;
  title: string;
  description: string;
  posterAssetId: number | null;
  videoSourceAssetId: number | null;
  videoHlsMasterAssetId: number | null;
  status: 'PUBLISHED' | 'DRAFT' | 'PRIVATE';
};

export default function ContentEditPage() {
  const router = useRouter();
  const params = useParams();
  const { user, hasHydrated } = useAuthStore();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const [detail, setDetail] = useState<ContentEditResponse | null>(null);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<ContentEditResponse['status']>('DRAFT');
  const [posterFile, setPosterFile] = useState<File | null>(null);
  const [file, setFile] = useState<File | null>(null);
  const [progress, setProgress] = useState(0);

  const isAssetPending = detail?.videoSourceAssetId == null;

  useEffect(() => {
    if (!hasHydrated) return;
    if (!user) {
      router.push('/login?redirect=/contents/manage');
      return;
    }

    const contentId = Number(params?.contentId);
    if (!contentId) {
      setError('잘못된 콘텐츠 정보입니다.');
      setLoading(false);
      return;
    }

    const fetchDetail = async () => {
      try {
        setLoading(true);
        const { data } = await api.get<ApiResponse<ContentEditResponse>>(`/contents/${contentId}/edit`);
        const content = data.data;
        if (content) {
          setDetail(content);
          setTitle(content.title);
          setDescription(content.description);
          setStatus(content.status);
        }
      } catch (err: any) {
        setError(err.response?.data?.message || '콘텐츠 정보를 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchDetail();
  }, [hasHydrated, params, router, user]);

  const refreshDetail = async () => {
    if (!detail) return;
    const { data } = await api.get<ApiResponse<ContentEditResponse>>(`/contents/${detail.contentId}/edit`);
    if (data.data) {
      setDetail(data.data);
      setTitle(data.data.title);
      setDescription(data.data.description);
      setStatus(data.data.status);
    }
  };

  const handleSave = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    setNotice('');

    if (!detail) return;

    try {
      setSaving(true);
      await api.put(`/contents/${detail.contentId}`, {
        title,
        description,
        posterAssetId: detail.posterAssetId,
        videoSourceAssetId: detail.videoSourceAssetId,
        videoHlsMasterAssetId: detail.videoHlsMasterAssetId,
        status,
      });
      setNotice('콘텐츠 정보가 저장되었습니다.');
    } catch (err: any) {
      setError(err.response?.data?.message || '콘텐츠 저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!detail) return;
    const confirmed = window.confirm('정말 이 콘텐츠를 삭제하시겠어요?');
    if (!confirmed) return;

    try {
      setSaving(true);
      await api.delete(`/contents/${detail.contentId}`);
      router.push('/contents/manage');
    } catch (err: any) {
      setError(err.response?.data?.message || '콘텐츠 삭제에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleUploadAssets = async () => {
    if (!detail) return;
    if (!file) {
      setError('업로드할 영상 파일을 선택해주세요.');
      return;
    }
    setSaving(true);
    setError('');
    setNotice('');

    try {
      const fileName = file.name;
      const contentType = file.type;

      if (posterFile) {
        const posterPresign = await api.post<ApiResponse<{ uploadUrl: string; objectKey: string }>>(
          '/api/assets/presign',
          {
            fileName: posterFile.name,
            contentType: posterFile.type,
            assetType: 'POSTER_IMAGE',
            ownerUserId: user?.userId,
            contentId: detail.contentId,
          }
        );

        const { uploadUrl: posterUploadUrl, objectKey: posterObjectKey } = posterPresign.data.data;

        await axios.put(posterUploadUrl, posterFile, {
          headers: { 'Content-Type': posterFile.type },
        });

        await api.post('/api/assets/complete', {
          assetType: 'POSTER_IMAGE',
          ownerUserId: user?.userId,
          contentId: detail.contentId,
          objectKey: posterObjectKey,
          contentType: posterFile.type,
        });
      }

      const { data: presignData } = await api.post<ApiResponse<{ uploadUrl: string; objectKey: string }>>(
        '/api/assets/presign',
        {
          fileName,
          contentType,
          assetType: 'VIDEO_SOURCE',
          ownerUserId: user?.userId,
          contentId: detail.contentId,
        }
      );

      const { uploadUrl, objectKey } = presignData.data;

      await axios.put(uploadUrl, file, {
        headers: { 'Content-Type': contentType },
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / (progressEvent.total || 1));
          setProgress(percentCompleted);
        },
      });

      await api.post('/api/assets/complete', {
        assetType: 'VIDEO_SOURCE',
        ownerUserId: user?.userId,
        contentId: detail.contentId,
        objectKey,
        contentType,
      });

      setNotice('업로드가 완료되었습니다.');
      await refreshDetail();
    } catch (err: any) {
      setError(err.response?.data?.message || '업로드에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="mx-auto max-w-4xl px-6 py-12 text-white">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">콘텐츠 수정</h1>
        <a
          href="/contents/manage"
          className="text-sm text-white/60 hover:text-white transition-colors"
        >
          목록으로 돌아가기
        </a>
      </div>

      {loading && (
        <div className="mt-8 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/60">
          콘텐츠 정보를 불러오는 중입니다...
        </div>
      )}

      {!loading && error && (
        <div className="mt-8 rounded-lg border border-red-500/30 bg-red-500/10 p-6 text-sm text-red-200">
          {error}
        </div>
      )}

      {!loading && detail && isAssetPending && (
        <div className="mt-8 rounded-2xl border border-white/10 bg-white/5 p-6 space-y-6">
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

          {saving && (
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

          {notice && (
            <div className="rounded-md border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-200">
              {notice}
            </div>
          )}
          {error && (
            <div className="rounded-md border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
              {error}
            </div>
          )}

          <button
            type="button"
            onClick={handleUploadAssets}
            disabled={saving}
            className="w-full rounded-md bg-red-600 py-3 text-sm font-semibold text-white hover:bg-red-500 disabled:bg-white/20 transition-colors"
          >
            {saving ? '업로드 중...' : '업로드 시작'}
          </button>
        </div>
      )}

      {!loading && detail && !isAssetPending && (
        <form
          onSubmit={handleSave}
          className="mt-8 rounded-2xl border border-white/10 bg-white/5 p-6 space-y-6"
        >
          <div>
            <label className="block text-sm font-medium text-white/80 mb-1">제목</label>
            <input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full rounded-md border border-white/10 bg-white/5 px-4 py-2 text-white placeholder:text-white/40 focus:ring-red-500 focus:border-red-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-white/80 mb-1">설명</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={5}
              className="w-full rounded-md border border-white/10 bg-white/5 px-4 py-2 text-white placeholder:text-white/40 focus:ring-red-500 focus:border-red-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-white/80 mb-1">공개 상태</label>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value as ContentEditResponse['status'])}
              className="w-full rounded-md border border-white/10 bg-white/5 px-4 py-2 text-white focus:ring-red-500 focus:border-red-500"
            >
              <option value="PUBLISHED">공개</option>
              <option value="DRAFT">임시 저장</option>
              <option value="PRIVATE">비공개</option>
            </select>
          </div>

          {notice && (
            <div className="rounded-md border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-200">
              {notice}
            </div>
          )}
          {error && (
            <div className="rounded-md border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
              {error}
            </div>
          )}

          <div className="flex flex-col gap-3 sm:flex-row">
            <button
              type="submit"
              disabled={saving}
              className="flex-1 rounded-md bg-red-600 py-3 text-sm font-semibold text-white hover:bg-red-500 disabled:bg-white/20 transition-colors"
            >
              {saving ? '저장 중...' : '변경사항 저장'}
            </button>
            <button
              type="button"
              onClick={handleDelete}
              disabled={saving}
              className="flex-1 rounded-md border border-red-500/40 py-3 text-sm font-semibold text-red-200 hover:border-red-400 hover:text-red-100 disabled:opacity-60 transition-colors"
            >
              콘텐츠 삭제
            </button>
          </div>
        </form>
      )}
    </div>
  );
}


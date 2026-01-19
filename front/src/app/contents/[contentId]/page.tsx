\"use client\";

import { useEffect, useMemo, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import type { ApiResponse, PageResponse, ReviewListResponse } from '@/types';

type ContentDetailResponse = {
  contentId: number;
  title: string;
  description: string;
  status: 'PUBLISHED' | 'DRAFT' | 'PRIVATE';
  ownerNickname: string;
  posterUrl: string | null;
  videoUrl: string | null;
  durationMs: number | null;
  tags: string[];
};

export default function ContentDetailPage() {
  const router = useRouter();
  const params = useParams();
  const contentId = useMemo(() => Number(params?.contentId), [params]);
  const { user } = useAuthStore();

  const [detail, setDetail] = useState<ContentDetailResponse | null>(null);
  const [reviews, setReviews] = useState<ReviewListResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingRating, setEditingRating] = useState(5);
  const [editingComment, setEditingComment] = useState('');

  const loadReviews = async (id: number) => {
    const { data } = await api.get<ApiResponse<PageResponse<ReviewListResponse>>>(`/contents/reviews/search/${id}`, {
      params: { page: 0, size: 20 },
    });
    setReviews(data.data?.content ?? []);
  };

  useEffect(() => {
    if (!contentId || Number.isNaN(contentId)) {
      setError('잘못된 콘텐츠 정보입니다.');
      setLoading(false);
      return;
    }

    const fetchDetail = async () => {
      try {
        setLoading(true);
        setError('');
        const { data } = await api.get<ApiResponse<ContentDetailResponse>>(`/contents/${contentId}`);
        setDetail(data.data ?? null);
        await loadReviews(contentId);
      } catch (err: any) {
        setError(err.response?.data?.message || '콘텐츠 정보를 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchDetail();
  }, [contentId]);

  const handleCreateReview = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!contentId) return;
    if (!user) {
      router.push(`/login?redirect=/contents/${contentId}`);
      return;
    }

    try {
      setSubmitting(true);
      await api.post('/contents/reviews', {
        rating,
        comment: comment.trim() || null,
        'content-id': contentId,
      });
      setComment('');
      setRating(5);
      await loadReviews(contentId);
    } catch (err: any) {
      setError(err.response?.data?.message || '리뷰 작성에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (review: ReviewListResponse) => {
    setEditingId(review.reviewId);
    setEditingRating(review.rating);
    setEditingComment(review.comment ?? '');
  };

  const handleUpdateReview = async () => {
    if (!editingId) return;
    try {
      setSubmitting(true);
      await api.put(`/contents/reviews/${editingId}`, {
        rating: editingRating,
        comment: editingComment.trim() || null,
      });
      setEditingId(null);
      await loadReviews(contentId);
    } catch (err: any) {
      setError(err.response?.data?.message || '리뷰 수정에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteReview = async (reviewId: number) => {
    if (!contentId) return;
    if (!window.confirm('리뷰를 삭제하시겠습니까?')) return;
    try {
      setSubmitting(true);
      await api.delete(`/contents/reviews/${reviewId}`);
      await loadReviews(contentId);
    } catch (err: any) {
      setError(err.response?.data?.message || '리뷰 삭제에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-5xl px-6 py-12 text-white">
      <button
        type="button"
        onClick={() => router.back()}
        className="text-sm text-white/60 hover:text-white transition-colors"
      >
        ← 목록으로 돌아가기
      </button>

      {loading && (
        <div className="mt-8 rounded-lg border border-white/10 bg-white/5 p-6 text-sm text-white/60">
          콘텐츠 정보를 불러오는 중입니다...
        </div>
      )}

      {error && (
        <div className="mt-8 rounded-lg border border-red-500/30 bg-red-500/10 p-6 text-sm text-red-200">
          {error}
        </div>
      )}

      {!loading && detail && (
        <div className="mt-8 space-y-8">
          <div className="grid gap-6 lg:grid-cols-[1fr_2fr]">
            <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
              {detail.posterUrl ? (
                <img
                  src={detail.posterUrl}
                  alt={detail.title}
                  className="w-full rounded-xl border border-white/10 object-cover"
                />
              ) : (
                <div className="flex h-80 items-center justify-center rounded-xl border border-white/10 bg-black/40 text-sm text-white/50">
                  포스터 없음
                </div>
              )}
            </div>
            <div className="space-y-4">
              <div>
                <h1 className="text-3xl font-bold">{detail.title}</h1>
                <p className="mt-2 text-sm text-white/60">감독 {detail.ownerNickname}</p>
              </div>
              <p className="text-white/70">{detail.description}</p>
              {detail.tags.length > 0 && (
                <div className="flex flex-wrap gap-2">
                  {detail.tags.map((tag) => (
                    <span key={tag} className="rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs text-white/70">
                      #{tag}
                    </span>
                  ))}
                </div>
              )}
              <div className="flex flex-wrap gap-3 text-sm text-white/60">
                <span>상태: {detail.status}</span>
                {detail.durationMs ? (
                  <span>영상 길이: {Math.ceil(detail.durationMs / 60000)}분</span>
                ) : (
                  <span>영상 길이: 정보 없음</span>
                )}
              </div>
              <div className="flex flex-col gap-3 sm:flex-row">
                <a
                  href="/search"
                  className="inline-flex items-center justify-center rounded-md bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-500"
                >
                  상영 일정 찾기
                </a>
              </div>
            </div>
          </div>

          <section className="rounded-2xl border border-white/10 bg-white/5 p-6">
            <h2 className="text-xl font-semibold">리뷰</h2>

            <form onSubmit={handleCreateReview} className="mt-4 space-y-3">
              <div className="flex items-center gap-3">
                <label className="text-sm text-white/70">별점</label>
                <select
                  value={rating}
                  onChange={(e) => setRating(Number(e.target.value))}
                  className="rounded-md border border-white/10 bg-black px-3 py-2 text-sm text-white"
                >
                  {[5, 4, 3, 2, 1].map((value) => (
                    <option key={value} value={value} className="bg-black text-white">
                      {value}점
                    </option>
                  ))}
                </select>
              </div>
              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                rows={3}
                placeholder="리뷰를 작성해주세요."
                className="w-full rounded-md border border-white/10 bg-black/60 px-3 py-2 text-sm text-white placeholder:text-white/40"
              />
              <button
                type="submit"
                disabled={submitting}
                className="rounded-md bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-500 disabled:bg-white/20"
              >
                리뷰 작성
              </button>
            </form>

            <div className="mt-6 space-y-4">
              {reviews.length === 0 && (
                <p className="text-sm text-white/60">아직 리뷰가 없습니다.</p>
              )}
              {reviews.map((review) => (
                <div key={review.reviewId} className="rounded-lg border border-white/10 bg-black/40 p-4 text-sm">
                  <div className="flex items-center justify-between text-white/60">
                    <span>{review.writerNickname}</span>
                    <span>{new Date(review.createdAt).toLocaleString()}</span>
                  </div>
                  {editingId === review.reviewId ? (
                    <div className="mt-3 space-y-3">
                      <select
                        value={editingRating}
                        onChange={(e) => setEditingRating(Number(e.target.value))}
                        className="rounded-md border border-white/10 bg-black px-3 py-2 text-sm text-white"
                      >
                        {[5, 4, 3, 2, 1].map((value) => (
                          <option key={value} value={value} className="bg-black text-white">
                            {value}점
                          </option>
                        ))}
                      </select>
                      <textarea
                        value={editingComment}
                        onChange={(e) => setEditingComment(e.target.value)}
                        rows={3}
                        className="w-full rounded-md border border-white/10 bg-black/60 px-3 py-2 text-sm text-white placeholder:text-white/40"
                      />
                      <div className="flex gap-2">
                        <button
                          type="button"
                          onClick={handleUpdateReview}
                          disabled={submitting}
                          className="rounded-md bg-red-600 px-3 py-1 text-xs font-semibold text-white hover:bg-red-500 disabled:bg-white/20"
                        >
                          수정 저장
                        </button>
                        <button
                          type="button"
                          onClick={() => setEditingId(null)}
                          className="rounded-md border border-white/20 px-3 py-1 text-xs text-white/80 hover:border-white/60"
                        >
                          취소
                        </button>
                      </div>
                    </div>
                  ) : (
                    <>
                      <p className="mt-2 text-white/80">⭐ {review.rating}점</p>
                      <p className="mt-1 text-white/70">{review.comment || '코멘트 없음'}</p>
                      {user?.nickname === review.writerNickname && (
                        <div className="mt-3 flex gap-2">
                          <button
                            type="button"
                            onClick={() => handleEdit(review)}
                            className="rounded-md border border-white/20 px-3 py-1 text-xs text-white/80 hover:border-white/60"
                          >
                            수정
                          </button>
                          <button
                            type="button"
                            onClick={() => handleDeleteReview(review.reviewId)}
                            className="rounded-md border border-red-500/40 px-3 py-1 text-xs text-red-200 hover:border-red-400"
                          >
                            삭제
                          </button>
                        </div>
                      )}
                    </>
                  )}
                </div>
              ))}
            </div>
          </section>
        </div>
      )}
    </div>
  );
}

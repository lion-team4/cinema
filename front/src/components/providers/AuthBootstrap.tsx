'use client';

import { useEffect, useRef } from 'react';
import { useAuthStore } from '@/lib/store';
import { api } from '@/lib/api';
import { type ApiResponse } from '@/types';

type UserProfileResponse = {
  userId: number;
  email: string;
  nickname: string;
  profileImageUrl: string | null;
  seller: boolean;
};

export default function AuthBootstrap() {
  const { accessToken, user, setUser, hasHydrated } = useAuthStore();
  const attempted = useRef(false);

  useEffect(() => {
    if (!hasHydrated) return;
    if (attempted.current) return;
    if (!accessToken || user) return;

    attempted.current = true;

    api
      .get<ApiResponse<UserProfileResponse>>('/users/me', {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      })
      .then((response) => {
        const profile = response.data.data;
        if (!profile) return;
        setUser({
          userId: profile.userId,
          email: profile.email,
          nickname: profile.nickname,
          profileImage: profile.profileImageUrl ?? null,
          seller: profile.seller,
        });
      })
      .catch(() => {
        // Keep tokens; some routes can still work without profile.
      });
  }, [accessToken, hasHydrated, setUser, user]);

  return null;
}


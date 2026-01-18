import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authTokenStorage } from './auth';

interface UserProfile {
  userId: number;
  email: string;
  nickname: string;
  profileImage: string | null;
  seller: boolean;
}

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: UserProfile | null;
  hasHydrated: boolean;
  
  setAuth: (accessToken: string, refreshToken: string) => void;
  setUser: (user: UserProfile) => void;
  setHasHydrated: (value: boolean) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      hasHydrated: false,

      setAuth: (accessToken, refreshToken) => {
        authTokenStorage.setTokens(accessToken, refreshToken);
        set({ accessToken, refreshToken });
      },

      setUser: (user) => set({ user }),
      setHasHydrated: (value) => set({ hasHydrated: value }),

      logout: () => {
        authTokenStorage.clearTokens();
        set({ accessToken: null, refreshToken: null, user: null });
      },

      isAuthenticated: () => !!get().accessToken,
    }),
    {
      name: 'auth-storage', // name of the item in the storage (must be unique)
      partialize: (state) => ({ accessToken: state.accessToken, refreshToken: state.refreshToken, user: state.user }),
      onRehydrateStorage: () => (state) => {
        state?.setHasHydrated(true);
      },
    }
  )
);

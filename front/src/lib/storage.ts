const isBrowser = () => typeof window !== 'undefined';

export const storage = {
  get(key: string): string | null {
    if (!isBrowser()) return null;
    return window.localStorage.getItem(key);
  },
  set(key: string, value: string): void {
    if (!isBrowser()) return;
    window.localStorage.setItem(key, value);
  },
  remove(key: string): void {
    if (!isBrowser()) return;
    window.localStorage.removeItem(key);
  },
};


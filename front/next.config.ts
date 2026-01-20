import type { NextConfig } from "next";

const backendUrl =
  process.env.INTERNAL_API_URL ||
  process.env.NEXT_PUBLIC_API_URL ||
  "http://localhost:8080";

const nextConfig: NextConfig = {
  output: 'standalone', // Docker 이미지 크기 최적화를 위한 standalone 모드
  // TOSS_CLIENT_KEY를 클라이언트에서도 접근 가능하게 설정 (NEXT_PUBLIC_ 접두어 없이 사용 가능)
  env: {
    TOSS_CLIENT_KEY: process.env.TOSS_CLIENT_KEY,
  },
  // 주의: App Router에서는 next.config.ts의 i18n 설정을 사용할 수 없습니다.
  // i18n이 필요하면 middleware.ts를 사용하여 App Router 방식으로 구현해야 합니다.
  // 현재는 한국어만 사용하므로 i18n 설정을 제거했습니다.
  async rewrites() {
    return {
      beforeFiles: [
        { source: "/contents", destination: `${backendUrl}/contents` },
        { source: "/schedules", destination: `${backendUrl}/schedules` },
      ],
      afterFiles: [
        { source: "/auth/:path*", destination: `${backendUrl}/auth/:path*` },
        { source: "/users/:path*", destination: `${backendUrl}/users/:path*` },
        { source: "/contents/:path*", destination: `${backendUrl}/contents/:path*` },
        { source: "/api/:path*", destination: `${backendUrl}/api/:path*` },
        { source: "/media-assets/:path*", destination: `${backendUrl}/media-assets/:path*` },
        { source: "/schedules/:path*", destination: `${backendUrl}/schedules/:path*` },
        { source: "/ws/:path*", destination: `${backendUrl}/ws/:path*` },
        { source: "/ws-sockjs/:path*", destination: `${backendUrl}/ws-sockjs/:path*` },
        { source: "/theaters/:path*", destination: `${backendUrl}/theaters/:path*` },
        { source: "/settlements/:path*", destination: `${backendUrl}/settlements/:path*` },
        { source: "/admin/:path*", destination: `${backendUrl}/admin/:path*` },
      ],
    };
  },
};

export default nextConfig;

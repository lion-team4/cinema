import type { NextConfig } from "next";

const backendUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

const nextConfig: NextConfig = {
  i18n: {
    locales: ["ko", "en"],
    defaultLocale: "ko",
  },
  async rewrites() {
    return {
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

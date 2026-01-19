import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import QueryProvider from "@/components/providers/QueryProvider";
import AuthBootstrap from "@/components/providers/AuthBootstrap";
import SiteHeader from "@/components/layout/SiteHeader";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "방구석 영화관",
  description: "집에서도 극장처럼 즐기는 스트리밍",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased min-h-screen flex flex-col bg-black text-white`}
      >
        <QueryProvider>
          <AuthBootstrap />
          <SiteHeader />
          <main className="flex-grow">
            {children}
          </main>
          <footer className="border-t border-white/10 bg-black py-10">
            <div className="container mx-auto px-6 text-center text-sm text-white/50">
              © 2026 방구석 영화관. All rights reserved.
            </div>
          </footer>
        </QueryProvider>
      </body>
    </html>
  );
}

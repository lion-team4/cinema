export default function Home() {
  return (
    <div className="relative min-h-screen bg-black text-white">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(229,9,20,0.35),_transparent_55%)]" />
      <div className="absolute inset-0 bg-[linear-gradient(to_bottom,_rgba(0,0,0,0.1)_0%,_rgba(0,0,0,0.9)_70%,_rgba(0,0,0,1)_100%)]" />

      <main className="relative mx-auto flex min-h-screen max-w-6xl flex-col justify-center px-6 py-16">
        <div className="max-w-2xl">
          <p className="text-sm font-semibold uppercase tracking-[0.35em] text-white/60">
            Your private cinema
          </p>
          <h1 className="mt-4 text-5xl font-black leading-tight tracking-tight sm:text-6xl">
            방구석 영화관
          </h1>
          <p className="mt-6 text-lg leading-8 text-white/75">
            집에서 즐기는 극장급 경험. 인기작, 최신작, 프리미엄 콘텐츠를
            언제 어디서나 끊김 없이 감상하세요.
          </p>
          <div className="mt-8 flex flex-col gap-4 sm:flex-row">
            <a
              href="/subscription"
              className="inline-flex items-center justify-center rounded-md bg-red-600 px-6 py-3 text-sm font-semibold text-white hover:bg-red-500 transition-colors"
            >
              지금 구독하기
            </a>
            <a
              href="/contents/create"
              className="inline-flex items-center justify-center rounded-md border border-white/20 px-6 py-3 text-sm font-semibold text-white/80 hover:border-white/60 hover:text-white transition-colors"
            >
              콘텐츠 업로드
            </a>
          </div>
        </div>

        <section className="mt-16 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {[
            {
              title: "프리미엄 동시 시청",
              description: "친구들과 함께 보는 동기화 스트리밍.",
            },
            {
              title: "초고화질 HLS",
              description: "끊김 없는 고화질 재생과 빠른 로딩.",
            },
            {
              title: "추천 큐레이션",
              description: "취향에 맞춘 개인화 콘텐츠 제안.",
            },
          ].map((item) => (
            <div
              key={item.title}
              className="rounded-xl border border-white/10 bg-white/5 p-6 backdrop-blur"
            >
              <h3 className="text-lg font-semibold">{item.title}</h3>
              <p className="mt-2 text-sm text-white/70">{item.description}</p>
            </div>
          ))}
        </section>
      </main>
    </div>
  );
}

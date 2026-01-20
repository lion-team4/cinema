export default function NotFound() {
  return (
    <div className="min-h-screen bg-black text-white flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-4xl font-bold mb-4">404</h1>
        <p className="text-white/60 mb-8">페이지를 찾을 수 없습니다.</p>
        <a
          href="/"
          className="inline-block px-6 py-3 bg-red-600 text-white rounded-md hover:bg-red-500 transition-colors"
        >
          홈으로 이동
        </a>
      </div>
    </div>
  );
}


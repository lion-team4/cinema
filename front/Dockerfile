# 1단계: 빌드 단계
FROM node:20-alpine AS builder
WORKDIR /app

ARG NEXT_PUBLIC_API_URL
ARG INTERNAL_API_URL
ARG TOSS_CLIENT_KEY

ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
ENV INTERNAL_API_URL=${INTERNAL_API_URL}
ENV TOSS_CLIENT_KEY=${TOSS_CLIENT_KEY}

RUN echo "Building with API URL: $NEXT_PUBLIC_API_URL" \
  && echo "Building with INTERNAL_API_URL: $INTERNAL_API_URL" \
  && echo "Building with TOSS_CLIENT_KEY provided: $(if [ -n "$TOSS_CLIENT_KEY" ]; then echo "Yes"; else echo "No"; fi)"

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 2단계: 실행 단계 (Next.js standalone 모드)
# standalone 모드를 사용하면 필요한 파일만 복사하여 이미지 크기를 80% 이상 줄일 수 있습니다.
FROM node:20-alpine
WORKDIR /app

ENV NODE_ENV production

# standalone 모드에서 생성된 최소한의 파일만 복사
# .next/standalone 폴더에는 server.js와 필요한 node_modules만 포함됨
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

EXPOSE 3000

# standalone 모드에서는 .next/standalone/server.js를 실행
# WORKDIR이 /app이므로 server.js는 /app/server.js에 위치
CMD ["node", "server.js"]


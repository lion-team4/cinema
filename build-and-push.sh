#!/bin/bash

# Docker 이미지 빌드 및 Docker Hub 푸시 스크립트
# 사용법: ./build-and-push.sh [API_URL]

set -e

# 색상 출력
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 환경 변수 우선순위 결정
# 1. 스크립트 인자 ($1)
# 2. 환경 변수 NEXT_PUBLIC_API_URL
# 3. 기본값 http://localhost:8080
API_URL=${1:-${NEXT_PUBLIC_API_URL:-"http://localhost:8080"}}
if [ -n "$NEXT_PUBLIC_WS_URL" ]; then
    WS_URL=$NEXT_PUBLIC_WS_URL
else
    if [[ "$API_URL" == https:* ]]; then
        WS_URL=${API_URL/https:/wss:}
    else
        WS_URL=${API_URL/http:/ws:}
    fi
    WS_URL=${WS_URL%/}
    WS_URL="${WS_URL}/ws"
fi
# 프론트엔드용 토스 키 (NEXT_PUBLIC_TOSS_CLIENT_KEY 우선, 없으면 TOSS_CLIENT_KEY 사용)
FRONT_TOSS_KEY=${NEXT_PUBLIC_TOSS_CLIENT_KEY:-${TOSS_CLIENT_KEY}}

echo -e "${GREEN}=== Docker 이미지 빌드 및 푸시 ===${NC}"
echo -e "API URL: ${YELLOW}${API_URL}${NC}"
echo -e "Toss Client Key: ${YELLOW}${FRONT_TOSS_KEY:-(미설정)}${NC}"
echo -e "WS URL: ${YELLOW}${WS_URL}${NC}"

# Docker Hub 로그인 확인
echo -e "\n${GREEN}[0/4] Docker Hub 로그인 확인 중...${NC}"
if ! docker info | grep -q "Username"; then
    echo -e "${YELLOW}Docker Hub에 로그인하세요.${NC}"
    docker login
fi

# 백엔드 Docker 이미지 빌드
echo -e "\n${GREEN}[1/4] 백엔드 Docker 이미지 빌드 중...${NC}"
cd back/cinema
docker build --platform linux/amd64 -t jeongbeomgyu/cinema-backend:latest .
cd ../..

# 프론트엔드 Docker 이미지 빌드
echo -e "\n${GREEN}[2/4] 프론트엔드 Docker 이미지 빌드 중...${NC}"

# TOSS_CLIENT_KEY가 설정되어 있는지 확인
if [ -z "$TOSS_CLIENT_KEY" ]; then
    echo -e "${RED}Error: TOSS_CLIENT_KEY 환경 변수가 설정되지 않았습니다.${NC}"
    echo -e "구독 결제 기능을 위해 반드시 필요합니다."
fi

cd front
docker build --platform linux/amd64 \
  --build-arg NEXT_PUBLIC_API_URL=${API_URL} \
  --build-arg NEXT_PUBLIC_WS_URL=${WS_URL} \
  --build-arg TOSS_CLIENT_KEY=${TOSS_CLIENT_KEY} \
  -t jeongbeomgyu/cinema-frontend:latest .
cd ..

# Docker Hub에 푸시
echo -e "\n${GREEN}[3/4] Docker Hub에 푸시 중...${NC}"
docker push jeongbeomgyu/cinema-backend:latest
docker push jeongbeomgyu/cinema-frontend:latest

echo -e "\n${GREEN}[4/4] 완료!${NC}"
echo -e "${GREEN}=== 빌드 및 푸시 완료 ===${NC}"
echo -e "이제 EC2에서 'docker-compose pull && docker-compose up -d'를 실행하세요."


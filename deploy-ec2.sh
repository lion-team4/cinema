#!/bin/bash

# EC2 내부에서 실행하는 배포 스크립트
# EC2 서버에 직접 접속해서 실행하거나, deploy.sh에서 자동으로 실행됩니다.

set -e

# 색상 출력
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== EC2 내부 배포 시작 ===${NC}"

# 현재 작업 디렉토리 확인 (docker-compose.yml과 .env가 같은 위치에 있어야 함)
CURRENT_DIR=$(pwd)
echo -e "${GREEN}작업 디렉토리: ${CURRENT_DIR}${NC}"

# docker-compose.yml 파일 확인
if [ ! -f docker-compose.yml ]; then
    echo -e "${RED}오류: docker-compose.yml 파일을 찾을 수 없습니다.${NC}"
    echo -e "${RED}현재 디렉토리: ${CURRENT_DIR}${NC}"
    exit 1
fi

# .env 파일 확인 (docker-compose.yml과 같은 위치에 있어야 함)
if [ ! -f .env ]; then
    echo -e "${YELLOW}경고: .env 파일이 없습니다.${NC}"
    echo -e "${YELLOW}현재 디렉토리: ${CURRENT_DIR}${NC}"
    echo -e "${YELLOW}환경 변수를 export하거나 .env 파일을 생성해주세요.${NC}"
    echo -e "${YELLOW}GitHub Actions를 사용하는 경우 자동으로 생성됩니다.${NC}"
    echo -e "${YELLOW}docker-compose.yml과 같은 위치에 .env 파일이 있어야 합니다.${NC}"
else
    echo -e "${GREEN}.env 파일 확인 완료 (${CURRENT_DIR}/.env)${NC}"
fi

# Docker Hub에서 이미지 가져오기
echo -e "\n${GREEN}[1/3] Docker Hub에서 이미지 가져오는 중...${NC}"
docker-compose pull

# 기존 컨테이너 중지 및 제거
echo -e "\n${GREEN}[2/3] 기존 컨테이너 중지 및 제거 중...${NC}"
docker-compose down

# 새 컨테이너 시작 (Docker Hub에서 받은 이미지 사용)
echo -e "\n${GREEN}[3/3] 새 컨테이너 시작 중...${NC}"
docker-compose up -d

# 컨테이너 상태 확인
echo -e "\n${GREEN}컨테이너 상태:${NC}"
docker-compose ps

# 로그 확인
echo -e "\n${YELLOW}최근 로그 (10줄):${NC}"
docker-compose logs --tail=10

echo -e "\n${GREEN}=== 배포 완료 ===${NC}"


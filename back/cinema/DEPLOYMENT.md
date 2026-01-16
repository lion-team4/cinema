# 배포 가이드

## 배포 시 수정해야 할 부분

### 1. CORS 설정

#### 백엔드 (AppMvcConfigurer.java)
현재는 환경 변수로 관리되도록 설정되어 있습니다.

**개발 환경 (application.yaml)**
```yaml
cors:
  allowed-origins: http://localhost:3000,http://127.0.0.1:3000
```

**프로덕션 환경 (application-prod.yaml 또는 환경 변수)**
```yaml
cors:
  allowed-origins: https://your-frontend-domain.com
```

또는 환경 변수로 설정:
```bash
export CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
```

### 2. 데이터베이스 설정

**환경 변수로 설정:**
```bash
export DB_URL=jdbc:mysql://your-db-host:3306/cinema-db
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
```

또는 `application-prod.yaml`에서 설정:
```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### 3. JWT Secret Key

**절대 하드코딩하지 마세요!** 환경 변수로 설정:
```bash
export JWT_SECRET=your-very-long-secret-key-here
```

### 4. AWS 설정

**환경 변수:**
```bash
export AWS_REGION=ap-northeast-2
export AWS_S3_BUCKET=your-bucket-name
export AWS_CLOUDFRONT_DOMAIN=your-cloudfront-domain.cloudfront.net
```

### 5. Toss Payments 설정

**환경 변수:**
```bash
export TOSS_CLIENT_KEY=your-client-key
export TOSS_SECRET_KEY=your-secret-key
```

### 6. Spring Profile 설정

**프로덕션 환경에서:**
```bash
export SPRING_PROFILES_ACTIVE=prod
```

또는 JAR 실행 시:
```bash
java -jar cinema-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 7. 프론트엔드 설정

프론트엔드의 `.env` 파일 또는 환경 변수:
```bash
VITE_API_BASE_URL=https://your-backend-api.com
VITE_WS_BASE_URL=wss://your-backend-api.com
```

## 배포 체크리스트

- [ ] CORS allowed-origins를 실제 프론트엔드 도메인으로 변경
- [ ] 데이터베이스 연결 정보 설정
- [ ] JWT_SECRET 환경 변수 설정 (강력한 키 사용)
- [ ] AWS 자격 증명 설정 (IAM 역할 또는 액세스 키)
- [ ] Toss Payments 키 설정
- [ ] Spring Profile을 `prod`로 설정
- [ ] 로깅 레벨 조정 (프로덕션에서는 info 이상)
- [ ] Hibernate ddl-auto를 `validate`로 변경
- [ ] 프론트엔드 API_BASE_URL 설정
- [ ] HTTPS 사용 (프로덕션)
- [ ] 보안 헤더 설정 (필요시)

## 환경 변수 예시

```bash
# 데이터베이스
export DB_URL=jdbc:mysql://db.example.com:3306/cinema-db
export DB_USERNAME=cinema_user
export DB_PASSWORD=secure_password

# JWT
export JWT_SECRET=your-very-long-and-secure-secret-key-minimum-512-bits

# CORS
export CORS_ALLOWED_ORIGINS=https://cinema.example.com

# AWS
export AWS_REGION=ap-northeast-2
export AWS_S3_BUCKET=cinema-content-prod
export AWS_CLOUDFRONT_DOMAIN=d1234567890.cloudfront.net

# Toss Payments
export TOSS_CLIENT_KEY=live_ck_xxxxx
export TOSS_SECRET_KEY=live_sk_xxxxx

# Spring Profile
export SPRING_PROFILES_ACTIVE=prod
```

## 주의사항

1. **절대 민감한 정보를 코드에 하드코딩하지 마세요**
2. **프로덕션에서는 `ddl-auto: validate` 사용** (데이터 손실 방지)
3. **CORS는 최소한의 도메인만 허용** (보안)
4. **HTTPS 사용 필수** (프로덕션)
5. **로깅 레벨 조정** (성능 및 보안)


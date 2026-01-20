# Project Context: Cinema / Streaming Platform

## Overview
This is a full-stack cinema and streaming platform application. It features user authentication, subscription management (via Toss Payments), content management (video upload to S3, encoding), scheduling of content, and real-time theater experiences using WebSockets.

## Tech Stack

### Frontend (`/front`)
*   **Framework:** Next.js 16.1.3 (App Router)
*   **Language:** TypeScript
*   **Styling:** Tailwind CSS v4
*   **State Management:** Zustand, React Query
*   **Key Libraries:** `@tosspayments/payment-widget-sdk`, `hls.js`, `lucide-react`, `axios`

### Backend (`/back/cinema`)
*   **Framework:** Spring Boot 3.5.9
*   **Language:** Java 21
*   **Database:** MySQL 8.0 (JPA, QueryDSL)
*   **Security:** Spring Security, JWT
*   **Infrastructure:** AWS SDK v2 (S3, CloudFront), Spring Batch
*   **Key Features:** WebSocket (Stomp), Swagger (SpringDoc), FFmpeg (implied for encoding)

### Infrastructure
*   **Containerization:** Docker, Docker Compose
*   **Database:** MySQL 8.0 container
*   **Deployment:** Docker Hub images (`jeongbeomgyu/cinema-backend`, `jeongbeomgyu/cinema-frontend`), EC2 scripts

## Architecture & Features
*   **Authentication:** Custom JWT implementation with login, signup, reissue, and logout.
*   **Content:** Movie/Video management with S3 upload presigning and processing.
*   **Subscription:** Payment integration with Toss Payments.
*   **Theaters:** Real-time synchronized viewing experience using WebSockets.
*   **Scheduling:** Managing when content is available or "screened".

## Development Workflow

### Prerequisites
*   Java 21
*   Node.js (v20+)
*   Docker & Docker Compose

### Running Locally (Full Stack)
The project is set up to run via Docker Compose for a complete environment.

```bash
# Start all services (MySQL, Backend, Frontend)
docker-compose up -d
```

### Running Backend Independently
```bash
cd back/cinema
./gradlew bootRun
```
*   **Configuration:** `src/main/resources/application.yaml` (dev) and `application-prod.yaml` (prod).
*   **API Docs:** Swagger UI is likely available at `/swagger-ui/index.html` (port 8080) when running.

### Running Frontend Independently
```bash
cd front
npm install
npm run dev
```
*   **Port:** 3000
*   **Env:** `NEXT_PUBLIC_API_URL` should point to the backend (default `http://localhost:8080`).

## Build & Deployment
*   **Scripts:**
    *   `./build-and-push.sh [API_URL]`: Builds Docker images for front/back and pushes to Docker Hub.
    *   `./deploy-ec2.sh`: Likely handles pulling and restarting containers on EC2.
*   **CI/CD:** GitHub Actions workflow in `.github/workflows/deploy.yml`.

## Key Documentation Files
*   **Backend Specs:**
    *   `back/cinema/API.md`: Detailed API endpoint definitions.
    *   `back/cinema/ENTITY.md`: Database entity and schema details.
    *   `back/cinema/DTO_SPEC.md`, `EXCEPTION_SPEC.md`: Data transfer object and exception standards.
    *   `back/cinema/DEPLOYMENT.md`: Detailed deployment configuration and env var guide.
*   **Frontend:**
    *   `front/FRONTEND_FLOW.md`: Frontend logic and user flows (check content).

-- 1. 테스트 유저 생성 (User ID: 6, Email: mmy428@naver.com)
-- 비밀번호 해시는 임시 값입니다. 실제 로그인 시에는 JWT 토큰 생성 로직에 따라 다를 수 있습니다.
INSERT INTO users (user_id, email, nickname, password_hash, seller, created_at, updated_at, deleted_at)
VALUES (6, 'mmy428@naver.com', 'ReviewTester', '$2a$10$DUMMYHASHFOREXAMPLEONLY', false, NOW(), NOW(), NULL);

-- 2. 콘텐츠 소유자 생성 (User ID: 999)
INSERT INTO users (user_id, email, nickname, password_hash, seller, created_at, updated_at, deleted_at)
VALUES (999, 'owner@cinema.com', 'MovieDirector', '$2a$10$DUMMYHASH', true, NOW(), NOW(), NULL);

-- 3. 테스트용 콘텐츠 생성 (Content ID: 101)
INSERT INTO contents (content_id, owner_user_id, title, description, status, total_view, month_view, created_at, updated_at)
VALUES (101, 999, '리뷰 테스트용 영화', '이 영화는 리뷰 테스트를 위해 제작되었습니다.', 'PUBLISHED', 100, 10, NOW(), NOW());

-- 4. 시청 기록 생성 (WatchHistory)
-- *중요*: 리뷰를 작성하려면 'view_counted'가 true여야 합니다. (실제 시청 인정)
INSERT INTO watch_histories (watch_history_id, user_id, content_id, view_counted, last_viewed_at, created_at, updated_at)
VALUES (1, 6, 101, true, NOW(), NOW(), NOW());

-- 5. (선택) 수정/삭제 테스트용 기존 리뷰 생성
-- User 6이 Content 101에 이미 남긴 리뷰가 있다고 가정할 경우 주석 해제하여 사용
/*
INSERT INTO reviews (review_id, user_id, content_id, comment, rating, created_at, updated_at)
VALUES (1, 6, 101, '이미 작성된 리뷰입니다.', 5, NOW(), NOW());
*/

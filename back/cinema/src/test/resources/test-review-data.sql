-- 시청 기록 및 리뷰 테스트 데이터
-- 전제 조건: test-content-data.sql 및 test-schedule-data.sql이 먼저 실행되어야 합니다.

USE `cinema-db`;

SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터 삭제 및 ID 초기화
TRUNCATE TABLE reviews;
TRUNCATE TABLE watch_histories;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. 시청 기록(WatchHistory) 데이터 삽입
-- User 2 (MovieLover)가 여러 영화를 시청한 기록
-- view_counted = true (1시간 이상 시청) 인 기록들 위주로 생성
INSERT INTO watch_histories (user_id, schedule_item_id, enter_at, left_at, view_counted, created_at, updated_at) VALUES
-- Content 1 (Inception of Dreams) 시청 기록 2개 (리뷰 2개 작성 가능 상태)
(2, 1, '2026-01-19 10:00:00', '2026-01-19 12:00:00', true, '2026-01-19 12:00:00', '2026-01-19 12:00:00'), -- watch_id=1
(2, 2, '2026-01-19 14:00:00', '2026-01-19 16:00:00', true, '2026-01-19 16:00:00', '2026-01-19 16:00:00'), -- watch_id=2

-- Content 5 (Avengers) 시청 기록 1개 (리뷰 1개 작성 가능)
(2, 7, '2026-01-19 11:00:00', '2026-01-19 13:00:00', true, '2026-01-19 13:00:00', '2026-01-19 13:00:00'), -- watch_id=3

-- Content 3 (Interstellar) 시청 기록 1개 (단, view_counted=false -> 리뷰 작성 불가 대상)
(2, 16, '2026-01-19 09:00:00', '2026-01-19 09:10:00', false, '2026-01-19 09:10:00', '2026-01-19 09:10:00'); -- watch_id=4


-- 2. 일부 시청 기록에 대해 이미 작성된 리뷰 데이터 삽입
-- Content 1의 첫 번째 시청 기록(watch_id=1)에 대해 리뷰 작성 완료
-- Content 1에 대해서는 이제 watch_id=2 기록을 통해 1개의 리뷰를 더 쓸 수 있는 상태가 됨
INSERT INTO reviews (content_id, user_id, watch_id, rating, comment, created_at, updated_at) VALUES
(1, 2, 1, 5, '인셉션은 정말 다시 봐도 명작이네요. 꿈 속의 꿈!', '2026-01-19 13:00:00', '2026-01-19 13:00:00');

-- 3. 다른 유저(User 3)의 시청 기록
INSERT INTO watch_histories (user_id, schedule_item_id, enter_at, left_at, view_counted, created_at, updated_at) VALUES
(3, 1, '2026-01-19 10:00:00', '2026-01-19 12:00:00', true, '2026-01-19 12:00:00', '2026-01-19 12:00:00'); -- watch_id=5

INSERT INTO reviews (content_id, user_id, watch_id, rating, comment, created_at, updated_at) VALUES
(1, 3, 5, 4, '복잡하지만 몰입감이 대단합니다.', '2026-01-19 13:30:00', '2026-01-19 13:30:00');

SET SQL_SAFE_UPDATES = 1;
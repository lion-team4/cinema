-- =============================================
-- 1. 과거 상영 일정 생성 (1/15)
-- =============================================

-- 유저 5의 콘텐츠(ID: 51) 상영 일정
INSERT INTO schedule_days (schedule_day_id, content_id, schedule_date, is_locked, created_at, updated_at)
VALUES (501, 51, '2026-01-15', true, NOW(), NOW());

INSERT INTO schedule_items (schedule_item_id, schedule_day_id, content_id, start_at, end_at, status, created_at, updated_at)
VALUES (5001, 501, 51, '2026-01-15 14:00:00', '2026-01-15 16:00:00', 'CLOSED', NOW(), NOW());

-- 유저 6의 콘텐츠(ID: 61) 상영 일정
INSERT INTO schedule_days (schedule_day_id, content_id, schedule_date, is_locked, created_at, updated_at)
VALUES (601, 61, '2026-01-15', true, NOW(), NOW());

INSERT INTO schedule_items (schedule_item_id, schedule_day_id, content_id, start_at, end_at, status, created_at, updated_at)
VALUES (6001, 601, 61, '2026-01-15 20:00:00', '2026-01-15 22:00:00', 'CLOSED', NOW(), NOW());


-- =============================================
-- 2. 시청 기록 생성 (유저 5, 6 서로 품앗이)
-- =============================================

-- 유저 6이 유저 5의 영화(51)를 시청 (리뷰 없음 -> 생성 테스트용)
INSERT INTO watch_histories (watch_id, user_id, schedule_item_id, enter_at, left_at, view_counted, created_at, updated_at)
VALUES (1001, 6, 5001, '2026-01-15 14:00:00', '2026-01-15 16:00:00', true, NOW(), NOW());

-- 유저 5가 유저 6의 영화(61)를 시청 (리뷰 존재함 -> 수정/삭제 테스트용)
INSERT INTO watch_histories (watch_id, user_id, schedule_item_id, enter_at, left_at, view_counted, created_at, updated_at)
VALUES (1002, 5, 6001, '2026-01-15 20:00:00', '2026-01-15 22:00:00', true, NOW(), NOW());


-- =============================================
-- 3. 기존 리뷰 생성 (유저 5의 리뷰)
-- =============================================

-- 유저 5가 유저 6의 영화(61)에 남긴 리뷰
INSERT INTO reviews (review_id, content_id, user_id, watch_id, rating, comment, created_at, updated_at)
VALUES (101, 61, 5, 1002, 5, '자연의 신비로움이 느껴지는 명작입니다!', NOW(), NOW());

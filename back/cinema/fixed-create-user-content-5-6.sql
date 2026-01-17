-- =============================================
-- 0. Create Users 5 and 6
-- =============================================
INSERT INTO users (user_id, email, nickname, password_hash, seller, created_at, updated_at) VALUES 
(5, 'user5@example.com', 'DrStrange', '{noop}password1234', true, NOW(), NOW()),
(6, 'user6@example.com', 'NatureLover', '{noop}password1234', true, NOW(), NOW());

-- =============================================
-- 1. User 5 Data (Dr. Strange Style)
-- =============================================

-- 1-1. Media Assets for User 5
INSERT INTO media_assets (asset_id, owner_user_id, asset_type, bucket, object_key, content_type, visibility, size_bytes, duration_ms, created_at, updated_at)
VALUES 
(501, 5, 'POSTER_IMAGE',     'lion-cinema-bucket', 'posters/user5_poster.jpg', 'image/jpeg', 'PUBLIC', 102400, null, NOW(), NOW()),
(502, 5, 'VIDEO_SOURCE',     'lion-cinema-bucket', 'videos/user5_source.mp4',  'video/mp4',  'PRIVATE', 524288000, 7200000, NOW(), NOW()),
(503, 5, 'VIDEO_HLS_MASTER', 'lion-cinema-bucket', 'hls/user5/master.m3u8',     'application/x-mpegURL', 'PUBLIC', 5120, 7200000, NOW(), NOW());

-- 1-2. Content for User 5
INSERT INTO contents (content_id, owner_user_id, title, description, status, encoding_status, poster_asset_id, video_source_asset_id, video_hls_master_asset_id, total_view, month_view, created_at, updated_at)
VALUES 
(51, 5, 'Multiverse of Madness', '닥터 스트레인지가 멀티버스의 광기 속으로 들어갑니다.', 'PUBLISHED', 'READY', 501, 502, 503, 1500, 120, NOW(), NOW());


-- =============================================
-- 2. User 6 Data (Nature Documentary Style)
-- =============================================

-- 2-1. Media Assets for User 6
INSERT INTO media_assets (asset_id, owner_user_id, asset_type, bucket, object_key, content_type, visibility, size_bytes, duration_ms, created_at, updated_at)
VALUES 
(601, 6, 'POSTER_IMAGE',     'lion-cinema-bucket', 'posters/user6_poster.jpg', 'image/jpeg', 'PUBLIC', 204800, null, NOW(), NOW()),
(602, 6, 'VIDEO_SOURCE',     'lion-cinema-bucket', 'videos/user6_source.mp4',  'video/mp4',  'PRIVATE', 1048576000, 3600000, NOW(), NOW()),
(603, 6, 'VIDEO_HLS_MASTER', 'lion-cinema-bucket', 'hls/user6/master.m3u8',     'application/x-mpegURL', 'PUBLIC', 10240, 3600000, NOW(), NOW());

-- 2-2. Content for User 6
INSERT INTO contents (content_id, owner_user_id, title, description, status, encoding_status, poster_asset_id, video_source_asset_id, video_hls_master_asset_id, total_view, month_view, created_at, updated_at)
VALUES 
(61, 6, 'Planet Earth: Jungles', '지구의 신비로운 정글 생태계를 탐험하는 다큐멘터리.', 'PUBLISHED', 'READY', 601, 602, 603, 5000, 450, NOW(), NOW());

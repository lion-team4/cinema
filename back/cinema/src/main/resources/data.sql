-- Data Initialization for Testing
-- Combined from test-content-data.sql, test-schedule-data.sql, test-review-data.sql

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE reviews;
TRUNCATE TABLE watch_histories;
TRUNCATE TABLE schedule_items;
TRUNCATE TABLE schedule_days;
TRUNCATE TABLE tag_maps;
TRUNCATE TABLE contents;
TRUNCATE TABLE media_assets;
TRUNCATE TABLE tags;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- Users (4)
INSERT INTO users (email, nickname, password_hash, seller, created_at, updated_at) VALUES 
('admin@example.com', 'Administrator', '{noop}password1234', true, NOW(), NOW()),
('user1@example.com', 'MovieLover', '{noop}password1234', false, NOW(), NOW()),
('user2@example.com', 'ActionFan', '{noop}password1234', true, NOW(), NOW()),
('user3@example.com', 'HorrorKing', '{noop}password1234', true, NOW(), NOW());

-- Tags (50)
INSERT INTO tags (name, created_at, updated_at) VALUES
('Action', NOW(), NOW()), ('Sci-Fi', NOW(), NOW()), ('Drama', NOW(), NOW()), ('Horror', NOW(), NOW()), ('Adventure', NOW(), NOW()),
('Thriller', NOW(), NOW()), ('Comedy', NOW(), NOW()), ('Romance', NOW(), NOW()), ('Fantasy', NOW(), NOW()), ('Animation', NOW(), NOW()),
('Documentary', NOW(), NOW()), ('Crime', NOW(), NOW()), ('Mystery', NOW(), NOW()), ('Family', NOW(), NOW()), ('Music', NOW(), NOW()),
('History', NOW(), NOW()), ('War', NOW(), NOW()), ('Western', NOW(), NOW()), ('Biography', NOW(), NOW()), ('Sport', NOW(), NOW()),
('Musical', NOW(), NOW()), ('Short', NOW(), NOW()), ('News', NOW(), NOW()), ('Reality-TV', NOW(), NOW()), ('Talk-Show', NOW(), NOW()),
('Game-Show', NOW(), NOW()), ('Adult', NOW(), NOW()), ('Noir', NOW(), NOW()), ('Neo-Noir', NOW(), NOW()), ('Cyberpunk', NOW(), NOW()),
('Steampunk', NOW(), NOW()), ('Space Opera', NOW(), NOW()), ('Superhero', NOW(), NOW()), ('Zombie', NOW(), NOW()), ('Vampire', NOW(), NOW()),
('Ghost', NOW(), NOW()), ('Slasher', NOW(), NOW()), ('Psychological', NOW(), NOW()), ('Parody', NOW(), NOW()), ('Satire', NOW(), NOW()),
('Indie', NOW(), NOW()), ('Blockbuster', NOW(), NOW()), ('Cult Classic', NOW(), NOW()), ('B-Movie', NOW(), NOW()), ('Silent', NOW(), NOW()),
('Black & White', NOW(), NOW()), ('3D', NOW(), NOW()), ('IMAX', NOW(), NOW()), ('4K', NOW(), NOW()), ('8K', NOW(), NOW());

-- Media Assets (30) - All POSTER_IMAGE
INSERT INTO media_assets (owner_user_id, asset_type, bucket, object_key, content_type, visibility, size_bytes, created_at, updated_at) VALUES
(1, 'POSTER_IMAGE', 'cinema-bucket', 'poster01.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(1, 'POSTER_IMAGE', 'cinema-bucket', 'poster02.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(2, 'POSTER_IMAGE', 'cinema-bucket', 'poster03.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(2, 'POSTER_IMAGE', 'cinema-bucket', 'poster04.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(3, 'POSTER_IMAGE', 'cinema-bucket', 'poster05.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(3, 'POSTER_IMAGE', 'cinema-bucket', 'poster06.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(4, 'POSTER_IMAGE', 'cinema-bucket', 'poster07.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(4, 'POSTER_IMAGE', 'cinema-bucket', 'poster08.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(1, 'POSTER_IMAGE', 'cinema-bucket', 'poster09.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(2, 'POSTER_IMAGE', 'cinema-bucket', 'poster10.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(3, 'POSTER_IMAGE', 'cinema-bucket', 'poster11.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(4, 'POSTER_IMAGE', 'cinema-bucket', 'poster12.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(1, 'POSTER_IMAGE', 'cinema-bucket', 'poster13.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(2, 'POSTER_IMAGE', 'cinema-bucket', 'poster14.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(3, 'POSTER_IMAGE', 'cinema-bucket', 'poster15.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(4, 'POSTER_IMAGE', 'cinema-bucket', 'poster16.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(1, 'POSTER_IMAGE', 'cinema-bucket', 'poster17.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(2, 'POSTER_IMAGE', 'cinema-bucket', 'poster18.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(3, 'POSTER_IMAGE', 'cinema-bucket', 'poster19.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(4, 'POSTER_IMAGE', 'cinema-bucket', 'poster20.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(1, 'POSTER_IMAGE', 'cinema-bucket', 'poster21.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(2, 'POSTER_IMAGE', 'cinema-bucket', 'poster22.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(3, 'POSTER_IMAGE', 'cinema-bucket', 'poster23.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(4, 'POSTER_IMAGE', 'cinema-bucket', 'poster24.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(1, 'POSTER_IMAGE', 'cinema-bucket', 'poster25.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(2, 'POSTER_IMAGE', 'cinema-bucket', 'poster26.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(3, 'POSTER_IMAGE', 'cinema-bucket', 'poster27.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(4, 'POSTER_IMAGE', 'cinema-bucket', 'poster28.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(1, 'POSTER_IMAGE', 'cinema-bucket', 'poster29.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW()),
(2, 'POSTER_IMAGE', 'cinema-bucket', 'poster30.jpg', 'image/jpeg', 'PUBLIC', 1024, NOW(), NOW());

-- Contents (30)
INSERT INTO contents (owner_user_id, title, description, poster_asset_id, status, total_view, month_view, created_at, updated_at) VALUES
(1, 'Inception of Dreams', 'A mind-bending thriller.', 1, 'PUBLISHED', 15000, 1200, '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
(2, 'The Dark Knight Rises Again', 'Batman returns.', 2, 'PUBLISHED', 50000, 4000, '2025-01-02 11:00:00', '2025-01-02 11:00:00'),
(3, 'Interstellar Voyage', 'Space exploration.', 3, 'PUBLISHED', 30000, 2500, '2025-01-03 12:00:00', '2025-01-03 12:00:00'),
(4, 'Parasite: The Hidden Story', 'Social commentary.', 4, 'PUBLISHED', 80000, 7000, '2025-01-04 13:00:00', '2025-01-04 13:00:00'),
(1, 'Avengers: Secret Wars', 'Marvel heroes.', 5, 'PUBLISHED', 120000, 15000, '2025-01-05 14:00:00', '2025-01-05 14:00:00'),
(2, 'Frozen Kingdom', 'Let it go.', 6, 'PUBLISHED', 90000, 8000, '2025-01-06 15:00:00', '2025-01-06 15:00:00'),
(3, 'Toy Story: The Beginning', 'Toys come alive.', 7, 'PUBLISHED', 45000, 3000, '2025-01-07 16:00:00', '2025-01-07 16:00:00'),
(4, 'Lion King: Legacy', 'Circle of life.', 8, 'PUBLISHED', 60000, 5000, '2025-01-08 17:00:00', '2025-01-08 17:00:00'),
(1, 'Spider-Man: Home Alone', 'Web slinging action.', 9, 'PUBLISHED', 110000, 12000, '2025-01-09 18:00:00', '2025-01-09 18:00:00'),
(2, 'Joker: Smile', 'Psychological thriller.', 10, 'PUBLISHED', 75000, 6500, '2025-01-10 19:00:00', '2025-01-10 19:00:00'),
(3, 'Harry Potter: The Auror', 'Wizarding world.', 11, 'PUBLISHED', 95000, 9000, '2025-01-11 20:00:00', '2025-01-11 20:00:00'),
(4, 'Lord of the Rings: New Age', 'Middle earth.', 12, 'PUBLISHED', 85000, 8000, '2025-01-12 21:00:00', '2025-01-12 21:00:00'),
(1, 'Star Wars: The Old Republic', 'Jedi vs Sith.', 13, 'PUBLISHED', 130000, 11000, '2025-01-13 22:00:00', '2025-01-13 22:00:00'),
(2, 'Jurassic Park: Survival', 'Dinosaurs are back.', 14, 'PUBLISHED', 70000, 6000, '2025-01-14 23:00:00', '2025-01-14 23:00:00'),
(3, 'Titanic: Raised', 'Ship romance.', 15, 'PUBLISHED', 150000, 14000, '2025-01-15 09:00:00', '2025-01-15 09:00:00'),
(4, 'Avatar: Fire Nation', 'Blue aliens.', 16, 'PUBLISHED', 200000, 18000, '2025-01-16 10:00:00', '2025-01-16 10:00:00'),
(1, 'Matrix: Reboot', 'Red or blue pill.', 17, 'PUBLISHED', 55000, 4500, '2025-01-17 11:00:00', '2025-01-17 11:00:00'),
(2, 'Gladiator: Arena', 'Are you entertained?', 18, 'PUBLISHED', 65000, 5500, '2025-01-18 12:00:00', '2025-01-18 12:00:00'),
(3, 'Forrest Gump: Run', 'Life is a box of chocolates.', 19, 'PUBLISHED', 100000, 9500, '2025-01-19 13:00:00', '2025-01-19 13:00:00'),
(4, 'Shawshank Redemption: Freedom', 'Hope is a good thing.', 20, 'PUBLISHED', 140000, 13000, '2025-01-20 14:00:00', '2025-01-20 14:00:00'),
(1, 'Godfather: Family', 'Offer he cannot refuse.', 21, 'PUBLISHED', 160000, 15000, '2025-01-21 15:00:00', '2025-01-21 15:00:00'),
(2, 'Pulp Fiction: Royale', 'Royale with cheese.', 22, 'PUBLISHED', 115000, 10500, '2025-01-22 16:00:00', '2025-01-22 16:00:00'),
(3, 'Fight Club: Mayhem', 'First rule.', 23, 'PUBLISHED', 125000, 11500, '2025-01-23 17:00:00', '2025-01-23 17:00:00'),
(4, 'Goodfellas: Mob', 'Funny how?', 24, 'PUBLISHED', 98000, 8800, '2025-01-24 18:00:00', '2025-01-24 18:00:00'),
(1, 'Seven: Sins', 'What is in the box.', 25, 'PUBLISHED', 82000, 7200, '2025-01-25 19:00:00', '2025-01-25 19:00:00'),
(2, 'Silence of the Lambs: Clarice', 'Hello Clarice.', 26, 'PUBLISHED', 92000, 8200, '2025-01-26 20:00:00', '2025-01-26 20:00:00'),
(3, 'City of God: Rio', 'Rocket.', 27, 'PUBLISHED', 52000, 4200, '2025-01-27 21:00:00', '2025-01-27 21:00:00'),
(4, 'Life is Beautiful: Ciao', 'Buongiorno principessa.', 28, 'PUBLISHED', 78000, 6800, '2025-01-28 22:00:00', '2025-01-28 22:00:00'),
(1, 'Spirited Away: Chihiro', 'Bathhouse.', 29, 'PUBLISHED', 135000, 12500, '2025-01-29 23:00:00', '2025-01-29 23:00:00'),
(2, 'Saving Private Ryan: Mission', 'Omaha beach.', 30, 'PUBLISHED', 105000, 9800, '2025-01-30 09:00:00', '2025-01-30 09:00:00');

-- Tag Maps
INSERT INTO tag_maps (content_id, tag_id) VALUES
(1, 1), (1, 2), (1, 6), -- Inception: Action, Sci-Fi, Thriller
(2, 1), (2, 6), (2, 12), -- Dark Knight: Action, Thriller, Crime
(3, 2), (3, 5), (3, 32), -- Interstellar: Sci-Fi, Adventure, Space Opera
(4, 3), (4, 6), (4, 42), -- Parasite: Drama, Thriller, Cult Classic
(5, 1), (5, 33), (5, 41), -- Avengers: Action, Superhero, Blockbuster
(6, 9), (6, 10), (6, 14), -- Frozen: Fantasy, Animation, Family
(7, 9), (7, 10), (7, 14), -- Toy Story: Fantasy, Animation, Family
(8, 3), (8, 9), (8, 10), -- Lion King: Drama, Fantasy, Animation
(9, 1), (9, 33), (9, 5), -- Spider-Man: Action, Superhero, Adventure
(10, 3), (10, 12), (10, 38), -- Joker: Drama, Crime, Psychological
(11, 5), (11, 9), (11, 14), -- Harry Potter: Adventure, Fantasy, Family
(12, 1), (12, 5), (12, 9), -- LOTR: Action, Adventure, Fantasy
(13, 1), (13, 2), (13, 32), -- Star Wars: Action, Sci-Fi, Space Opera
(14, 2), (14, 5), (14, 6), -- Jurassic Park: Sci-Fi, Adventure, Thriller
(15, 3), (15, 8), (15, 41), -- Titanic: Drama, Romance, Blockbuster
(16, 1), (16, 2), (16, 48), -- Avatar: Action, Sci-Fi, IMAX
(17, 1), (17, 2), (17, 30), -- Matrix: Action, Sci-Fi, Cyberpunk
(18, 1), (18, 3), (18, 16), -- Gladiator: Action, Drama, History
(19, 3), (19, 8), (19, 42), -- Forrest Gump: Drama, Romance, Cult Classic
(20, 3), (20, 12), (20, 42), -- Shawshank: Drama, Crime, Cult Classic
(21, 3), (21, 12), (21, 28), -- Godfather: Drama, Crime, Noir
(22, 3), (22, 12), (22, 29), -- Pulp Fiction: Drama, Crime, Neo-Noir
(23, 3), (23, 38), (23, 40), -- Fight Club: Drama, Psychological, Satire
(24, 3), (24, 12), (24, 19), -- Goodfellas: Drama, Crime, Biography
(25, 3), (25, 12), (25, 13), -- Seven: Drama, Crime, Mystery
(26, 3), (26, 6), (26, 38), -- Silence of Lambs: Drama, Thriller, Psychological
(27, 3), (27, 12), (27, 40), -- City of God: Drama, Crime, Indie
(28, 3), (28, 7), (28, 17), -- Life is Beautiful: Drama, Comedy, War
(29, 5), (29, 9), (29, 10), -- Spirited Away: Adventure, Fantasy, Animation
(30, 1), (30, 3), (30, 17); -- Saving Private Ryan: Action, Drama, War

-- Schedule Days
INSERT INTO schedule_days (content_id, schedule_date, is_locked, created_at, updated_at) VALUES
(1, '2026-01-19', true, NOW(), NOW()),   -- ID: 1
(1, '2026-01-20', true, NOW(), NOW()),   -- ID: 2
(1, '2026-01-21', false, NOW(), NOW()),  -- ID: 3
(5, '2026-01-19', true, NOW(), NOW()),   -- ID: 4
(5, '2026-01-22', false, NOW(), NOW()),  -- ID: 5
(9, '2026-01-20', true, NOW(), NOW()),   -- ID: 6
(9, '2026-01-23', true, NOW(), NOW()),   -- ID: 7
(13, '2026-01-21', true, NOW(), NOW()),  -- ID: 8
(13, '2026-01-24', false, NOW(), NOW()), -- ID: 9
(17, '2026-01-22', true, NOW(), NOW()),  -- ID: 10
(17, '2026-01-25', true, NOW(), NOW()),  -- ID: 11
(3, '2026-01-19', true, NOW(), NOW()),   -- ID: 12
(3, '2026-01-21', true, NOW(), NOW()),   -- ID: 13
(3, '2026-01-23', false, NOW(), NOW()),  -- ID: 14
(7, '2026-01-20', true, NOW(), NOW()),   -- ID: 15
(7, '2026-01-22', true, NOW(), NOW()),   -- ID: 16
(11, '2026-01-21', true, NOW(), NOW()),  -- ID: 17
(11, '2026-01-24', true, NOW(), NOW()),  -- ID: 18
(15, '2026-01-22', true, NOW(), NOW()),  -- ID: 19
(15, '2026-01-25', false, NOW(), NOW()), -- ID: 20
(4, '2026-01-19', true, NOW(), NOW()),   -- ID: 21
(4, '2026-01-20', true, NOW(), NOW()),   -- ID: 22
(4, '2026-01-22', false, NOW(), NOW()),  -- ID: 23
(8, '2026-01-21', true, NOW(), NOW()),   -- ID: 24
(8, '2026-01-23', true, NOW(), NOW()),   -- ID: 25
(12, '2026-01-20', true, NOW(), NOW()),  -- ID: 26
(12, '2026-01-24', true, NOW(), NOW()),  -- ID: 27
(16, '2026-01-21', true, NOW(), NOW()),  -- ID: 28
(16, '2026-01-25', false, NOW(), NOW()); -- ID: 29

-- Schedule Items
INSERT INTO schedule_items (schedule_day_id, content_id, start_at, end_at, status, created_at, updated_at) VALUES
(1, 1, '2026-01-19 10:00:00', '2026-01-19 12:00:00', 'CLOSED', NOW(), NOW()),
(1, 1, '2026-01-19 14:00:00', '2026-01-19 16:00:00', 'CLOSED', NOW(), NOW()),
(1, 1, '2026-01-19 20:00:00', '2026-01-19 22:00:00', 'CLOSED', NOW(), NOW()),
(2, 1, '2026-01-20 09:00:00', '2026-01-20 11:00:00', 'CLOSED', NOW(), NOW()),
(2, 1, '2026-01-20 15:00:00', '2026-01-20 17:00:00', 'CLOSED', NOW(), NOW()),
(3, 1, '2026-01-21 11:00:00', '2026-01-21 13:00:00', 'CLOSED', NOW(), NOW()),
(4, 5, '2026-01-19 11:00:00', '2026-01-19 13:00:00', 'CLOSED', NOW(), NOW()),
(4, 5, '2026-01-19 17:00:00', '2026-01-19 19:00:00', 'CLOSED', NOW(), NOW()),
(5, 5, '2026-01-22 10:00:00', '2026-01-22 12:00:00', 'CLOSED', NOW(), NOW()),
(6, 9, '2026-01-20 13:00:00', '2026-01-20 15:00:00', 'CLOSED', NOW(), NOW()),
(7, 9, '2026-01-23 19:00:00', '2026-01-23 21:00:00', 'CLOSED', NOW(), NOW()),
(8, 13, '2026-01-21 10:00:00', '2026-01-21 12:00:00', 'CLOSED', NOW(), NOW()),
(9, 13, '2026-01-24 11:00:00', '2026-01-24 13:00:00', 'CLOSED', NOW(), NOW()),
(10, 17, '2026-01-22 09:00:00', '2026-01-22 11:00:00', 'CLOSED', NOW(), NOW()),
(11, 17, '2026-01-25 15:00:00', '2026-01-25 17:00:00', 'CLOSED', NOW(), NOW()),
(12, 3, '2026-01-19 09:00:00', '2026-01-19 11:00:00', 'CLOSED', NOW(), NOW()),
(12, 3, '2026-01-19 21:00:00', '2026-01-19 23:00:00', 'CLOSED', NOW(), NOW()),
(13, 3, '2026-01-21 14:00:00', '2026-01-21 16:00:00', 'CLOSED', NOW(), NOW()),
(14, 3, '2026-01-23 10:00:00', '2026-01-23 12:00:00', 'CLOSED', NOW(), NOW()),
(15, 7, '2026-01-20 14:00:00', '2026-01-20 16:00:00', 'CLOSED', NOW(), NOW()),
(16, 7, '2026-01-22 18:00:00', '2026-01-22 20:00:00', 'CLOSED', NOW(), NOW()),
(17, 11, '2026-01-21 08:00:00', '2026-01-21 10:00:00', 'CLOSED', NOW(), NOW()),
(18, 11, '2026-01-24 20:00:00', '2026-01-24 22:00:00', 'CLOSED', NOW(), NOW()),
(19, 15, '2026-01-22 10:00:00', '2026-01-22 12:00:00', 'CLOSED', NOW(), NOW()),
(20, 15, '2026-01-25 21:00:00', '2026-01-25 23:00:00', 'CLOSED', NOW(), NOW()),
(21, 4, '2026-01-19 22:00:00', '2026-01-19 23:59:00', 'CLOSED', NOW(), NOW()),
(22, 4, '2026-01-20 10:00:00', '2026-01-20 12:00:00', 'CLOSED', NOW(), NOW()),
(22, 4, '2026-01-20 18:00:00', '2026-01-20 20:00:00', 'CLOSED', NOW(), NOW()),
(23, 4, '2026-01-22 14:00:00', '2026-01-22 16:00:00', 'CLOSED', NOW(), NOW()),
(24, 8, '2026-01-21 13:00:00', '2026-01-21 15:00:00', 'CLOSED', NOW(), NOW()),
(25, 8, '2026-01-23 21:00:00', '2026-01-23 23:00:00', 'CLOSED', NOW(), NOW()),
(26, 12, '2026-01-20 08:00:00', '2026-01-20 10:00:00', 'CLOSED', NOW(), NOW()),
(27, 12, '2026-01-24 16:00:00', '2026-01-24 18:00:00', 'CLOSED', NOW(), NOW()),
(28, 16, '2026-01-21 19:00:00', '2026-01-21 21:00:00', 'CLOSED', NOW(), NOW()),
(29, 16, '2026-01-25 11:00:00', '2026-01-25 13:00:00', 'CLOSED', NOW(), NOW());

-- Watch Histories & Reviews
INSERT INTO watch_histories (user_id, schedule_item_id, enter_at, left_at, view_counted, created_at, updated_at) VALUES
(2, 1, '2026-01-19 10:00:00', '2026-01-19 12:00:00', true, '2026-01-19 12:00:00', '2026-01-19 12:00:00'),
(2, 2, '2026-01-19 14:00:00', '2026-01-19 16:00:00', true, '2026-01-19 16:00:00', '2026-01-19 16:00:00'),
(2, 7, '2026-01-19 11:00:00', '2026-01-19 13:00:00', true, '2026-01-19 13:00:00', '2026-01-19 13:00:00'),
(2, 16, '2026-01-19 09:00:00', '2026-01-19 09:10:00', false, '2026-01-19 09:10:00', '2026-01-19 09:10:00');

INSERT INTO reviews (content_id, user_id, watch_id, rating, comment, created_at, updated_at) VALUES
(1, 2, 1, 5, '인셉션은 정말 다시 봐도 명작이네요. 꿈 속의 꿈!', '2026-01-19 13:00:00', '2026-01-19 13:00:00');

INSERT INTO watch_histories (user_id, schedule_item_id, enter_at, left_at, view_counted, created_at, updated_at) VALUES
(3, 1, '2026-01-19 10:00:00', '2026-01-19 12:00:00', true, '2026-01-19 12:00:00', '2026-01-19 12:00:00');

INSERT INTO reviews (content_id, user_id, watch_id, rating, comment, created_at, updated_at) VALUES
(1, 3, 5, 4, '복잡하지만 몰입감이 대단합니다.', '2026-01-19 13:30:00', '2026-01-19 13:30:00');

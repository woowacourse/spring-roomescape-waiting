-- 시간 데이터
INSERT INTO reservation_time (id, start_at) VALUES (1, '09:00:00'); -- id=1, 오늘 이미 지난 시간
INSERT INTO reservation_time (id, start_at) VALUES (2, '11:00:00'); -- id=2, 오늘 아직 안 지난 시간
INSERT INTO reservation_time (id, start_at) VALUES (3, '10:00:00'); -- id=3, 오늘 이미 지난 시간

-- 테마 데이터
INSERT INTO theme (id, name, description, thumbnail_url) VALUES (1, '링', '공포 테마', 'http://thumbnail.com');

-- 대기 데이터
-- 포함돼야 하는 케이스
INSERT INTO waiting (customer_name, reservation_date, time_id, theme_id) VALUES ('재키', '2026-05-08', 2, 1); -- 오늘 + 아직 안 지난 시간
INSERT INTO waiting (customer_name, reservation_date, time_id, theme_id) VALUES ('재키', '2026-05-09', 1, 1); -- 미래 날짜

-- 포함되지 않아야 하는 케이스
INSERT INTO waiting (customer_name, reservation_date, time_id, theme_id) VALUES ('재키', '2026-05-08', 1, 1); -- 오늘 + 이미 지난 시간 (09:00)
INSERT INTO waiting (customer_name, reservation_date, time_id, theme_id) VALUES ('재키', '2026-05-08', 3, 1); -- 오늘 + 이미 지난 시간 (10:00)
INSERT INTO waiting (customer_name, reservation_date, time_id, theme_id) VALUES ('재키', '2026-05-07', 2, 1); -- 과거 날짜
INSERT INTO waiting (customer_name, reservation_date, time_id, theme_id) VALUES ('코로구', '2026-05-09', 1, 1); -- 다른 사람

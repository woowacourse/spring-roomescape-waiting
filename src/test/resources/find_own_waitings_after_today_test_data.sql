-- 시간 데이터
INSERT INTO reservation_time (id, start_at) VALUES (1, '09:00:00'); -- id=1, 오늘 이미 지난 시간
INSERT INTO reservation_time (id, start_at) VALUES (2, '11:00:00'); -- id=2, 오늘 아직 안 지난 시간
INSERT INTO reservation_time (id, start_at) VALUES (3, '10:00:00'); -- id=3, 오늘 이미 지난 시간

-- 테마 데이터
INSERT INTO theme (id, name, description, thumbnail_url, price) VALUES (1, '링', '공포 테마', 'http://thumbnail.com', 10000);

-- 예약 슬롯 데이터
INSERT INTO reservation_slot (id, reservation_date, time_id, theme_id)
VALUES (1, CURRENT_DATE, 2, 1),
       (2, DATEADD('DAY', 1, CURRENT_DATE), 1, 1),
       (3, CURRENT_DATE, 1, 1),
       (4, CURRENT_DATE, 3, 1),
       (5, DATEADD('DAY', -1, CURRENT_DATE), 2, 1);

-- 대기 데이터
-- 포함돼야 하는 케이스
INSERT INTO waiting (customer_name, customer_email, slot_id) VALUES ('재키', 'jaekkii@example.com', 1); -- 오늘 + 아직 안 지난 시간
INSERT INTO waiting (customer_name, customer_email, slot_id) VALUES ('재키', 'jaekkii@example.com', 2); -- 미래 날짜

-- 포함되지 않아야 하는 케이스
INSERT INTO waiting (customer_name, customer_email, slot_id) VALUES ('재키', 'jaekkii@example.com', 3); -- 오늘 + 이미 지난 시간 (09:00)
INSERT INTO waiting (customer_name, customer_email, slot_id) VALUES ('재키', 'jaekkii@example.com', 4); -- 오늘 + 이미 지난 시간 (10:00)
INSERT INTO waiting (customer_name, customer_email, slot_id) VALUES ('재키', 'jaekkii@example.com', 5); -- 과거 날짜
INSERT INTO waiting (customer_name, customer_email, slot_id) VALUES ('코로구', 'korogoo@example.com', 2); -- 다른 사람

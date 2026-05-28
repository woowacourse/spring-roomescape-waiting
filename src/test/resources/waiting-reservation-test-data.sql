INSERT INTO reservation_date (play_day)
VALUES (CURRENT_DATE + 10),
       (CURRENT_DATE + 15),
       (CURRENT_DATE + 20);

INSERT INTO reservation_time (start_at)
VALUES ('22:00'),
       ('12:00'),
       ('14:00');

INSERT INTO theme (name, content, url)
VALUES ('테스트테마', '설명', 'url'),
       ('공포테마', '무서운 테마', 'url2'),
       ('스릴러테마', '스릴 넘치는 테마', 'url3'),
       ('코미디테마', '웃긴 테마', 'url4');

-- 각 슬롯에 기존 예약자 배치 (대기 신청 조건 충족)
INSERT INTO reservation (name, date_id, time_id, theme_id)
VALUES ('기존예약자', 1, 1, 1),
       ('기존예약자', 2, 2, 2),
       ('기존예약자', 3, 3, 3),
       ('기존예약자', 1, 1, 4);

-- 슬롯1: 기존대기자(1순위) → 고래(2순위)
-- 슬롯2: 나무(1순위) → 이산(2순위) → 고래(3순위)
-- 슬롯3: 고래만 (1순위)
INSERT INTO waiting_reservation (name, date_id, time_id, theme_id, created_at)
VALUES ('기존대기자', 1, 1, 1, CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
       ('고래',      1, 1, 1, CURRENT_TIMESTAMP - INTERVAL '1' HOUR),
       ('나무',      2, 2, 2, CURRENT_TIMESTAMP - INTERVAL '3' HOUR),
       ('이산',      2, 2, 2, CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
       ('고래',      2, 2, 2, CURRENT_TIMESTAMP - INTERVAL '1' HOUR),
       ('고래',      3, 3, 3, CURRENT_TIMESTAMP);

-- 시간 데이터
INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('11:00'),
       ('12:00'),
       ('13:00'),
       ('14:00'),
       ('15:00');

-- 테마 데이터
INSERT INTO theme (name, description, thumbnail_url, price)
VALUES ('무인도 탈출', '갯벌이 많은 무인도를 탈출하는 흥미진진 대탈출!', 'https://picsum.photos/seed/roomescape1/800/600.jpg', 30000),
       ('도시 탈출', '아포칼립스 상황인 도시 탈출하는 흥미진진 대탈출!', 'https://picsum.photos/seed/roomescape2/800/600.jpg', 28000),
       ('열기구 탈출', '터지기 5분전! 열기구 탈출하는 흥미진진 대탈출!', 'https://picsum.photos/seed/roomescape3/800/600.jpg', 32000),
       ('페허 탈출', '고립된 페어를 탈출하는 흥미진진 대탈출 !', 'https://picsum.photos/seed/roomescape4/800/600.jpg', 25000),
       ('혹한기 탈출', '혹한기에 에베레스트를 탈출하는 흥미진진 대탈출 !', 'https://picsum.photos/seed/roomescape6/800/600.jpg', 35000),
       (' 이름 1', '설명1', 'https://picsum.photos/seed/roomescape7/800/600.jpg', 10000),
       (' 이름 2', '설명2', 'https://picsum.photos/seed/roomescape8/800/600.jpg', 10000),
       (' 이름 3', '설명3', 'https://picsum.photos/seed/roomescape9/800/600.jpg', 10000),
       (' 이름 4', '설명4', 'https://picsum.photos/seed/roomescape10/800/600.jpg', 10000),
       (' 이름 5', '설명5', 'https://picsum.photos/seed/roomescape11/800/600.jpg', 10000),
       (' 이름 6', '설명6', 'https://picsum.photos/seed/roomescape12/800/600.jpg', 10000),
       (' 이름 7', '설명7', 'https://picsum.photos/seed/roomescape13/800/600.jpg', 10000),
       (' 이름 8', '설명8', 'https://picsum.photos/seed/roomescape14/800/600.jpg', 10000);

-- 날짜 데이터 생성 (과거 일부 + 오늘부터 30일치)
INSERT INTO reservation_date (date)
VALUES (DATEADD('DAY', -1, CURRENT_DATE)),
       (DATEADD('DAY', -5, CURRENT_DATE)),
       (DATEADD('DAY', -8, CURRENT_DATE));

INSERT INTO reservation_date (date)
SELECT DATEADD('DAY', x, CURRENT_DATE)
FROM SYSTEM_RANGE(0, 30);

-- 모든 가능한 슬롯 생성 (날짜 x 시간 x 테마)
INSERT INTO reservation_slot (date_id, time_id, theme_id)
SELECT d.id, t.id, th.id
FROM reservation_date d, reservation_time t, theme th;

-- 인기 테마 검증용 reservation
INSERT INTO reservation (name, slot_id)
SELECT 'user1', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', -1, CURRENT_DATE) AND s.time_id = 1 AND s.theme_id = 1 UNION ALL
SELECT 'user2', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', -1, CURRENT_DATE) AND s.time_id = 1 AND s.theme_id = 1 UNION ALL
SELECT 'user2', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', -1, CURRENT_DATE) AND s.time_id = 2 AND s.theme_id = 1 UNION ALL
SELECT 'user3', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', -1, CURRENT_DATE) AND s.time_id = 3 AND s.theme_id = 1 UNION ALL
SELECT 'user4', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', -5, CURRENT_DATE) AND s.time_id = 1 AND s.theme_id = 1 UNION ALL
SELECT 'user5', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', -5, CURRENT_DATE) AND s.time_id = 2 AND s.theme_id = 1;

-- 도시(theme_id=2): 5일 전 4건 = 4건 + 8일 전 2건
INSERT INTO reservation (name, slot_id)
SELECT 'user6', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', -5, CURRENT_DATE) AND s.time_id = 1 AND s.theme_id = 2 UNION ALL
SELECT 'user7', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', -5, CURRENT_DATE) AND s.time_id = 2 AND s.theme_id = 2 UNION ALL
SELECT 'user8', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', -5, CURRENT_DATE) AND s.time_id = 3 AND s.theme_id = 2 UNION ALL
SELECT 'user9', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', -5, CURRENT_DATE) AND s.time_id = 4 AND s.theme_id = 2 UNION ALL
SELECT 'user10', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', -8, CURRENT_DATE) AND s.time_id = 1 AND s.theme_id = 2 UNION ALL
SELECT 'user11', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', -8, CURRENT_DATE) AND s.time_id = 2 AND s.theme_id = 2;

-- 열기구(theme_id=3): 어제 1건
INSERT INTO reservation (name, slot_id)
SELECT 'user12', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', -1, CURRENT_DATE) AND s.time_id = 1 AND s.theme_id = 3;

-- 추가: 무인도(theme_id=1) 오늘 5건
INSERT INTO reservation (name, slot_id)
SELECT 'user13', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = CURRENT_DATE AND s.time_id = 1 AND s.theme_id = 1 UNION ALL
SELECT 'user14', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = CURRENT_DATE AND s.time_id = 2 AND s.theme_id = 1 UNION ALL
SELECT 'user15', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = CURRENT_DATE AND s.time_id = 3 AND s.theme_id = 1 UNION ALL
SELECT 'user16', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = CURRENT_DATE AND s.time_id = 4 AND s.theme_id = 1 UNION ALL
SELECT 'user17', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = CURRENT_DATE AND s.time_id = 5 AND s.theme_id = 1;

-- 활동적인 유저
INSERT INTO reservation (name, slot_id)
SELECT 'user1', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', 5, CURRENT_DATE) AND s.time_id = 3 AND s.theme_id = 2 UNION ALL
SELECT 'user1', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', 10, CURRENT_DATE) AND s.time_id = 4 AND s.theme_id = 5 UNION ALL
SELECT 'user1', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', 20, CURRENT_DATE) AND s.time_id = 2 AND s.theme_id = 8 UNION ALL
SELECT 'user1', s.id FROM reservation_slot s JOIN reservation_date d ON s.date_id = d.id WHERE d.date = DATEADD('DAY', 25, CURRENT_DATE) AND s.time_id = 6 AND s.theme_id = 11;

-- 시간 슬롯 5개 (id 1~6)
INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('11:00'),
       ('12:00'),
       ('13:00'),
       ('14:00'),
       ('15:00');


-- 테마 3개 (id 1~13)
INSERT INTO theme (name, description, thumbnail_url)
VALUES ('무인도 탈출', '갯벌이 많은 무인도를 탈출하는 흥미진진 대탈출!', 'https://picsum.photos/seed/roomescape1/800/600.jpg'),
       ('도시 탈출', '아포칼립스 상황인 도시 탈출하는 흥미진진 대탈출!', 'https://picsum.photos/seed/roomescape2/800/600.jpg'),
       ('열기구 탈출', '터지기 5분전! 열기구 탈출하는 흥미진진 대탈출!', 'https://picsum.photos/seed/roomescape3/800/600.jpg'),
       ('페허 탈출', '고립된 페어를 탈출하는 흥미진진 대탈출 !', 'https://picsum.photos/seed/roomescape4/800/600.jpg'),
       ('혹한기 탈출', '혹한기에 에베레스트를 탈출하는 흥미진진 대탈출 !', 'https://picsum.photos/seed/roomescape6/800/600.jpg'),
       (' 이름 1', '설명1', 'https://picsum.photos/seed/roomescape7/800/600.jpg'),
       (' 이름 2', '설명2', 'https://picsum.photos/seed/roomescape8/800/600.jpg'),
       (' 이름 3', '설명3', 'https://picsum.photos/seed/roomescape9/800/600.jpg'),
       (' 이름 4', '설명4', 'https://picsum.photos/seed/roomescape10/800/600.jpg'),
       (' 이름 5', '설명5', 'https://picsum.photos/seed/roomescape11/800/600.jpg'),
       (' 이름 6', '설명6', 'https://picsum.photos/seed/roomescape12/800/600.jpg'),
       (' 이름 7', '설명7', 'https://picsum.photos/seed/roomescape13/800/600.jpg'),
       (' 이름 8', '설명8', 'https://picsum.photos/seed/roomescape14/800/600.jpg');



-- 인기 테마 검증용 reservation
-- 기대 결과: 무인도(theme_id=1) 5건, 도시(theme_id=2) 4건, 열기구(theme_id=3) 1건

-- 무인도(theme_id=1): 어제 3건 + 5일 전 2건 = 5건
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', -1, CURRENT_DATE), 1, 1),
       ('user2', DATEADD('DAY', -1, CURRENT_DATE), 1, 1),
       ('user2', DATEADD('DAY', -1, CURRENT_DATE), 2, 1),
       ('user3', DATEADD('DAY', -1, CURRENT_DATE), 3, 1),
       ('user4', DATEADD('DAY', -5, CURRENT_DATE), 1, 1),
       ('user5', DATEADD('DAY', -5, CURRENT_DATE), 2, 1);

-- 도시(theme_id=2): 5일 전 4건 = 4건  + 8일 전 2건
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user6', DATEADD('DAY', -5, CURRENT_DATE), 1, 2),
       ('user7', DATEADD('DAY', -5, CURRENT_DATE), 2, 2),
       ('user8', DATEADD('DAY', -5, CURRENT_DATE), 3, 2),
       ('user9', DATEADD('DAY', -5, CURRENT_DATE), 4, 2),
       ('user10', DATEADD('DAY', -8, CURRENT_DATE), 1, 2),
       ('user11', DATEADD('DAY', -8, CURRENT_DATE), 2, 2);

-- 열기구(theme_id=3): 어제 1건 = 1건
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user12', DATEADD('DAY', -1, CURRENT_DATE), 1, 3);

-- 추가: 무인도(theme_id=1) 오늘 5건
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user13', CURRENT_DATE, 1, 1),
       ('user14', CURRENT_DATE, 2, 1),
       ('user15', CURRENT_DATE, 3, 1),
       ('user16', CURRENT_DATE, 4, 1),
       ('user17', CURRENT_DATE, 5, 1);


-- 총 11개의 테마가 예약됨. 하지만 10개만 인기 테마로 나와야함.
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user18', DATEADD('DAY', -1, CURRENT_DATE), 6, 9),
       ('user19', DATEADD('DAY', -1, CURRENT_DATE), 6, 10),
       ('user20', DATEADD('DAY', -1, CURRENT_DATE), 6, 11),
       ('user21', DATEADD('DAY', -1, CURRENT_DATE), 6, 12),
       ('user22', DATEADD('DAY', -1, CURRENT_DATE), 6, 5),
       ('user23', DATEADD('DAY', -1, CURRENT_DATE), 6, 6),
       ('user24', DATEADD('DAY', -1, CURRENT_DATE), 6, 7),
       ('user25', DATEADD('DAY', -1, CURRENT_DATE), 6, 8);


-- ============================================
-- 미래 예약 데이터 (다양한 날짜/시간/테마 조합)
-- ============================================

-- 내일: 인기 시간대(저녁) + 인기 테마
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', 1, CURRENT_DATE), 5, 1),
       ('user3', DATEADD('DAY', 1, CURRENT_DATE), 6, 2),
       ('user5', DATEADD('DAY', 1, CURRENT_DATE), 4, 3),
       ('user7', DATEADD('DAY', 1, CURRENT_DATE), 3, 4),
       ('user10', DATEADD('DAY', 1, CURRENT_DATE), 2, 5);

-- 3일 후: 골고루 분포
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user2', DATEADD('DAY', 3, CURRENT_DATE), 1, 6),
       ('user4', DATEADD('DAY', 3, CURRENT_DATE), 2, 7),
       ('user6', DATEADD('DAY', 3, CURRENT_DATE), 3, 8),
       ('user8', DATEADD('DAY', 3, CURRENT_DATE), 4, 9),
       ('user9', DATEADD('DAY', 3, CURRENT_DATE), 5, 10);

-- 일주일 후
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user11', DATEADD('DAY', 7, CURRENT_DATE), 1, 11),
       ('user12', DATEADD('DAY', 7, CURRENT_DATE), 2, 12),
       ('user13', DATEADD('DAY', 7, CURRENT_DATE), 3, 13),
       ('user14', DATEADD('DAY', 7, CURRENT_DATE), 4, 1),
       ('user15', DATEADD('DAY', 7, CURRENT_DATE), 5, 2);

-- 2주 후
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user16', DATEADD('DAY', 14, CURRENT_DATE), 6, 3),
       ('user17', DATEADD('DAY', 14, CURRENT_DATE), 1, 4),
       ('user18', DATEADD('DAY', 14, CURRENT_DATE), 2, 5),
       ('user19', DATEADD('DAY', 14, CURRENT_DATE), 3, 6),
       ('user20', DATEADD('DAY', 14, CURRENT_DATE), 4, 7);

-- 한 달 후
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user21', DATEADD('DAY', 30, CURRENT_DATE), 5, 8),
       ('user22', DATEADD('DAY', 30, CURRENT_DATE), 6, 9),
       ('user23', DATEADD('DAY', 30, CURRENT_DATE), 1, 10),
       ('user24', DATEADD('DAY', 30, CURRENT_DATE), 2, 11),
       ('user25', DATEADD('DAY', 30, CURRENT_DATE), 3, 12);


-- ============================================
-- 활동적인 유저: 동일 유저가 여러 예약 보유 (마이페이지/내 예약 조회 테스트용)
-- ============================================

-- user1: 총 6개 예약 (과거 1 + 미래 5)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', 5, CURRENT_DATE), 3, 2),
       ('user1', DATEADD('DAY', 10, CURRENT_DATE), 4, 5),
       ('user1', DATEADD('DAY', 20, CURRENT_DATE), 2, 8),
       ('user1', DATEADD('DAY', 25, CURRENT_DATE), 6, 11);

-- user13: 총 5개 예약
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user13', DATEADD('DAY', 2, CURRENT_DATE), 6, 7),
       ('user13', DATEADD('DAY', 9, CURRENT_DATE), 1, 9),
       ('user13', DATEADD('DAY', 21, CURRENT_DATE), 4, 12);

-- user5: 총 4개 예약
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user5', DATEADD('DAY', 6, CURRENT_DATE), 1, 13),
       ('user5', DATEADD('DAY', 12, CURRENT_DATE), 5, 1);


-- ============================================
-- 인기 슬롯: 동일 (날짜+시간+테마)에 여러 유저 예약 (대기열 테스트용)
-- ============================================

-- 슬롯 A: 내일 15:00 무인도 탈출 (theme_id=1)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user2', DATEADD('DAY', 1, CURRENT_DATE), 6, 1),
       ('user6', DATEADD('DAY', 1, CURRENT_DATE), 6, 1),
       ('user11', DATEADD('DAY', 1, CURRENT_DATE), 6, 1);

-- 슬롯 B: 7일 후 14:00 도시 탈출 (theme_id=2)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user3', DATEADD('DAY', 7, CURRENT_DATE), 5, 2),
       ('user8', DATEADD('DAY', 7, CURRENT_DATE), 5, 2);

-- 슬롯 C: 3일 후 15:00 열기구 탈출 (theme_id=3)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user14', DATEADD('DAY', 3, CURRENT_DATE), 6, 3),
       ('user17', DATEADD('DAY', 3, CURRENT_DATE), 6, 3),
       ('user20', DATEADD('DAY', 3, CURRENT_DATE), 6, 3),
       ('user24', DATEADD('DAY', 3, CURRENT_DATE), 6, 3);

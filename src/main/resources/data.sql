-- 시간 슬롯 5개 (id 1~6)
INSERT INTO reservation_time (start_at)
VALUES ('10:00');
INSERT INTO reservation_time (start_at)
VALUES ('11:00');
INSERT INTO reservation_time (start_at)
VALUES ('12:00');
INSERT INTO reservation_time (start_at)
VALUES ('13:00');
INSERT INTO reservation_time (start_at)
VALUES ('14:00');
INSERT INTO reservation_time (start_at)
VALUES ('15:00');


-- 테마 3개 (id 1~13)
INSERT INTO theme (name, description, thumbnail_url)
VALUES ('무인도 탈출', '갯벌이 많은 무인도를 탈출하는 흥미진진 대탈출!', 'https://picsum.photos/seed/roomescape1/800/600.jpg');
INSERT INTO theme (name, description, thumbnail_url)
VALUES ('도시 탈출', '아포칼립스 상황인 도시 탈출하는 흥미진진 대탈출!', 'https://picsum.photos/seed/roomescape2/800/600.jpg');
INSERT INTO theme (name, description, thumbnail_url)
VALUES ('열기구 탈출', '터지기 5분전! 열기구 탈출하는 흥미진진 대탈출!', 'https://picsum.photos/seed/roomescape3/800/600.jpg');
INSERT INTO theme (name, description, thumbnail_url)
VALUES ('페허 탈출', '고립된 페어를 탈출하는 흥미진진 대탈출 !', 'https://picsum.photos/seed/roomescape4/800/600.jpg');
INSERT INTO theme (name, description, thumbnail_url)
VALUES ('혹한기 탈출', '혹한기에 에베레스트를 탈출하는 흥미진진 대탈출 !', 'https://picsum.photos/seed/roomescape6/800/600.jpg');
INSERT INTO theme (name, description, thumbnail_url)
VALUES (' 이름 1', '설명1', 'https://picsum.photos/seed/roomescape7/800/600.jpg');
INSERT INTO theme (name, description, thumbnail_url)
VALUES (' 이름 2', '설명2', 'https://picsum.photos/seed/roomescape8/800/600.jpg');
INSERT INTO theme (name, description, thumbnail_url)
VALUES (' 이름 3', '설명3', 'https://picsum.photos/seed/roomescape9/800/600.jpg');
INSERT INTO theme (name, description, thumbnail_url)
VALUES (' 이름 4', '설명4', 'https://picsum.photos/seed/roomescape10/800/600.jpg');
INSERT INTO theme (name, description, thumbnail_url)
VALUES (' 이름 5', '설명5', 'https://picsum.photos/seed/roomescape11/800/600.jpg');
INSERT INTO theme (name, description, thumbnail_url)
VALUES (' 이름 6', '설명6', 'https://picsum.photos/seed/roomescape12/800/600.jpg');
INSERT INTO theme (name, description, thumbnail_url)
VALUES (' 이름 7', '설명7', 'https://picsum.photos/seed/roomescape13/800/600.jpg');
INSERT INTO theme (name, description, thumbnail_url)
VALUES (' 이름 8', '설명8', 'https://picsum.photos/seed/roomescape14/800/600.jpg');



-- 인기 테마 검증용 reservation
-- 기대 결과: 무인도(theme_id=1) 5건, 도시(theme_id=2) 4건, 열기구(theme_id=3) 1건

-- 무인도(theme_id=1): 어제 3건 + 5일 전 2건 = 5건
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user2', DATEADD('DAY', -1, CURRENT_DATE), 2, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user3', DATEADD('DAY', -1, CURRENT_DATE), 3, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user4', DATEADD('DAY', -5, CURRENT_DATE), 1, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user5', DATEADD('DAY', -5, CURRENT_DATE), 2, 1);

-- 도시(theme_id=2): 5일 전 4건 = 4건  + 8일 전 2건
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user6', DATEADD('DAY', -5, CURRENT_DATE), 1, 2);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user7', DATEADD('DAY', -5, CURRENT_DATE), 2, 2);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user8', DATEADD('DAY', -5, CURRENT_DATE), 3, 2);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user9', DATEADD('DAY', -5, CURRENT_DATE), 4, 2);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user10', DATEADD('DAY', -8, CURRENT_DATE), 1, 2);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user11', DATEADD('DAY', -8, CURRENT_DATE), 2, 2);

-- 열기구(theme_id=3): 어제 1건 = 1건
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user12', DATEADD('DAY', -1, CURRENT_DATE), 1, 3);

-- 추가: 무인도(theme_id=1) 오늘 5건
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user13', CURRENT_DATE, 1, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user14', CURRENT_DATE, 2, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user15', CURRENT_DATE, 3, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user16', CURRENT_DATE, 4, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user17', CURRENT_DATE, 5, 1);


-- 총 11개의 테마가 예약됨. 하지만 10개만 인기 테마로 나와야함.
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user18', DATEADD('DAY', -1, CURRENT_DATE), 6, 9);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user19', DATEADD('DAY', -1, CURRENT_DATE), 6, 10);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user20', DATEADD('DAY', -1, CURRENT_DATE), 6, 11);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user21', DATEADD('DAY', -1, CURRENT_DATE), 6, 12);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user22', DATEADD('DAY', -1, CURRENT_DATE), 6, 5);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user23', DATEADD('DAY', -1, CURRENT_DATE), 6, 6);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user24', DATEADD('DAY', -1, CURRENT_DATE), 6, 7);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user25', DATEADD('DAY', -1, CURRENT_DATE), 6, 8);

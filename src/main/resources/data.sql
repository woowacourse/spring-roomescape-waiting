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
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user2', DATEADD('DAY', -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user2', DATEADD('DAY', -1, CURRENT_DATE), 2, 1);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user3', DATEADD('DAY', -1, CURRENT_DATE), 3, 1);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user4', DATEADD('DAY', -5, CURRENT_DATE), 1, 1);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user5', DATEADD('DAY', -5, CURRENT_DATE), 2, 1);

-- 도시(theme_id=2): 5일 전 4건 = 4건  + 8일 전 2건
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user6', DATEADD('DAY', -5, CURRENT_DATE), 1, 2);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user7', DATEADD('DAY', -5, CURRENT_DATE), 2, 2);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user8', DATEADD('DAY', -5, CURRENT_DATE), 3, 2);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user9', DATEADD('DAY', -5, CURRENT_DATE), 4, 2);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user10', DATEADD('DAY', -8, CURRENT_DATE), 1, 2);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user11', DATEADD('DAY', -8, CURRENT_DATE), 2, 2);

-- 열기구(theme_id=3): 어제 1건 = 1건
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user12', DATEADD('DAY', -1, CURRENT_DATE), 1, 3);

-- 추가: 무인도(theme_id=1) 오늘 5건
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user13', CURRENT_DATE, 1, 1);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user14', CURRENT_DATE, 2, 1);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user15', CURRENT_DATE, 3, 1);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user16', CURRENT_DATE, 4, 1);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user17', CURRENT_DATE, 5, 1);


-- 총 11개의 테마가 예약됨. 하지만 10개만 인기 테마로 나와야함.
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user18', DATEADD('DAY', -1, CURRENT_DATE), 6, 9);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user19', DATEADD('DAY', -1, CURRENT_DATE), 6, 10);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user20', DATEADD('DAY', -1, CURRENT_DATE), 6, 11);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user21', DATEADD('DAY', -1, CURRENT_DATE), 6, 12);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user22', DATEADD('DAY', -1, CURRENT_DATE), 6, 5);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user23', DATEADD('DAY', -1, CURRENT_DATE), 6, 6);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user24', DATEADD('DAY', -1, CURRENT_DATE), 6, 7);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user25', DATEADD('DAY', -1, CURRENT_DATE), 6, 8);


-- ============================================
-- 미래 예약 데이터 (다양한 날짜/시간/테마 조합)
-- ============================================

-- 내일: 인기 시간대(저녁) + 인기 테마
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', 1, CURRENT_DATE), 5, 1);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user3', DATEADD('DAY', 1, CURRENT_DATE), 6, 2);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user5', DATEADD('DAY', 1, CURRENT_DATE), 4, 3);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user7', DATEADD('DAY', 1, CURRENT_DATE), 3, 4);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user10', DATEADD('DAY', 1, CURRENT_DATE), 2, 5);

-- 3일 후: 골고루 분포
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user2', DATEADD('DAY', 3, CURRENT_DATE), 1, 6);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user4', DATEADD('DAY', 3, CURRENT_DATE), 2, 7);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user6', DATEADD('DAY', 3, CURRENT_DATE), 3, 8);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user8', DATEADD('DAY', 3, CURRENT_DATE), 4, 9);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user9', DATEADD('DAY', 3, CURRENT_DATE), 5, 10);

-- 일주일 후
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user11', DATEADD('DAY', 7, CURRENT_DATE), 1, 11);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user12', DATEADD('DAY', 7, CURRENT_DATE), 2, 12);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user13', DATEADD('DAY', 7, CURRENT_DATE), 3, 13);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user14', DATEADD('DAY', 7, CURRENT_DATE), 4, 1);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user15', DATEADD('DAY', 7, CURRENT_DATE), 5, 2);

-- 2주 후
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user16', DATEADD('DAY', 14, CURRENT_DATE), 6, 3);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user17', DATEADD('DAY', 14, CURRENT_DATE), 1, 4);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user18', DATEADD('DAY', 14, CURRENT_DATE), 2, 5);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user19', DATEADD('DAY', 14, CURRENT_DATE), 3, 6);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user20', DATEADD('DAY', 14, CURRENT_DATE), 4, 7);

-- 한 달 후
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user21', DATEADD('DAY', 30, CURRENT_DATE), 5, 8);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user22', DATEADD('DAY', 30, CURRENT_DATE), 6, 9);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user23', DATEADD('DAY', 30, CURRENT_DATE), 1, 10);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user24', DATEADD('DAY', 30, CURRENT_DATE), 2, 11);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user25', DATEADD('DAY', 30, CURRENT_DATE), 3, 12);


-- ============================================
-- 활동적인 유저: 동일 유저가 여러 예약 보유 (마이페이지/내 예약 조회 테스트용)
-- ============================================

-- user1: 총 6개 예약 (과거 1 + 미래 5)
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', 5, CURRENT_DATE), 3, 2);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', 10, CURRENT_DATE), 4, 5);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', 20, CURRENT_DATE), 2, 8);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', 25, CURRENT_DATE), 6, 11);

-- user13: 총 5개 예약
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user13', DATEADD('DAY', 2, CURRENT_DATE), 6, 7);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user13', DATEADD('DAY', 9, CURRENT_DATE), 1, 9);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user13', DATEADD('DAY', 21, CURRENT_DATE), 4, 12);

-- user5: 총 4개 예약
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user5', DATEADD('DAY', 6, CURRENT_DATE), 1, 13);
INSERT INTO reservation (reserver_name, date, time_id, theme_id)
VALUES ('user5', DATEADD('DAY', 12, CURRENT_DATE), 5, 1);


-- ============================================
-- 인기 슬롯: 동일 (날짜+시간+테마)에 여러 유저 예약 (대기열 테스트용)
-- ============================================

-- 각 슬롯: 먼저 줄 선 1명이 확정(CONFIRMED), 나머지는 대기(WAITING).
-- enqueued_at을 1초씩 늦춰 대기 순번이 결정적으로 정해지게 한다.

-- 슬롯 A: 내일 15:00 무인도 탈출 (theme_id=1) — 확정 1 + 대기 2
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at)
VALUES ('user2', DATEADD('DAY', 1, CURRENT_DATE), 6, 1, 'CONFIRMED', DATEADD('SECOND', 0, CURRENT_TIMESTAMP));
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at)
VALUES ('user6', DATEADD('DAY', 1, CURRENT_DATE), 6, 1, 'WAITING', DATEADD('SECOND', 1, CURRENT_TIMESTAMP));
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at)
VALUES ('user11', DATEADD('DAY', 1, CURRENT_DATE), 6, 1, 'WAITING', DATEADD('SECOND', 2, CURRENT_TIMESTAMP));

-- 슬롯 B: 7일 후 14:00 도시 탈출 (theme_id=2) — 확정 1 + 대기 1
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at)
VALUES ('user3', DATEADD('DAY', 7, CURRENT_DATE), 5, 2, 'CONFIRMED', DATEADD('SECOND', 0, CURRENT_TIMESTAMP));
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at)
VALUES ('user8', DATEADD('DAY', 7, CURRENT_DATE), 5, 2, 'WAITING', DATEADD('SECOND', 1, CURRENT_TIMESTAMP));

-- 슬롯 C: 3일 후 15:00 열기구 탈출 (theme_id=3) — 확정 1 + 대기 3
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at)
VALUES ('user14', DATEADD('DAY', 3, CURRENT_DATE), 6, 3, 'CONFIRMED', DATEADD('SECOND', 0, CURRENT_TIMESTAMP));
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at)
VALUES ('user17', DATEADD('DAY', 3, CURRENT_DATE), 6, 3, 'WAITING', DATEADD('SECOND', 1, CURRENT_TIMESTAMP));
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at)
VALUES ('user20', DATEADD('DAY', 3, CURRENT_DATE), 6, 3, 'WAITING', DATEADD('SECOND', 2, CURRENT_TIMESTAMP));
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at)
VALUES ('user24', DATEADD('DAY', 3, CURRENT_DATE), 6, 3, 'WAITING', DATEADD('SECOND', 3, CURRENT_TIMESTAMP));

-- 슬롯 D: 2일 후 13:00 도시 탈출 (theme_id=2) — 확정 1 + 대기 3 (긴 대기열)
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at)
VALUES ('user7', DATEADD('DAY', 2, CURRENT_DATE), 4, 2, 'CONFIRMED', DATEADD('SECOND', 0, CURRENT_TIMESTAMP));
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at)
VALUES ('user1', DATEADD('DAY', 2, CURRENT_DATE), 4, 2, 'WAITING', DATEADD('SECOND', 1, CURRENT_TIMESTAMP));
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at)
VALUES ('user5', DATEADD('DAY', 2, CURRENT_DATE), 4, 2, 'WAITING', DATEADD('SECOND', 2, CURRENT_TIMESTAMP));
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at)
VALUES ('user9', DATEADD('DAY', 2, CURRENT_DATE), 4, 2, 'WAITING', DATEADD('SECOND', 3, CURRENT_TIMESTAMP));

-- 취소된 예약 데이터 (soft delete / 내 예약 취소 이력 표시 확인용)
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status)
VALUES ('user1', DATEADD('DAY', 2, CURRENT_DATE), 3, 2, 'CANCELED');
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status)
VALUES ('user1', DATEADD('DAY', 4, CURRENT_DATE), 5, 1, 'CANCELED');
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status)
VALUES ('user2', DATEADD('DAY', 2, CURRENT_DATE), 4, 2, 'CANCELED');
-- 과거(최근 7일 내) 취소 건: 인기 테마 집계에서 제외되는지 확인용
INSERT INTO reservation (reserver_name, date, time_id, theme_id, status)
VALUES ('user5', DATEADD('DAY', -3, CURRENT_DATE), 1, 1, 'CANCELED');

-- RESERVATION_TIME: 10:00 ~ 20:00 (1시간 단위, 11개)
INSERT INTO RESERVATION_TIME (start_at) VALUES ('10:00');
INSERT INTO RESERVATION_TIME (start_at) VALUES ('11:00');
INSERT INTO RESERVATION_TIME (start_at) VALUES ('12:00');
INSERT INTO RESERVATION_TIME (start_at) VALUES ('13:00');
INSERT INTO RESERVATION_TIME (start_at) VALUES ('14:00');
INSERT INTO RESERVATION_TIME (start_at) VALUES ('15:00');
INSERT INTO RESERVATION_TIME (start_at) VALUES ('16:00');
INSERT INTO RESERVATION_TIME (start_at) VALUES ('17:00');
INSERT INTO RESERVATION_TIME (start_at) VALUES ('18:00');
INSERT INTO RESERVATION_TIME (start_at) VALUES ('19:00');
INSERT INTO RESERVATION_TIME (start_at) VALUES ('20:00');

-- THEME: 5개 (인기도 차별화)
INSERT INTO THEME (name, description, thumbnail_url) VALUES ('공포의 저택', '으스스한 저택에서 탈출하세요', 'https://img.khan.co.kr/news/2021/12/31/l_2022010101000053900351432.jpg');
INSERT INTO THEME (name, description, thumbnail_url) VALUES ('우주 탐험', '광활한 우주의 비밀을 풀어보세요', 'https://imagescdn.gettyimagesbank.com/500/201801/jv11110379.jpg');
INSERT INTO THEME (name, description, thumbnail_url) VALUES ('마법 학교', '마법 학교의 숨겨진 비밀을 찾아라', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRHtPE2r2OnsA7PLT22YBX4EWbmWXcDDDyPcw&s');
INSERT INTO THEME (name, description, thumbnail_url) VALUES ('고대 유적', '고대 문명의 유적을 탐험하세요', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTbfoc4tfrkbUaKHBGhvdiTtoyzUmh3YNRsuw&s');
INSERT INTO THEME (name, description, thumbnail_url) VALUES ('탐정 사무소', '미스터리 사건을 해결하세요', 'https://img.freepik.com/free-photo/private-detective-empty-workplace-with-crime-case-evidences-board-hanging-desk-police-investigator-office-surrounded-with-murder-scene-photos-clues-night-time_482257-59756.jpg?semt=ais_hybrid&w=740&q=80');

-- RESERVATION: 30개 (2026-05-07 ~ 2026-05-14)
-- created_at: 현재 시각(2026-05-27) 이후, id 순으로 증가, 각 row 모두 날짜·시각 중복 없음
-- Theme 1 (공포의 저택): 10건 → 1위
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('김철수', '2026-05-07', 1, 1, '2026-05-28 09:12:33');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('이영희', '2026-05-07', 2, 1, '2026-05-28 11:45:07');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('박민수', '2026-05-08', 3, 1, '2026-05-28 14:30:51');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('홍길동', '2026-05-08', 4, 1, '2026-05-28 18:05:22');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('정수진', '2026-05-09', 5, 1, '2026-05-28 21:40:18');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('한동훈', '2026-05-09', 6, 1, '2026-05-29 08:15:44');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('임채원', '2026-05-10', 7, 1, '2026-05-29 10:50:09');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('서태양', '2026-05-11', 8, 1, '2026-05-29 13:22:37');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('김철수', '2026-05-12', 9, 1, '2026-05-29 16:48:55');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('유민호', '2026-05-14', 10, 1, '2026-05-29 20:11:02');

-- Theme 2 (우주 탐험): 8건 → 2위
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('강민준', '2026-05-07', 3, 2, '2026-05-30 07:33:19');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('조현아', '2026-05-08', 4, 2, '2026-05-30 09:58:41');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('김철수', '2026-05-09', 5, 2, '2026-05-30 12:27:06');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('홍길동', '2026-05-10', 6, 2, '2026-05-30 15:44:50');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('황준혁', '2026-05-10', 7, 2, '2026-05-30 19:09:28');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('송미래', '2026-05-11', 8, 2, '2026-05-31 08:41:13');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('안태양', '2026-05-12', 9, 2, '2026-05-31 11:16:39');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('배소희', '2026-05-14', 10, 2, '2026-05-31 14:52:04');

-- Theme 3 (마법 학교): 6건 → 3위
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('권지훈', '2026-05-08', 1, 3, '2026-05-31 17:30:47');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('홍길동', '2026-05-09', 2, 3, '2026-05-31 20:55:21');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('김철수', '2026-05-10', 3, 3, '2026-06-01 09:05:58');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('류지아', '2026-05-11', 4, 3, '2026-06-01 12:38:16');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('서태양', '2026-05-12', 5, 3, '2026-06-01 15:11:33');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('서태양', '2026-05-14', 6, 3, '2026-06-01 18:47:09');

-- Theme 4 (고대 유적): 4건 → 4위
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('홍길동', '2026-05-09', 7, 4, '2026-06-02 08:23:42');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('전현무', '2026-05-10', 8, 4, '2026-06-02 10:59:27');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('서태양', '2026-05-11', 9, 4, '2026-06-02 13:34:50');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('표민혁', '2026-05-12', 10, 4, '2026-06-02 16:20:15');

-- Theme 5 (탐정 사무소): 2건 → 5위
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('서태양', '2026-05-10', 11, 5, '2026-06-02 19:48:33');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('홍길동', '2026-05-14', 1, 5, '2026-06-03 09:14:06');

-- 같은 슬롯 예약 (대기 순번 테스트용): 2026-05-07, time_id=1, theme_id=1
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('대기자A', '2026-05-07', 1, 1, '2026-05-28 09:30:00');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('대기자B', '2026-05-07', 1, 1, '2026-05-28 10:00:00');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at) VALUES ('대기자C', '2026-05-07', 1, 1, '2026-05-28 10:30:00');
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

-- RESERVATION: 33개 (2026-05-23 ~ 2026-05-30)
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('김철수', '2026-05-23', 1, 1, '2026-05-21 09:12:33', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('이영희', '2026-05-23', 2, 1, '2026-05-21 11:45:07', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('박민수', '2026-05-24', 3, 1, '2026-05-22 14:30:51', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('홍길동', '2026-05-24', 4, 1, '2026-05-22 18:05:22', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('정수진', '2026-05-25', 5, 1, '2026-05-23 21:40:18', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('한동훈', '2026-05-25', 6, 1, '2026-05-24 08:15:44', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('임채원', '2026-05-26', 7, 1, '2026-05-24 10:50:09', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('서태양', '2026-05-27', 8, 1, '2026-05-25 13:22:37', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('김철수', '2026-05-28', 9, 1, '2026-05-26 16:48:55', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('유민호', '2026-05-30', 10, 1, '2026-05-28 20:11:02', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('강민준', '2026-05-23', 3, 2, '2026-05-20 07:33:19', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('조현아', '2026-05-24', 4, 2, '2026-05-22 09:58:41', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('김철수', '2026-05-25', 5, 2, '2026-05-23 12:27:06', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('홍길동', '2026-05-26', 6, 2, '2026-05-24 15:44:50', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('황준혁', '2026-05-26', 7, 2, '2026-05-25 19:09:28', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('송미래', '2026-05-27', 8, 2, '2026-05-26 08:41:13', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('안태양', '2026-05-28', 9, 2, '2026-05-27 11:16:39', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('배소희', '2026-05-30', 10, 2, '2026-05-29 14:52:04', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('권지훈', '2026-05-24', 1, 3, '2026-05-22 17:30:47', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('홍길동', '2026-05-25', 2, 3, '2026-05-23 20:55:21', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('김철수', '2026-05-26', 3, 3, '2026-05-25 09:05:58', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('류지아', '2026-05-27', 4, 3, '2026-05-26 12:38:16', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('서태양', '2026-05-28', 5, 3, '2026-05-27 15:11:33', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('서태양', '2026-05-30', 6, 3, '2026-05-29 18:47:09', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('홍길동', '2026-05-25', 7, 4, '2026-05-23 08:23:42', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('전현무', '2026-05-26', 8, 4, '2026-05-25 10:59:27', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('서태양', '2026-05-27', 9, 4, '2026-05-26 13:34:50', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('표민혁', '2026-05-28', 10, 4, '2026-05-27 16:20:15', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('서태양', '2026-05-26', 11, 5, '2026-05-24 19:48:33', 'APPROVED');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('홍길동', '2026-05-30', 1, 5, '2026-05-28 09:14:06', 'APPROVED');

-- 같은 슬롯 대기 테스트용
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('대기자A', '2026-05-23', 1, 1, '2026-05-21 09:30:00', 'WAITING');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('대기자B', '2026-05-23', 1, 1, '2026-05-21 10:00:00', 'WAITING');
INSERT INTO RESERVATION (name, date, time_id, theme_id, created_at, status) VALUES ('대기자C', '2026-05-23', 1, 1, '2026-05-21 10:30:00', 'WAITING');

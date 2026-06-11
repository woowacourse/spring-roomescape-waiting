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

-- SLOT: 30개 (date + time_id + theme_id 고유 조합)
-- Theme 1 (공포의 저택): slots 1~10
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-23', 1,  1);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-23', 2,  1);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-24', 3,  1);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-24', 4,  1);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-25', 5,  1);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-25', 6,  1);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-26', 7,  1);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-27', 8,  1);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-28', 9,  1);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-30', 10, 1);
-- Theme 2 (우주 탐험): slots 11~18
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-23', 3,  2);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-24', 4,  2);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-25', 5,  2);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-26', 6,  2);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-26', 7,  2);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-27', 8,  2);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-28', 9,  2);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-30', 10, 2);
-- Theme 3 (마법 학교): slots 19~24
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-24', 1,  3);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-25', 2,  3);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-26', 3,  3);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-27', 4,  3);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-28', 5,  3);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-30', 6,  3);
-- Theme 4 (고대 유적): slots 25~28
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-25', 7,  4);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-26', 8,  4);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-27', 9,  4);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-28', 10, 4);
-- Theme 5 (탐정 사무소): slots 29~30
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-26', 11, 5);
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-30', 1,  5);

-- RESERVATION: 33건 (slot_id 참조)
-- Theme 1 (공포의 저택): 10건 → 1위
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('김철수', 1,  'APPROVED', '2026-05-21 09:12:33');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('이영희', 2,  'APPROVED', '2026-05-21 11:45:07');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('박민수', 3,  'APPROVED', '2026-05-22 14:30:51');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('홍길동', 4,  'APPROVED', '2026-05-22 18:05:22');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('정수진', 5,  'APPROVED', '2026-05-23 21:40:18');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('한동훈', 6,  'APPROVED', '2026-05-24 08:15:44');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('임채원', 7,  'APPROVED', '2026-05-24 10:50:09');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('서태양', 8,  'APPROVED', '2026-05-25 13:22:37');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('김철수', 9,  'APPROVED', '2026-05-26 16:48:55');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('유민호', 10, 'APPROVED', '2026-05-28 20:11:02');
-- Theme 2 (우주 탐험): 8건 → 2위
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('강민준', 11, 'APPROVED', '2026-05-20 07:33:19');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('조현아', 12, 'APPROVED', '2026-05-22 09:58:41');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('김철수', 13, 'APPROVED', '2026-05-23 12:27:06');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('홍길동', 14, 'APPROVED', '2026-05-24 15:44:50');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('황준혁', 15, 'APPROVED', '2026-05-25 19:09:28');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('송미래', 16, 'APPROVED', '2026-05-26 08:41:13');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('안태양', 17, 'APPROVED', '2026-05-27 11:16:39');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('배소희', 18, 'APPROVED', '2026-05-29 14:52:04');
-- Theme 3 (마법 학교): 6건 → 3위
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('권지훈', 19, 'APPROVED', '2026-05-22 17:30:47');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('홍길동', 20, 'APPROVED', '2026-05-23 20:55:21');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('김철수', 21, 'APPROVED', '2026-05-25 09:05:58');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('류지아', 22, 'APPROVED', '2026-05-26 12:38:16');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('서태양', 23, 'APPROVED', '2026-05-27 15:11:33');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('서태양', 24, 'APPROVED', '2026-05-29 18:47:09');
-- Theme 4 (고대 유적): 4건 → 4위
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('홍길동', 25, 'APPROVED', '2026-05-23 08:23:42');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('전현무', 26, 'APPROVED', '2026-05-25 10:59:27');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('서태양', 27, 'APPROVED', '2026-05-26 13:34:50');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('표민혁', 28, 'APPROVED', '2026-05-27 16:20:15');
-- Theme 5 (탐정 사무소): 2건 → 5위
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('서태양', 29, 'APPROVED', '2026-05-24 19:48:33');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('홍길동', 30, 'APPROVED', '2026-05-28 09:14:06');
-- 같은 슬롯 대기 예약 (slot 1 공유)
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('대기자A', 1, 'WAITING', '2026-05-21 09:30:00');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('대기자B', 1, 'WAITING', '2026-05-21 10:00:00');
INSERT INTO RESERVATION (name, slot_id, status, created_at) VALUES ('대기자C', 1, 'WAITING', '2026-05-21 10:30:00');

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

-- THEME: 5개
INSERT INTO THEME (name, description, thumbnail_url)
VALUES ('공포의 저택', '으스스한 저택에서 탈출하세요', 'https://img.khan.co.kr/news/2021/12/31/l_2022010101000053900351432.jpg');
INSERT INTO THEME (name, description, thumbnail_url)
VALUES ('우주 탐험', '광활한 우주의 비밀을 풀어보세요', 'https://imagescdn.gettyimagesbank.com/500/201801/jv11110379.jpg');
INSERT INTO THEME (name, description, thumbnail_url)
VALUES ('마법 학교', '마법 학교의 숨겨진 비밀을 찾아라',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRHtPE2r2OnsA7PLT22YBX4EWbmWXcDDDyPcw&s');
INSERT INTO THEME (name, description, thumbnail_url)
VALUES ('고대 유적', '고대 문명의 유적을 탐험하세요',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTbfoc4tfrkbUaKHBGhvdiTtoyzUmh3YNRsuw&s');
INSERT INTO THEME (name, description, thumbnail_url)
VALUES ('탐정 사무소', '미스터리 사건을 해결하세요',
        'https://img.freepik.com/free-photo/private-detective-empty-workplace-with-crime-case-evidences-board-hanging-desk-police-investigator-office-surrounded-with-murder-scene-photos-clues-night-time_482257-59756.jpg?semt=ais_hybrid&w=740&q=80');

-- MEMBER: 22명
INSERT INTO MEMBER (name) VALUES ('김철수');  -- id=1
INSERT INTO MEMBER (name) VALUES ('이영희');  -- id=2
INSERT INTO MEMBER (name) VALUES ('박민수');  -- id=3
INSERT INTO MEMBER (name) VALUES ('홍길동');  -- id=4
INSERT INTO MEMBER (name) VALUES ('정수진');  -- id=5
INSERT INTO MEMBER (name) VALUES ('한동훈');  -- id=6
INSERT INTO MEMBER (name) VALUES ('임채원');  -- id=7
INSERT INTO MEMBER (name) VALUES ('서태양');  -- id=8
INSERT INTO MEMBER (name) VALUES ('유민호');  -- id=9
INSERT INTO MEMBER (name) VALUES ('강민준');  -- id=10
INSERT INTO MEMBER (name) VALUES ('조현아');  -- id=11
INSERT INTO MEMBER (name) VALUES ('황준혁');  -- id=12
INSERT INTO MEMBER (name) VALUES ('송미래');  -- id=13
INSERT INTO MEMBER (name) VALUES ('안태양');  -- id=14
INSERT INTO MEMBER (name) VALUES ('배소희');  -- id=15
INSERT INTO MEMBER (name) VALUES ('권지훈');  -- id=16
INSERT INTO MEMBER (name) VALUES ('류지아');  -- id=17
INSERT INTO MEMBER (name) VALUES ('전현무');  -- id=18
INSERT INTO MEMBER (name) VALUES ('표민혁');  -- id=19
INSERT INTO MEMBER (name) VALUES ('대기자A'); -- id=20
INSERT INTO MEMBER (name) VALUES ('대기자B'); -- id=21
INSERT INTO MEMBER (name) VALUES ('대기자C'); -- id=22

-- SLOT: 고유한 (date, time_id, theme_id) 조합 30개
-- theme_id=1
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-23', 1, 1); -- slot_id=1  (대기자 있음)
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-23', 2, 1); -- slot_id=2
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-24', 3, 1); -- slot_id=3
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-24', 4, 1); -- slot_id=4
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-25', 5, 1); -- slot_id=5
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-25', 6, 1); -- slot_id=6
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-26', 7, 1); -- slot_id=7
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-27', 8, 1); -- slot_id=8
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-28', 9, 1); -- slot_id=9
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-30', 10, 1); -- slot_id=10
-- theme_id=2
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-23', 3, 2); -- slot_id=11
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-24', 4, 2); -- slot_id=12
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-25', 5, 2); -- slot_id=13
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-26', 6, 2); -- slot_id=14
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-26', 7, 2); -- slot_id=15
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-27', 8, 2); -- slot_id=16
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-28', 9, 2); -- slot_id=17
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-30', 10, 2); -- slot_id=18
-- theme_id=3
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-24', 1, 3); -- slot_id=19
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-25', 2, 3); -- slot_id=20
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-26', 3, 3); -- slot_id=21
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-27', 4, 3); -- slot_id=22
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-28', 5, 3); -- slot_id=23
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-30', 6, 3); -- slot_id=24
-- theme_id=4
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-25', 7, 4); -- slot_id=25
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-26', 8, 4); -- slot_id=26
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-27', 9, 4); -- slot_id=27
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-28', 10, 4); -- slot_id=28
-- theme_id=5
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-26', 11, 5); -- slot_id=29
INSERT INTO SLOT (date, time_id, theme_id) VALUES ('2026-05-30', 1, 5);  -- slot_id=30

-- RESERVATION: member_id 참조
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (1,  1,  'APPROVED', '2026-05-21 09:12:33');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (2,  2,  'APPROVED', '2026-05-21 11:45:07');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (3,  3,  'APPROVED', '2026-05-22 14:30:51');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (4,  4,  'APPROVED', '2026-05-22 18:05:22');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (5,  5,  'APPROVED', '2026-05-23 21:40:18');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (6,  6,  'APPROVED', '2026-05-24 08:15:44');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (7,  7,  'APPROVED', '2026-05-24 10:50:09');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (8,  8,  'APPROVED', '2026-05-25 13:22:37');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (9,  1,  'APPROVED', '2026-05-26 16:48:55'); -- 김철수
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (10, 9,  'APPROVED', '2026-05-28 20:11:02');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (11, 10, 'APPROVED', '2026-05-20 07:33:19');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (12, 11, 'APPROVED', '2026-05-22 09:58:41');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (13, 1,  'APPROVED', '2026-05-23 12:27:06'); -- 김철수
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (14, 4,  'APPROVED', '2026-05-24 15:44:50'); -- 홍길동
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (15, 12, 'APPROVED', '2026-05-25 19:09:28');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (16, 13, 'APPROVED', '2026-05-26 08:41:13');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (17, 14, 'APPROVED', '2026-05-27 11:16:39');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (18, 15, 'APPROVED', '2026-05-29 14:52:04');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (19, 16, 'APPROVED', '2026-05-22 17:30:47');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (20, 4,  'APPROVED', '2026-05-23 20:55:21'); -- 홍길동
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (21, 1,  'APPROVED', '2026-05-25 09:05:58'); -- 김철수
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (22, 17, 'APPROVED', '2026-05-26 12:38:16');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (23, 8,  'APPROVED', '2026-05-27 15:11:33'); -- 서태양
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (24, 8,  'APPROVED', '2026-05-29 18:47:09'); -- 서태양
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (25, 4,  'APPROVED', '2026-05-23 08:23:42'); -- 홍길동
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (26, 18, 'APPROVED', '2026-05-25 10:59:27');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (27, 8,  'APPROVED', '2026-05-26 13:34:50'); -- 서태양
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (28, 19, 'APPROVED', '2026-05-27 16:20:15');
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (29, 8,  'APPROVED', '2026-05-24 19:48:33'); -- 서태양
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (30, 4,  'APPROVED', '2026-05-28 09:14:06'); -- 홍길동

-- 같은 슬롯(slot_id=1) 대기 테스트용
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (1, 20, 'WAITING', '2026-05-21 09:30:00'); -- 대기자A
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (1, 21, 'WAITING', '2026-05-21 10:00:00'); -- 대기자B
INSERT INTO RESERVATION (slot_id, member_id, status, created_at) VALUES (1, 22, 'WAITING', '2026-05-21 10:30:00'); -- 대기자C

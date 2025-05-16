-- 1. 예약 시간
INSERT INTO reservation_time (start_at)
VALUES ('10:00');
INSERT INTO reservation_time (start_at)
VALUES ('11:30');
INSERT INTO reservation_time (start_at)
VALUES ('13:00');
INSERT INTO reservation_time (start_at)
VALUES ('14:30');
INSERT INTO reservation_time (start_at)
VALUES ('16:00');
INSERT INTO reservation_time (start_at)
VALUES ('17:30');
INSERT INTO reservation_time (start_at)
VALUES ('19:00');
INSERT INTO reservation_time (start_at)
VALUES ('20:30');

-- 2. 테마
INSERT INTO theme (name, description, thumbnail)
VALUES ('공포의 저택', '무서운 분위기의 탈출 게임', 'https://ibb.co/LX9kvnB0');
INSERT INTO theme (name, description, thumbnail)
VALUES ('미래 도시', 'SF 컨셉의 퍼즐 탈출', 'https://ibb.co/nMfdprVX');
INSERT INTO theme (name, description, thumbnail)
VALUES ('탐정 사무소', '추리력을 시험하는 사건 해결', 'https://ibb.co/ksW1qjQr');
INSERT INTO theme (name, description, thumbnail)
VALUES ('마법 학교', '마법 세계를 배경으로 한 탈출', 'https://ibb.co/fR41HgN');

-- 3. 회원
INSERT INTO member (name, email, password, role)
VALUES ('슬링키', 'minki@naver.com', '1234', 'ADMIN');
INSERT INTO member (name, email, password, role)
VALUES ('메롱유저', 'melong@naver.com', '1234', 'USER');

-- 4. 예약 (member_id는 슬링키의 id 1로 고정)
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -7, CURRENT_DATE), 1, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -7, CURRENT_DATE), 2, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 3, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 4, 4, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 5, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 6, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 7, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 8, 4, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -7, CURRENT_DATE), 1, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 2, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 3, 3, 1);

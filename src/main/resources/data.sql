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
INSERT INTO theme (name, description, thumbnail)
VALUES ('사막의 보물', '보물을 찾는 어드벤처 테마', '');
INSERT INTO theme (name, description, thumbnail)
VALUES ('해적선', '해적선을 탈출하는 스릴', '');
INSERT INTO theme (name, description, thumbnail)
VALUES ('서부의 결투', '서부시대 배경 추리', '');
INSERT INTO theme (name, description, thumbnail)
VALUES ('미궁 속으로', '미로 탈출 테마', '');
INSERT INTO theme (name, description, thumbnail)
VALUES ('외계인 실험실', 'SF 실험실 탈출', '');
INSERT INTO theme (name, description, thumbnail)
VALUES ('요괴의 숲', '전통적 요소와 공포 결합', '');
INSERT INTO theme (name, description, thumbnail)
VALUES ('마피아의 비밀', '범죄 수사 추리', '');
INSERT INTO theme (name, description, thumbnail)
VALUES ('백룸', '공간왜곡 테마', '');

-- 3. 회원
INSERT INTO member (name, email, password, role)
VALUES ('슬링키', 'minki@naver.com', '1234', 'ADMIN');
INSERT INTO member (name, email, password, role)
VALUES ('메롱유저', 'melong@naver.com', '1234', 'USER');

-- 4. 예약 (member_id는 슬링키의 id 1로 고정)
-- 날짜는 최근 7일 내 (DATEADD 사용)

-- theme 1 (10회)
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 2, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 3, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 4, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 5, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 6, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -7, CURRENT_DATE), 7, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (CURRENT_DATE, 8, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (CURRENT_DATE, 1, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (CURRENT_DATE, 2, 1, 1);

-- theme 2 (9회)
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 2, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 3, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 4, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 5, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 6, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -7, CURRENT_DATE), 7, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (CURRENT_DATE, 8, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (CURRENT_DATE, 1, 2, 1);

-- theme 3 (8회)
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 2, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 3, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 4, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 5, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 6, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -7, CURRENT_DATE), 7, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (CURRENT_DATE, 8, 3, 1);

-- theme 4 (7회)
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 4, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 2, 4, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 3, 4, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 4, 4, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 5, 4, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 6, 4, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -7, CURRENT_DATE), 7, 4, 1);

-- theme 5 (6회)
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 5, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 2, 5, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 3, 5, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 4, 5, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 5, 5, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 6, 5, 1);

-- theme 6 (5회)
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 6, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 2, 6, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 3, 6, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 4, 6, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 5, 6, 1);

-- theme 7 (4회)
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 7, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 2, 7, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 3, 7, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 4, 7, 1);

-- theme 8 (3회)
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 8, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 2, 8, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 3, 8, 1);

-- theme 9 (2회)
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 9, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 2, 9, 1);

-- theme 10 (1회)
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 10, 1);

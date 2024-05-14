-- 회원
INSERT INTO member (name, email, password, role)
VALUES
('러너덕', 'duck@email.com', '1234', 'ADMIN'),
('카키', 'kaki@email.com', '1234', default),
('솔라', 'solar@email.com', '1234', default),
('브라운', 'brown@email.com', '1234', default),
('네오', 'neo@email.com', '1234', default),
('브리', 'bre@email.com', '1234', default),
('포비', 'pobi@email.com', '1234', default),
('구구', 'googoo@email.com', '1234', default),
('토미', 'tomi@email.com', '1234', default),
('리사', 'risa@email.com', '1234', default);
ALTER TABLE member ALTER COLUMN id RESTART WITH 11;

-- 에약 시간
INSERT INTO reservation_time (start_at)
VALUES
('10:00'),
('10:30'),
('11:00'),
('11:30'),
('12:00'),
('12:30'),
('13:00'),
('13:30'),
('14:00'),
('14:30');
ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 11;

-- 테마
INSERT INTO theme (name, description, thumbnail)
VALUES
('공포', '무서워요', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
('SF', '미래', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
('원숭이 사원', '원숭이들의 공격', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
('나가야 산다', '빨리 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
('좀비 사태', '좀비들의 공격', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
('공포의 놀이공원', '놀이공원 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
('지하실', '지하실 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
('타이타닉', '타이타닉에서 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
('미술관을 털어라', '미술관을 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
( '바이러스', '바이러스를 막으세요', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
( '프리즌 브레이크', '감옥을 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
( '아즈텍 신전', '신전을 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
( '우주 정거장', '우주 정거장을 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
( '치과의사', '치과의사를 피해 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
( '비밀요원', '비밀요원이 돼 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
ALTER TABLE theme ALTER COLUMN id RESTART WITH 16;

-- 예약
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES
(1, CURRENT_DATE, 1, 1),
(2, CURRENT_DATE, 1, 2),
(3, CURRENT_DATE, 1, 3),
(4, CURRENT_DATE, 1, 4),
(5, CURRENT_DATE, 1, 5),
(6, CURRENT_DATE, 1, 6),
(7, CURRENT_DATE, 1, 7),
(8, CURRENT_DATE, 1, 8),
(9, CURRENT_DATE, 1, 9),
(1, DATEADD('DAY', 1, CURRENT_DATE), 10, 10),
(2, DATEADD('DAY', 1, CURRENT_DATE), 1, 11),
(3, DATEADD('DAY', 1, CURRENT_DATE), 2, 12),
(4, DATEADD('DAY', 1, CURRENT_DATE), 3, 13),
(5, DATEADD('DAY', 1, CURRENT_DATE), 4, 14),
(1, DATEADD('DAY', 2, CURRENT_DATE), 5, 15),
(2, DATEADD('DAY', 2, CURRENT_DATE), 6, 1),
(3, DATEADD('DAY', 2, CURRENT_DATE), 7, 2),
(4, DATEADD('DAY', 2, CURRENT_DATE), 8, 3),
(1, DATEADD('DAY', 3, CURRENT_DATE), 9, 4),
(2, DATEADD('DAY', 3, CURRENT_DATE), 10, 5),
(3, DATEADD('DAY', 3, CURRENT_DATE), 1, 6),
(1, DATEADD('DAY', 4, CURRENT_DATE), 2, 7),
(2, DATEADD('DAY', 4, CURRENT_DATE), 3, 1),
(1, DATEADD('DAY', 5, CURRENT_DATE), 4, 1);
ALTER TABLE reservation ALTER COLUMN id RESTART WITH 25;

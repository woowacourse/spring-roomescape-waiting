INSERT INTO member (id, email, password, name, role)
values (1, 't1@t1.com', '318adf7cef291e2202e709edb3637b25e9a4553965c6494b294d6ff19a18ddaa', '재즈', 'MEMBER'),
       (2, 't2@t2.com', '26d57e8a8698c950b968e2d0741a7b912f38b7b232d97b2f91a6f30a561159fb', '영이', 'MEMBER'),
       (3, 't3@t3.com', 'c1d54238087e39249c74c6a0606a58fab7c9e74af21939a8a8e392a64d4f7ceb', '워니', 'MEMBER'),
       (4, 't4@t4.com', 'fb986663302fbda8d481d3f29ee76b33f5e0a8d01919a283a6928d2b2c451fcc', '브리', 'MEMBER'),
       (5, 't5@t5.com', 'ec8984c8e025eb355cc0e038b2b88b67d4c0c70433841285860dc5b4b7cc9c28', '구구', 'MEMBER'),
       (6, 't6@t6.com', '02818d436cb96b1692fa134510d95f0ca318c22ebf448c095d15a7594defff0a', '네오', 'MEMBER'),
       (7, 't7@t7.com', 'd9890c4e4298626c888a314038be9a45bd339a079b577e308e1c1f72ae52ae01', '브라운', 'MEMBER'),
       (8, 'tt@tt.com', '5dc0de6479704206182a97b991a196347b750e5efc0217ea1d1e6cb2755f9193', '어드민', 'ADMIN');

INSERT INTO reservation_time (id, start_at)
values (1, '12:00'),
       (2, '16:00'),
       (3, '20:00');

INSERT INTO theme (id, name, description, thumbnail)
values (1, '꿈나라', 'zZZ',
        'https://bit.ly/3WS8AVv'),
       (2, '자바를 자바라', '자바를 잡지 못하면 당신은 백엔드로써 자격이 없습니다. 프론트로 가게됩니다.',
        'https://bit.ly/3UUTlZx'),
       (3, '이불', '난이도 ★★★★★',
        'https://bit.ly/3WSqOGy');

INSERT INTO reservation (id, member_id, theme_id, date, reservation_time_id, status, created_at)
values (1, 4, 3, '2024-05-18', 3, 'CONFIRMED', '2024-05-11 03:36:00'),
       (2, 3, 2, '2024-05-25', 2, 'CONFIRMED', '2024-05-11 14:50:00'),
       (3, 5, 2, '2024-05-18', 1, 'CONFIRMED', '2024-05-12 21:36:00'),
       (4, 6, 3, '2024-05-18', 1, 'CONFIRMED', '2024-05-14 13:32:00'),
       (5, 3, 3, '2024-05-17', 1, 'CONFIRMED', '2024-05-14 20:12:20'),
       (6, 1, 1, '2024-05-16', 2, 'CONFIRMED', '2024-05-14 20:36:00'),
       (7, 7, 3, '2024-05-19', 2, 'CONFIRMED', '2024-05-15 09:22:00'),
       (8, 2, 1, '2024-05-17', 2, 'CONFIRMED', '2024-05-15 16:34:00'),
       (9, 2, 3, '2024-05-25', 2, 'CONFIRMED', '2024-05-18 14:43:00'),
       (10, 4, 2, '2024-05-25', 2, 'WAITING', '2024-05-18 17:03:00'),
       (11, 1, 2, '2024-05-25', 2, 'WAITING', '2024-05-19 19:37:00'),
       (12, 4, 2, '2024-05-29', 1, 'CONFIRMED', '2024-05-21 11:00:00'),
       (13, 3, 2, '2024-05-30', 1, 'CONFIRMED', '2024-05-22 11:04:00'),
       (14, 7, 1, '2024-05-26', 1, 'CONFIRMED', '2024-05-22 12:12:00'),
       (15, 5, 1, '2024-05-26', 1, 'WAITING', '2024-05-23 00:43:00'),
       (16, 5, 3, '2024-05-26', 1, 'CONFIRMED', '2024-05-23 00:45:00'),
       (17, 6, 3, '2024-05-26', 1, 'WAITING', '2024-05-23 00:50:00'),
       (18, 2, 2, '2024-05-29', 1, 'WAITING', '2024-05-23 16:40:00'),
       (19, 1, 3, '2024-05-26', 1, 'WAITING', '2024-05-24 13:30:00'),
       (20, 1, 2, '2024-05-30', 1, 'WAITING', '2024-05-24 16:54:00'),
       (21, 1, 2, '2024-05-29', 1, 'WAITING', '2024-05-24 17:33:00');

ALTER TABLE member
    ALTER COLUMN id RESTART WITH 9;
ALTER TABLE reservation_time
    ALTER COLUMN id RESTART WITH 4;
ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 4;
ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 22;

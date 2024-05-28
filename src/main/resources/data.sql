-- 테마 목록 : 11개
INSERT INTO theme (name, description, thumbnail)
VALUES ('테마1', '재밌는 테마입니다', 'https://cdn.pixabay.com/photo/2016/01/22/11/50/live-escape-game-1155620_1280.jpg'),
       ('테마2', '재밌는 테마입니다', 'https://cdn.pixabay.com/photo/2016/01/22/11/50/live-escape-game-1155620_1280.jpg'),
       ('테마3', '재밌는 테마입니다', 'https://cdn.pixabay.com/photo/2016/01/22/11/50/live-escape-game-1155620_1280.jpg'),
       ('테마4', '재밌는 테마입니다', 'https://cdn.pixabay.com/photo/2016/01/22/11/50/live-escape-game-1155620_1280.jpg'),
       ('테마5', '재밌는 테마입니다', 'https://cdn.pixabay.com/photo/2016/01/22/11/50/live-escape-game-1155620_1280.jpg'),
       ('테마6', '재밌는 테마입니다', 'https://cdn.pixabay.com/photo/2016/01/22/11/50/live-escape-game-1155620_1280.jpg'),
       ('테마7', '재밌는 테마입니다', 'https://cdn.pixabay.com/photo/2016/01/22/11/50/live-escape-game-1155620_1280.jpg'),
       ('테마8', '재밌는 테마입니다', 'https://cdn.pixabay.com/photo/2016/01/22/11/50/live-escape-game-1155620_1280.jpg'),
       ('테마9', '재밌는 테마입니다', 'https://cdn.pixabay.com/photo/2016/01/22/11/50/live-escape-game-1155620_1280.jpg'),
       ('테마10', '재밌는 테마입니다', 'https://cdn.pixabay.com/photo/2016/01/22/11/50/live-escape-game-1155620_1280.jpg'),
       ('테마11', '재밌는 테마입니다', 'https://cdn.pixabay.com/photo/2016/01/22/11/50/live-escape-game-1155620_1280.jpg');

-- 예약 시간 목록 : 5개
INSERT INTO reservation_time (start_at)
VALUES ('08:00'),
       ('10:00'),
       ('13:00'),
       ('21:00'),
       ('23:00');

-- 유저 목록 : 2개
INSERT INTO member(name, email, password, role)
VALUES ('admin','admin@email.com', 'f6f2ea8f45d8a057c9566a33f99474da2e5c6a6604d736121650e2730c6fb0a3', 'ADMIN'),
       ('member1', 'email1@email.com', 'f6f2ea8f45d8a057c9566a33f99474da2e5c6a6604d736121650e2730c6fb0a3', 'MEMBER'),
       ('member2', 'email2@email.com', 'f6f2ea8f45d8a057c9566a33f99474da2e5c6a6604d736121650e2730c6fb0a3', 'MEMBER'),
       ('member3', 'email3@email.com', 'f6f2ea8f45d8a057c9566a33f99474da2e5c6a6604d736121650e2730c6fb0a3', 'MEMBER');

-- 8, 4, 2, 5, 2, 3, 1, 1, 1, 1, 1
-- 내림차순 정렬 ID : 1, 4, 2, 6, 3, 5, 7, 8, 9, 10, 11
-- 테마 1 예약 목록 : 8개
INSERT INTO reservation (date, time_id, theme_id, reservation_member_id, status)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 1, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 2, 1, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 3, 1, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 4, 1, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 5, 1, 1, 'APPROVED'),
       (DATEADD('DAY', 1, CURRENT_DATE), 1, 1, 2, 'APPROVED'),  -- 동일한 날짜/시간/테마에 member1 예약
       (DATEADD('DAY', 1, CURRENT_DATE), 1, 1, 3, 'PENDING'),   -- 동일한 날짜/시간/테마에 member2 예약 대기
       (DATEADD('DAY', 1, CURRENT_DATE), 1, 1, 4, 'PENDING');   -- 동일한 날짜/시간/테마에 member3 예약 대기

-- 테마 2 예약 목록 : 4개
INSERT INTO reservation (date, time_id, theme_id, reservation_member_id, status)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 2, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 2, 2, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 3, 2, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 4, 2, 1, 'APPROVED');

-- 테마 3 예약 목록 : 2개
INSERT INTO reservation (date, time_id, theme_id, reservation_member_id, status)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 3, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 2, 3, 1, 'APPROVED');

-- 테마 4 예약 목록 : 5개
INSERT INTO reservation (date, time_id, theme_id, reservation_member_id, status)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 4, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 2, 4, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 3, 4, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 4, 4, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 5, 4, 1, 'APPROVED');

-- 테마 5 예약 목록 : 2개
INSERT INTO reservation (date, time_id, theme_id, reservation_member_id, status)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 5, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 5, 5, 1, 'APPROVED');

-- 테마 6 예약 목록 : 3개
INSERT INTO reservation (date, time_id, theme_id, reservation_member_id, status)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 6, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 2, 6, 1, 'APPROVED'),
       (DATEADD('DAY', -3, CURRENT_DATE), 3, 6, 1, 'APPROVED');

-- 테마 7 예약 목록
INSERT INTO reservation (date, time_id, theme_id, reservation_member_id, status)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 7, 1, 'APPROVED');

-- 테마 8 예약 목록
INSERT INTO reservation (date, time_id, theme_id, reservation_member_id, status)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 8, 1, 'APPROVED');

-- 테마 9 예약 목록
INSERT INTO reservation (date, time_id, theme_id, reservation_member_id, status)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 9, 1, 'APPROVED');

-- 테마 10 예약 목록
INSERT INTO reservation (date, time_id, theme_id, reservation_member_id, status)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 10, 1, 'APPROVED');

-- 테마 11 예약 목록
INSERT INTO reservation (date, time_id, theme_id, reservation_member_id, status)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 5, 11, 1, 'APPROVED');

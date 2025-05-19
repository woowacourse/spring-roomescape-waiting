INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('11:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('테마1', '테마 1입니다', '썸네일1'),
       ('테마2', '테마 2입니다', '썸네일2'),
       ('테마3', '테마 3입니다', '썸네일3'),
       ('테마4', '테마 4입니다', '썸네일4'),
       ('테마5', '테마 5입니다', '썸네일5'),
       ('테마6', '테마 6입니다', '썸네일6'),
       ('테마7', '테마 7입니다', '썸네일7'),
       ('테마8', '테마 8입니다', '썸네일8'),
       ('테마9', '테마 9입니다', '썸네일9'),
       ('테마10', '테마 10입니다', '썸네일10'),
       ('테마11', '테마 11입니다', '썸네일11');

INSERT INTO member (name, email, password, role)
VALUES ('어드민', 'admin@email.com', 'password', 'ADMIN'),
       ('브라운', 'brown@email.com', 'brown', 'USER'),
       ('브리', 'brie@email.com', 'brie', 'USER'),
       ('솔라', 'solar@email.com', 'solar', 'USER');

INSERT INTO reservation (member_id, theme_id, date, time_id, status)
VALUES (2, 1, '2025-05-07', 1, 'RESERVED'),
       (4, 1, '2025-05-08', 2, 'RESERVED'),
       (3, 1, '2025-05-09', 1, 'RESERVED'),
       (3, 1, '2025-05-11', 1, 'RESERVED'),
       (3, 2, '2025-05-11', 1, 'RESERVED'),
       (3, 2, '2025-05-12', 1, 'RESERVED'),
       (3, 2, '2025-05-12', 1, 'RESERVED'),
       (3, 8, '2025-05-12', 1, 'RESERVED'),
       (3, 8, '2025-05-12', 1, 'RESERVED'),
       (3, 8, '2025-05-12', 1, 'RESERVED'),
       (3, 9, '2025-05-12', 1, 'RESERVED'),
       (3, 11, '2025-05-12', 1, 'RESERVED'),
       (1, 11, '2025-05-12', 1, 'RESERVED');

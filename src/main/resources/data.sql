SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE member;
TRUNCATE TABLE reservation;
TRUNCATE TABLE theme;
TRUNCATE TABLE reservation_time;
SET FOREIGN_KEY_CHECKS = 1;

-- member
INSERT INTO member (name, role, email, password)
VALUES
    ("admin", "ADMIN", "admin@email.com", "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"),
    ("유저1", "USER", "user1@email.com", "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"),
    ("유저2", "USER", "user2@email.com", "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4");

-- reservation_time
INSERT INTO reservation_time (start_at)
VALUES
    ('10:00'),
    ('11:00'),
    ('12:00');

-- theme
INSERT INTO theme (name, description, thumbnail)
VALUES
    ('레벨2 탈출', '우테코 레벨2를 탈출하는 내용입니다.','https://example.com/image.jpg'),
    ('지하 감옥', '깊은 감옥에서 탈출하라!', 'https://example.com/jail.jpg');

-- reservation
INSERT INTO reservation (member_id, date, time_id, theme_id, status, reserved_at)
VALUES
    (2, '2025-05-29', 2, 1, 'RESERVED', '2025-05-28 10:00:00'),
    (3, '2025-05-28', 3, 2, 'RESERVED', '2025-05-27 10:00:00'),
    (2, '2025-05-27', 1, 1, 'RESERVED', '2025-05-26 10:00:00'),
    (3, '2025-05-26', 2, 2, 'RESERVED', '2025-05-25 10:00:00'),
    (2, '2025-05-25', 3, 1, 'RESERVED', '2025-05-24 10:00:00'),
    (3, '2025-05-24', 1, 2, 'RESERVED', '2025-05-23 10:00:00'),
    (2, '2025-05-23', 2, 1, 'RESERVED', '2025-05-22 10:00:00'),
    (3, '2025-05-22', 3, 2, 'RESERVED', '2025-05-21 10:00:00'),
    (2, '2025-05-21', 1, 1, 'RESERVED', '2025-05-20 10:00:00'),
    (3, '2025-05-20', 2, 2, 'RESERVED', '2025-05-19 10:00:00'),
    (2, '2025-05-30', 2, 2, 'RESERVED', '2025-05-29 10:00:00'),
    (3, '2025-05-31', 3, 1, 'RESERVED', '2025-05-30 10:00:00'),
    (2, '2025-06-01', 1, 2, 'RESERVED', '2025-05-31 10:00:00'),
    (3, '2025-06-02', 2, 1, 'RESERVED', '2025-06-01 10:00:00'),
    (2, '2025-06-03', 3, 2, 'RESERVED', '2025-06-02 10:00:00'),
    (3, '2025-06-04', 1, 1, 'RESERVED', '2025-06-03 10:00:00'),
    (2, '2025-06-05', 2, 2, 'RESERVED', '2025-06-04 10:00:00');

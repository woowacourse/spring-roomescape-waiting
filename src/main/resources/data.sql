SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE member;
TRUNCATE TABLE reservation;
TRUNCATE TABLE theme;
TRUNCATE TABLE reservation_time;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO member (name, role, email, password)
VALUES ("admin", "ADMIN", "admin@email.com", "1234");
INSERT INTO member (name, role, email, password)
VALUES ("유저1", "USER", "user1@email.com", "1234");
INSERT INTO member (name, role, email, password)
VALUES ("유저2", "USER", "user2@email.com", "1234");

-- reservation_time
INSERT INTO reservation_time (start_at)
VALUES ('10:00');
INSERT INTO reservation_time (start_at)
VALUES ('11:00');
INSERT INTO reservation_time (start_at)
VALUES ('12:00');

-- theme
INSERT INTO theme (name, description, thumbnail)
VALUES ('레벨2 탈출',
        '우테코 레벨2를 탈출하는 내용입니다.',
        'https://example.com/image.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('지하 감옥',
        '깊은 감옥에서 탈출하라!',
        'https://example.com/jail.jpg');

-- reservation: 오래된 데이터
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (2, '2025-05-08', 2, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (3, '2025-05-07', 3, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (2, '2025-05-06', 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (3, '2025-05-05', 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (2, '2025-05-04', 3, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (3, '2025-05-03', 1, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (2, '2025-05-02', 2, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (3, '2025-05-01', 3, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (2, '2025-04-30', 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (3, '2025-04-29', 2, 2);

-- reservation: 최근 데이터
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (2, '2025-05-09', 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (3, '2025-05-10', 3, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (2, '2025-05-11', 1, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (3, '2025-05-12', 2, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (2, '2025-05-13', 3, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (3, '2025-05-14', 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (2, '2025-05-15', 2, 2);

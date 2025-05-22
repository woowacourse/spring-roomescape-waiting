DELETE FROM reservation;
DELETE FROM reservation_time;
DELETE FROM theme;
DELETE FROM member;

INSERT INTO member(name, email, password, role)
VALUES ('훌라', 'test@test.com', 'test', 'USER'),
       ('어드민', 'admin@admin.com', 'admin', 'ADMIN'),
       ('김모험', 'qwe@qwe.com', 'qwe', 'USER'),
       ('이추리', 'detective@escape.com', 'password', 'USER');

INSERT INTO theme (name, description, thumbnail)
VALUES ('공포의 방', '소름 끼치는 공포 테마', 'url'),
       ('추리의 방', '논리력으로 푸는 추리 테마', 'url'),
       ('시간 여행자', '과거와 미래를 넘나드는 방 탈출', 'url');

INSERT INTO reservation_time (start_at)
VALUES ('10:00'), ('12:00'), ('14:00'), ('16:00');

INSERT INTO reservation (member_id, date, time_id, theme_id, type)
VALUES
(1, '2025-05-25', 1, 1, 'confirm'),
(1, '2025-05-26', 2, 1, 'confirm'),
(3, '2025-06-01', 3, 2, 'confirm'),
(4, '2025-06-02', 1, 3, 'confirm'),
(3, '2025-06-03', 2, 2, 'confirm'),
(4, '2025-06-03', 4, 3, 'confirm'),
(3, '2025-06-04', 3, 1, 'confirm'),
(4, '2025-06-05', 4, 2, 'confirm');

INSERT INTO reservation (member_id, date, time_id, theme_id, type)
VALUES
(2, '2025-05-25', 1, 1, 'waiting'),
(2, '2025-05-26', 2, 1, 'waiting'),
(1, '2025-06-03', 2, 2, 'waiting'),
(2, '2025-06-05', 4, 2, 'waiting');

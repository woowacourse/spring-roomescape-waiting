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

--INSERT INTO reservation (id, member_id, date, time_id, theme_id)
--VALUES
--(1, 1, '2025-05-25', 1, 1),
--(2, 3, '2025-06-01', 3, 2),
--(3, 4, '2025-06-02', 1, 3),
--(4, 1, '2025-06-03', 2, 2);
--
--INSERT INTO confirmed_reservation (id) VALUES (1), (2), (3), (4);
--
--INSERT INTO reservation (id, member_id, date, time_id, theme_id)
--VALUES
--(5, 2, '2025-05-25', 1, 1),
--(6, 4, '2025-06-01', 3, 2),
--(7, 3, '2025-06-02', 1, 3),
--(8, 2, '2025-06-03', 2, 2);
--
--INSERT INTO waiting_reservation (id) VALUES (5), (6), (7), (8);

SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE waiting RESTART IDENTITY;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
TRUNCATE TABLE member RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO member(name, email, password, role)
VALUES ('테드', 'test1@email.com', '1450575459', 'USER'),
       ('아톰', 'test2@email.com', '1450575459', 'USER'),
       ('종이', 'test3@email.com', '1450575459', 'USER'),
       ('오리', 'test4@email.com', '1450575459', 'USER');

INSERT INTO theme (theme_name, description, thumbnail)
VALUES ('테마1', '테마1 설명 설명 설명', 'thumbnail1.jpg');

INSERT INTO reservation_time (start_at)
VALUES ('10:00');

INSERT INTO reservation (date, time_id, theme_id, member_id, status)
VALUES (DATEADD('DAY', 1, CURRENT_DATE), 1, 1, 1, 'RESERVED'),
       (DATEADD('DAY', 1, CURRENT_DATE), 1, 1, 2, 'WAITING'),
       (DATEADD('DAY', 1, CURRENT_DATE), 1, 1, 3, 'WAITING');

INSERT INTO waiting (reservation_id, waiting_order)
VALUES (2, 1),
       (3, 2);

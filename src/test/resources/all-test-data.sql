SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
TRUNCATE TABLE member RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO member(name, email, password, role)
VALUES ('어드민', 'admin@email.com', '1450575459', 'ADMIN'),
       ('테드', 'ted@email.com', '1450575459', 'USER'),
       ('아톰', 'atom@email.com', '1450575459', 'USER');

INSERT INTO theme (theme_name, description, thumbnail)
VALUES ('테마1', '테마1 설명 설명 설명', 'thumbnail1.jpg'),
       ('테마2', '테마2 설명 설명 설명', 'thumbnail2.jpg');

INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('11:00'),
       ('12:00');

INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES (CURRENT_DATE() - INTERVAL '1' DAY, 1, 1, 1),
       (CURRENT_DATE() + INTERVAL '1' DAY, 1, 1, 1),
       (CURRENT_DATE() + INTERVAL '1' DAY, 2, 1, 1),
       (CURRENT_DATE() + INTERVAL '2' DAY, 2, 1, 1);

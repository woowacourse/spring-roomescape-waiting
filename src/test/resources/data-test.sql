SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE theme RESTART IDENTITY;
TRUNCATE TABLE time RESTART IDENTITY;
TRUNCATE TABLE member RESTART IDENTITY;
TRUNCATE TABLE reservation RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO theme (id, name, description, thumbnail) VALUES (1, '테마1', '설명1' ,'https://image.jpg');
INSERT INTO time (id, start_at) VALUES (1, '10:00');
INSERT INTO member(id, name, email, password, role) VALUES (1, '켬미', 'aaa@naver.com', '1111', 1);
INSERT INTO reservation (id, date, member_id, time_id, theme_id) VALUES (999, '2023-08-05', 1, 1, 1);


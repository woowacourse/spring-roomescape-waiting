SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE reservation;
TRUNCATE TABLE reservation_time;
TRUNCATE TABLE theme;
TRUNCATE TABLE member;

ALTER TABLE reservation ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE reservation_time ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE theme ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE member ALTER COLUMN ID RESTART WITH 1;

SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO theme(name, description, thumbnail) VALUES ('테마1', '설명1', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('테마2', '설명2', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('테마3', '설명3', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time(start_at) VALUES ('10:00');

INSERT INTO member(name, email, password, role) VALUES('admin', 'admin@email.com', 'admin123', 'ADMIN');
INSERT INTO member(name, email, password, role) VALUES('guest', 'guest@email.com', 'guest123', 'GUEST');
INSERT INTO member(name, email, password, role) VALUES('토미', 'tomi@email.com', 'tomi123', 'GUEST');


INSERT INTO reservation(date, member_id, time_id, theme_id) VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 1, 1);
INSERT INTO reservation(date, member_id, time_id, theme_id) VALUES (DATEADD('DAY', -7, CURRENT_DATE), 2, 1, 2);
INSERT INTO reservation(date, member_id, time_id, theme_id) VALUES (DATEADD('DAY', -8, CURRENT_DATE), 3, 1, 3);

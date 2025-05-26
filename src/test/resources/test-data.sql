DELETE FROM member;
ALTER TABLE member ALTER COLUMN id RESTART WITH 1;

DELETE FROM theme;
ALTER TABLE theme ALTER COLUMN id RESTART WITH 1;

DELETE FROM reservation_time;
ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1;

DELETE FROM reservation;
ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;

DELETE FROM waiting;
ALTER TABLE waiting ALTER COLUMN id RESTART WITH 1;

INSERT INTO member (name, email, password, role) VALUES ('어드민', 'admin@gmail.com', 'wooteco7', 'ADMIN');
INSERT INTO member (name, email, password, role) VALUES ('회원', 'user@gmail.com', 'wooteco7', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('회원2', 'user2@gmail.com', 'wooteco7', 'USER');

INSERT INTO theme (name, description, thumbnail) VALUES ('테마 A', '테마 A입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time (start_at) VALUES ('09:00');

INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, '2025-05-27', 1, 1);

INSERT INTO waiting (member_id, date, time_id, theme_id) VALUES (2, '2025-05-27', 1, 1);

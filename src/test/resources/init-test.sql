DELETE FROM reservation;
DELETE FROM reservation_time;
DELETE FROM theme;
DELETE FROM member;
ALTER TABLE reservation ALTER COLUMN id RESTART;
ALTER TABLE reservation_time ALTER COLUMN id RESTART;
ALTER TABLE theme ALTER COLUMN id RESTART;
ALTER TABLE member ALTER COLUMN id RESTART;

INSERT INTO reservation_time(start_at) VALUES ('10:00:00');
INSERT INTO reservation_time(start_at) VALUES ('19:00:00');
INSERT INTO reservation_time(start_at) VALUES ('21:00:00');

INSERT INTO theme(name, description, thumbnail) VALUES ('레벨2 탈출', '우테코 레벨2 탈출기!', 'https://img.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('레벨3 탈출', '우테코 레벨3 탈출기!', 'https://img.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('레벨4 탈출', '우테코 레벨4 탈출기!', 'https://img.jpg');

INSERT INTO member(name, email, role) VALUES ('관리자', 'admin@abc.com', 'ADMIN');
INSERT INTO member(name, email, role) VALUES ('브라운', 'brown@abc.com', 'USER');
INSERT INTO member(name, email, role) VALUES ('브리', 'bri@abc.com', 'USER');
INSERT INTO member(name, email, role) VALUES ('오리', 'duck@abc.com', 'USER');

INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, CURRENT_DATE - 1, 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (3, CURRENT_DATE - 2, 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (4, CURRENT_DATE - 2, 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, '2022-05-05', 2, 1);

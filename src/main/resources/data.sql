INSERT INTO theme (name, description, thumbnail)
VALUES ('theme1', 'description1', 'thumbnail1');
INSERT INTO theme (name, description, thumbnail)
VALUES ('theme2', 'description2', 'thumbnail2');

INSERT INTO reservation_time (start_at)
VALUES ('10:00');
INSERT INTO reservation_time (start_at)
VALUES ('11:00');

INSERT INTO member (name, email, password, `role`)
VALUES ('testUser', 'user@naver.com', '1234', 'USER');
INSERT INTO member (name, email, password, `role`)
VALUES ('testAdmin', 'admin@naver.com', '1234', 'ADMIN');
INSERT INTO reservation (member_id, date, reservation_time_id, theme_id)
VALUES (1, CURRENT_DATE + INTERVAL '1' DAY, 1, 1);

INSERT INTO theme (id, name, description, thumbnail)
VALUES (1, 'theme1', 'description1', 'thumbnail1');
INSERT INTO theme (id, name, description, thumbnail)
VALUES (2, 'theme2', 'description2', 'thumbnail2');

INSERT INTO reservation_time (id, start_at)
VALUES (1, '10:00');
INSERT INTO reservation_time (id, start_at)
VALUES (2, '11:00');

INSERT INTO member
VALUES (1, 'testUser', 'test@naver.com', '1234', 'USER');
INSERT INTO member
VALUES (2, 'testAdmin', 'admin@naver.com', '1234', 'ADMIN');

INSERT INTO reservation (member_id, date, reservation_time_id, theme_Id)
VALUES (1, CURRENT_DATE + INTERVAL '1' DAY, 1, 1);


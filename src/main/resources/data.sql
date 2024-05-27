INSERT INTO theme (name, description, thumbnail, created_at)
VALUES ('theme1', 'description1', 'thumbnail1', current_timestamp);
INSERT INTO theme (name, description, thumbnail, created_at)
VALUES ('theme2', 'description2', 'thumbnail2', current_timestamp);
INSERT INTO theme (name, description, thumbnail, created_at)
VALUES ('theme3', 'description3', 'thumbnail3', current_timestamp);

INSERT INTO reservation_time (start_at, created_at)
VALUES ('10:00', current_timestamp);
INSERT INTO reservation_time (start_at, created_at)
VALUES ('11:00', current_timestamp);
INSERT INTO reservation_time (start_at, created_at)
VALUES ('12:00', current_timestamp);

INSERT INTO member (name, email, password, `role`, created_at)
VALUES ('testUser', 'user@naver.com', '1234', 'MEMBER', current_timestamp);
INSERT INTO member (name, email, password, `role`, created_at)
VALUES ('testAdmin', 'admin@naver.com', '1234', 'ADMIN', current_timestamp);
INSERT INTO member (name, email, password, `role`, created_at)
VALUES ('testUser2', 'user2@naver.com', '1234', 'MEMBER', current_timestamp);

INSERT INTO reservation (member_id, date, reservation_time_id, theme_id, reservation_status, created_at)
VALUES (1, CURRENT_DATE + INTERVAL '1' DAY, 1, 1, 'RESERVED', current_timestamp);
INSERT INTO reservation (member_id, date, reservation_time_id, theme_id, reservation_status, created_at)
VALUES (2, CURRENT_DATE + INTERVAL '1' DAY, 1, 1, 'WAITING', current_timestamp + INTERVAL '1' MINUTE);
INSERT INTO reservation (member_id, date, reservation_time_id, theme_id, reservation_status, created_at)
VALUES (3, CURRENT_DATE + INTERVAL '1' DAY, 1, 1, 'WAITING', current_timestamp + INTERVAL '2' MINUTE);
INSERT INTO reservation (member_id, date, reservation_time_id, theme_id, reservation_status, created_at)
VALUES (1, CURRENT_DATE + INTERVAL '2' DAY, 2, 2, 'RESERVED', current_timestamp);
INSERT INTO reservation (member_id, date, reservation_time_id, theme_id, reservation_status, created_at)
VALUES (2, CURRENT_DATE + INTERVAL '2' DAY, 2, 2, 'WAITING', current_timestamp + INTERVAL '1' MINUTE);
INSERT INTO reservation (member_id, date, reservation_time_id, theme_id, reservation_status, created_at)
VALUES (3, CURRENT_DATE + INTERVAL '2' DAY, 2, 2, 'WAITING', current_timestamp + INTERVAL '2' MINUTE);

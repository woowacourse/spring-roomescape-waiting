INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 1', '설명 1', 'url 1');
INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 2', '설명 2', 'url 2');
INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 3', '설명 3', 'url 3');

INSERT INTO reservation_time (start_at)
VALUES ('12:00');
INSERT INTO reservation_time (start_at)
VALUES ('13:00');
INSERT INTO reservation_time (start_at)
VALUES ('14:00');
INSERT INTO reservation_time (start_at)
VALUES ('15:00');

INSERT INTO member (NAME, ROLE, EMAIL, PASSWORD)
VALUES ('admin', 'ADMIN', 'admin@email.com', 'password');
INSERT INTO member (NAME, ROLE, EMAIL, PASSWORD)
VALUES ('user', 'USER', 'user@email.com', 'password');
INSERT INTO member (NAME, ROLE, EMAIL, PASSWORD)
VALUES ('user2', 'USER', 'user2@email.com', 'password');

INSERT INTO reservation (date, created_at, time_id, theme_id, member_id, status)
VALUES (CURRENT_DATE - 3, CURRENT_TIMESTAMP, 1, 1, 1, 'BOOKED');
INSERT INTO reservation (date, created_at, time_id, theme_id, member_id, status)
VALUES (CURRENT_DATE - 3, CURRENT_TIMESTAMP, 2, 1, 2, 'BOOKED');
INSERT INTO reservation (date, created_at, time_id, theme_id, member_id, status)
VALUES (CURRENT_DATE - 3, CURRENT_TIMESTAMP, 3, 1, 1, 'BOOKED');
INSERT INTO reservation (date, created_at, time_id, theme_id, member_id, status)
VALUES (CURRENT_DATE - 3, CURRENT_TIMESTAMP, 4, 2, 1, 'BOOKED');
INSERT INTO reservation (date, created_at, time_id, theme_id, member_id, status)
VALUES (CURRENT_DATE - 3, CURRENT_TIMESTAMP, 1, 3, 1, 'BOOKED');
INSERT INTO reservation (date, created_at, time_id, theme_id, member_id, status)
VALUES (CURRENT_DATE - 2, CURRENT_TIMESTAMP, 1, 1, 1, 'BOOKED');
INSERT INTO reservation (date, created_at, time_id, theme_id, member_id, status)
VALUES (CURRENT_DATE - 1, CURRENT_TIMESTAMP, 1, 1, 1, 'BOOKED');
INSERT INTO reservation (date, created_at, time_id, theme_id, member_id, status)
VALUES (CURRENT_DATE - 1, CURRENT_TIMESTAMP,2, 1, 1, 'BOOKED');
INSERT INTO reservation (date, created_at, time_id, theme_id, member_id, status)
VALUES (CURRENT_DATE + 1, CURRENT_TIMESTAMP, 1, 2, 1, 'BOOKED');
INSERT INTO reservation (date, created_at, time_id, theme_id, member_id, status)
VALUES (CURRENT_DATE + 2, CURRENT_TIMESTAMP, 1, 2, 1, 'BOOKED');

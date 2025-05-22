INSERT INTO theme (name, description, thumbnail) VALUES ('테마1', '재밌음', '/image/default.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마2', '무서움', '/image/default.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마3', '놀라움', '/image/default.jpg');

INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('11:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00');

INSERT INTO member (name, email, password, role) VALUES ('코기','ind07152@naver.com','asd','USER');
INSERT INTO member (name, email, password, role) VALUES ('율무','ind07162@naver.com','asd','USER');
INSERT INTO member (name, email, password, role) VALUES ('ADMIN','admin@naver.com','1234','ADMIN');

INSERT INTO reservation (member_id, date, time_id, theme_id, status, created_at) VALUES (1, '2025-05-23', 1, 1,'RESERVED', '2025-04-22 14:30:00');
INSERT INTO reservation (member_id, date, time_id, theme_id, status, created_at) VALUES (1, '2025-05-28', 2, 1,'RESERVED', '2025-04-22 14:30:00');
INSERT INTO reservation (member_id, date, time_id, theme_id, status, created_at) VALUES (2, '2025-05-26', 1, 3,'RESERVED', '2025-04-22 14:30:00');
INSERT INTO reservation (member_id, date, time_id, theme_id, status, created_at) VALUES (2, '2025-05-18', 1, 2,'RESERVED', '2025-04-22 14:30:00');

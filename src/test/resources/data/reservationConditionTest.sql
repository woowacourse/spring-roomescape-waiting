INSERT INTO theme (id, name, description, thumbnail) VALUES (1,'테마1', '재밌음', '/image/default.jpg');
INSERT INTO theme (id, name, description, thumbnail) VALUES (2,'테마2', '무서움', '/image/default.jpg');
INSERT INTO theme (id, name, description, thumbnail) VALUES (3,'테마3', '놀라움', '/image/default.jpg');

INSERT INTO reservation_time (id, start_at) VALUES (1,'10:00');
INSERT INTO reservation_time (id, start_at) VALUES (2,'11:00');
INSERT INTO reservation_time (id, start_at) VALUES (3,'12:00');

INSERT INTO member (id, name, email, password, role) VALUES (1,'코기','ind07152@naver.com','asd','user');
INSERT INTO member (id, name, email, password, role) VALUES (2,'율무','ind07162@naver.com','asd','user');
INSERT INTO member (id, name, email, password, role) VALUES (3,'ADMIN','admin@naver.com','1234','admin');

INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, '2025-04-18', 1, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, '2025-04-26', 1, 3);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, '2025-04-28', 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, '2025-04-28', 2, 1);

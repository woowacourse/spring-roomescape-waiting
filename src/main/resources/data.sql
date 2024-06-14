INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('11:00');

INSERT INTO theme (name, description, thumbnail) VALUES ('에버', '공포', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('배키', '스릴러', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO member (name, email, password, role) VALUES ('에버', 'treeboss@gmail.com', 'treeboss123!', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('아토', 'atto@gmail.com', 'atto123!', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('관리자', 'admin@gmail.com', 'admin123!', 'ADMIN');

INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2024-05-08', 1, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2024-05-08', 2, 2, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2024-12-20', 1, 1, 1);

INSERT INTO waiting (date, time_id, theme_id, member_id, created_at) VALUES ('2024-12-20', 1, 1, 2, '2024-05-26 17:00:00');
INSERT INTO waiting (date, time_id, theme_id, member_id, created_at) VALUES ('2024-12-20', 1, 1, 3, '2024-05-26 17:00:01');

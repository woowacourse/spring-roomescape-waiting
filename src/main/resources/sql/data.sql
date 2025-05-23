INSERT INTO member (name, email, password, role) VALUES ('어드민', 'admin@gmail.com', 'wooteco7', 'ADMIN');
INSERT INTO member (name, email, password, role) VALUES ('회원1', 'user@gmail.com', 'wooteco7', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('회원2', 'user2@gmail.com', 'wooteco7', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('회원3', 'user3@gmail.com', 'wooteco7', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('회원4', 'user4@gmail.com', 'wooteco7', 'USER');

INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('13:00');
INSERT INTO reservation_time (start_at) VALUES ('15:00');
INSERT INTO reservation_time (start_at) VALUES ('17:00');

INSERT INTO theme (name, description, thumbnail) VALUES ('테마 A', '테마 A입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마 B', '테마 B입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마 C', '테마 C입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마 D', '테마 D입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마 E', '테마 E입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, '2025-05-27', 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, '2025-05-27', 2, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, '2025-05-27', 3, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, '2025-05-27', 4, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, '2025-05-28', 1, 2);

INSERT INTO waiting (member_id, date, time_id, theme_id) VALUES (2, '2025-05-27', 1, 1);
INSERT INTO waiting (member_id, date, time_id, theme_id) VALUES (2, '2025-05-27', 3, 1);
INSERT INTO waiting (member_id, date, time_id, theme_id) VALUES (3, '2025-05-27', 2, 1);
INSERT INTO waiting (member_id, date, time_id, theme_id) VALUES (3, '2025-05-27', 1, 1);
INSERT INTO waiting (member_id, date, time_id, theme_id) VALUES (4, '2025-05-27', 1, 1);

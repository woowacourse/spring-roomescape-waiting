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

INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, '2025-05-05', 1, 4);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (3, '2025-05-05', 2, 3);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (4, '2025-05-06', 3, 3);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, '2025-05-06', 4, 3);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (4, '2025-05-06', 1, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (5, '2025-05-07', 2, 3);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, '2025-05-07', 3, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (3, '2025-05-08', 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (3, '2025-05-09', 3, 5);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (3, '2025-05-09', 4, 5);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, '2025-05-09', 1, 4);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, '2025-05-09', 1, 3);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (5, '2025-05-09', 2, 4);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, '2025-05-10', 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (3, '2025-05-10', 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (4, '2025-05-10', 3, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (5, '2025-05-11', 1, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (5, '2025-05-12', 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (5, '2025-05-13', 4, 5);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (5, '2025-05-14', 1, 1);

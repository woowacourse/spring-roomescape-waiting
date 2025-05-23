INSERT INTO member (name, email, password, role) VALUES ('어드민', 'admin@gmail.com', 'wooteco7', 'ADMIN');
INSERT INTO member (name, email, password, role) VALUES ('회원', 'user@gmail.com', 'wooteco7', 'USER');

INSERT INTO theme (name, description, thumbnail) VALUES ('테마 A', '테마 A입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time (start_at) VALUES ('09:00');

INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, '2025-05-11', 1, 1);

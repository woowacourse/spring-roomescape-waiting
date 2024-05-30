INSERT INTO member (role, name, email, password) VALUES ('USER', '유저1', 'user1@email.com', 'password');
INSERT INTO member (role, name, email, password) VALUES ('USER', '유저2', 'user2@email.com', 'password');
INSERT INTO member (role, name, email, password) VALUES ('USER', '유저3', 'user3@email.com', 'password');
INSERT INTO member (role, name, email, password) VALUES ('ADMIN', '관리자', 'admin@email.com', 'password');


INSERT INTO theme (name, description, thumbnail) VALUES ('테마명01', '테마 설명01', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명02', '테마 설명02', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명03', '테마 설명03', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명04', '테마 설명04', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명05', '테마 설명05', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time (start_at) VALUES ('12:00');
INSERT INTO reservation_time (start_at) VALUES ('14:00');
INSERT INTO reservation_time (start_at) VALUES ('15:00');
INSERT INTO reservation_time (start_at) VALUES ('17:00');
INSERT INTO reservation_time (start_at) VALUES ('18:00');

INSERT INTO reservation (date, member_id, time_id, theme_id) VALUES (DATEADD(DAY, 5, CURDATE()), '1', '1', '1');
INSERT INTO reservation (date, member_id, time_id, theme_id) VALUES (DATEADD(DAY, 5, CURDATE()), '1', '2', '1');
INSERT INTO reservation (date, member_id, time_id, theme_id) VALUES (DATEADD(DAY, 5, CURDATE()), '1', '3', '1');
INSERT INTO reservation (date, member_id, time_id, theme_id) VALUES (DATEADD(DAY, 5, CURDATE()), '1', '4', '1');

INSERT INTO waiting (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, 5, CURDATE()), '2', '1', '1', 'DENY');
INSERT INTO waiting (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, 5, CURDATE()), '2', '2', '1', 'WAITING');

INSERT INTO waiting (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, 5, CURDATE()), '3', '1', '1', 'WAITING');
INSERT INTO waiting (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, 5, CURDATE()), '3', '2', '1', 'WAITING');

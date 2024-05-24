INSERT INTO member (role, name, email, password) VALUES ('USER', '유저1', 'user1@email.com', 'password');
INSERT INTO member (role, name, email, password) VALUES ('USER', '유저2', 'user2@email.com', 'password');
INSERT INTO member (role, name, email, password) VALUES ('USER', '유저3', 'user3@email.com', 'password');
INSERT INTO member (role, name, email, password) VALUES ('ADMIN', '관리자', 'admin@email.com', 'password');


INSERT INTO theme (name, description, thumbnail) VALUES ('테마명01', '테마 설명01', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명02', '테마 설명02', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명03', '테마 설명03', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명04', '테마 설명04', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명05', '테마 설명05', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명06', '테마 설명06', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명07', '테마 설명07', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명08', '테마 설명08', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명09', '테마 설명09', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명10', '테마 설명10', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명11', '테마 설명11', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명12', '테마 설명12', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명13', '테마 설명13', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명14', '테마 설명14', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마명15', '테마 설명15', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time (start_at) VALUES ('12:00');
INSERT INTO reservation_time (start_at) VALUES ('14:00');
INSERT INTO reservation_time (start_at) VALUES ('15:00');
INSERT INTO reservation_time (start_at) VALUES ('17:00');
INSERT INTO reservation_time (start_at) VALUES ('18:00');

INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '1', '1', '1', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '1', '2', '1', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '1', '3', '1', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '1', '4', '1', 'BOOKED');

INSERT INTO waiting (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '2', '1', '1', 'WAITING');
INSERT INTO waiting (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '2', '2', '1', 'WAITING');
INSERT INTO waiting (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '2', '3', '1', 'WAITING');
INSERT INTO waiting (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '2', '4', '1', 'WAITING');

INSERT INTO waiting (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '3', '1', '1', 'WAITING');
INSERT INTO waiting (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '3', '2', '1', 'WAITING');
INSERT INTO waiting (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '3', '3', '1', 'WAITING');
INSERT INTO waiting (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '3', '4', '1', 'WAITING');

INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '1', '3', '7', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '1', '3', '8', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '1', '3', '9', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -7, CURDATE()), '1', '3', '10', 'BOOKED');

INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -6, CURDATE()), '1', '1', '2', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -6, CURDATE()), '1', '2', '2', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -6, CURDATE()), '1', '3', '2', 'BOOKED');

INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -5, CURDATE()), '1', '1', '3', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -5, CURDATE()), '1', '2', '3', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -5, CURDATE()), '1', '3', '3', 'BOOKED');

INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -4, CURDATE()), '1', '1', '4', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -4, CURDATE()), '1', '2', '4', 'BOOKED');

INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -3, CURDATE()), '1', '1', '5', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -3, CURDATE()), '1', '2', '5', 'BOOKED');

INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -2, CURDATE()), '1', '1', '6', 'BOOKED');

INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES ('2024-04-01', '1', '1', '15', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES (DATEADD(DAY, -1, CURDATE()), '1', '1', '11', 'BOOKED');

INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES ('2023-04-26', '1', '1', '1', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES ('2023-04-26', '1', '2', '1', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES ('2023-04-26', '1', '3', '1', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES ('2023-04-26', '1', '4', '1', 'BOOKED');

INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES ('2023-04-26', '1', '3', '10', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES ('2023-04-26', '1', '1', '10', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES ('2023-04-26', '1', '2', '10', 'BOOKED');
INSERT INTO reservation (date, member_id, time_id, theme_id, status) VALUES ('2023-04-26', '1', '4', '10', 'BOOKED');


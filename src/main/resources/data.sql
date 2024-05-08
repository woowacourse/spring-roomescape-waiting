INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('11:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00');
INSERT INTO reservation_time (start_at) VALUES ('13:00');
INSERT INTO reservation_time (start_at) VALUES ('14:00');
INSERT INTO reservation_time (start_at) VALUES ('15:00');

INSERT INTO theme (name, description, thumbnail) VALUES ('에버', '공포', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('배키', '스릴러', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('네오', '공포', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('리사', '판타지', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('썬', '드라마', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('포비', '스릴러', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('구구', '판타지', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('토미', '드라마', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('브리', '드라마', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('솔라', '공포', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('왼손', '판타지', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO users (name, email, role, password) VALUES ('썬', 'sun@email.com', 'MEMBER', '1234');
INSERT INTO users (name, email, role, password) VALUES ('배키', 'dmsgml@email.com', 'MEMBER', '1111');
INSERT INTO users (name, email, role, password) VALUES ('포비', 'pobi@email.com', 'ADMIN', '2222');

INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2024-05-09', 1, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2024-05-07', 2, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2024-05-08', 4, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2024-05-09', 4, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2024-05-09', 3, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2024-05-09', 1, 3, 2);

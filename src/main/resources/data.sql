INSERT INTO reservation_time (start_at)
VALUES ('13:00:00'),
       ('14:00:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 1', '테마 1입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 2', '테마 2입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 3', '테마 3입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 4', '테마 4입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 5', '테마 5입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 6', '테마 6입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 7', '테마 7입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 8', '테마 8입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 9', '테마 9입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 10', '테마 10입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 11', '테마 11입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO member (name, email, password, role)
VALUES ('어드민', 'admin@admin.com', '1234', 'ADMIN'),
       ('미아', 'mia@mia.com', '1234', 'USER'),
       ('뽀로로', 'roro@roro.com', '1234', 'USER'),
       ('수달', 'sudal@sudal.com', '1234', 'USER'),
       ('홍길동', 'gildong@gildong.com', '1234', 'USER'),
       ('유저', 'user@user.com', '1234', 'USER');

INSERT INTO reservation(member_id, date, time_id, theme_id, status)
VALUES (2, '2025-05-10', 1, 1, 'BOOKING'),
       (2, '2025-05-11', 2, 2, 'BOOKING'),
       (3, '2025-05-12', 1, 1, 'BOOKING'),
       (3, '2025-05-13', 2, 2, 'BOOKING'),
       (4, '2025-05-14', 1, 1, 'BOOKING'),
       (4, '2025-05-15', 2, 2, 'BOOKING'),
       (3, '2025-05-10', 1, 1, 'WAITING'),
       (4, '2025-05-10', 1, 1, 'WAITING'),
       (5, '2025-05-10', 1, 1, 'WAITING'),
       (6, '2025-05-10', 1, 1, 'WAITING');

INSERT INTO waiting(member_id, date, time_id, theme_id)
VALUES (3, '2025-05-10', 1, 1),
       (4, '2025-05-10', 1, 1),
       (5, '2025-05-10', 1, 1),
       (6, '2025-05-10', 1, 1);

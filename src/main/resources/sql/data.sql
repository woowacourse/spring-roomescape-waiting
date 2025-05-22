INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('13:00'),
       ('15:00'),
       ('17:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 A', '테마 A입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 B', '테마 B입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 C', '테마 C입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 D', '테마 D입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 E', '테마 E입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO member (name, email, password, role)
VALUES ('가이온', 'jumdo12', 'jumdo12', 'ADMIN'),
       ('모다', 'moda14', 'moda14', 'USER');

INSERT INTO reservation (date, time_id, theme_id, member_id, status)
VALUES ('2025-05-25', 2, 4, 1, 'RESERVED'),
       ('2025-05-25', 2, 3, 1, 'RESERVED'),
       ('2025-05-26', 2, 3, 1, 'RESERVED'),
       ('2025-05-27', 1, 3, 1, 'RESERVED');

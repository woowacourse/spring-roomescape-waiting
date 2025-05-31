INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('13:00'),
       ('15:00'),
       ('17:00')
        ;

INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 A', '테마 A입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 B', '테마 B입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 C', '테마 C입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 D', '테마 D입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 E', '테마 E입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg')
       ;

INSERT INTO member (name, email, password, role)
VALUES ('가이온', 'jumdo12', 'jumdo12', 'ADMIN'),
       ('모다', 'moda14', 'moda14', 'USER'),
       ('대니', 'danny00', 'danny00', 'USER')
       ;

INSERT INTO reservation (date, time_id, theme_id, member_id, status)
VALUES ('2025-05-24', 2, 4, 3, 'RESERVED'),
       ('2025-05-24', 2, 3, 2, 'RESERVED'),
       ('2025-05-24', 4, 3, 2, 'RESERVED'),
       ('2025-05-25', 2, 3, 2, 'RESERVED'),
       ('2025-05-26', 1, 3, 2, 'RESERVED'),
       ('2025-05-24', 2, 4, 2, 'WAITING'),
       ('2025-05-24', 2, 4, 1, 'WAITING'),
       ('2025-05-26', 1, 3, 2, 'WAITING')
       ;

INSERT INTO reservation_waiting_ticket (reservation_id, created_at)
VALUES (6, '2025-05-20 21:40:03.000000'),
       (7, '2025-05-20 10:40:03.000000'),
       (8, '2025-05-21 10:10:10.000000')
       ;

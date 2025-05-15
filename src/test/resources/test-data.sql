INSERT INTO member (name, email, password, role)
VALUES
    ('레오', 'admin@gmail.com', 'qwer!', 'ADMIN');

INSERT INTO reservation_time (start_at)
VALUES
    ('10:00'),
    ('15:00'),
    ('16:00');

INSERT INTO theme (name, description, thumbnail)
VALUES
    ('테마1',  '테마1입니다.',  'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
    ('테마2',  '테마2입니다.',  'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
    ('테마3',  '테마3입니다.',  'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
    ('테마4',  '테마4입니다.',  'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
    ('테마5',  '테마5입니다.',  'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
    ('테마6',  '테마6입니다.',  'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
    ('테마7',  '테마7입니다.',  'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
    ('테마8',  '테마8입니다.',  'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
    ('테마9',  '테마9입니다.',  'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
    ('테마10', '테마10입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
    ('테마11', '테마11입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation (date, member_id, time_id, theme_id, status)
VALUES
    ('2025-04-30', 1, 1, 11, 'RESERVED'),
    ('2025-04-30', 1, 2, 11, 'RESERVED'),
    ('2025-04-29', 1, 1, 11, 'RESERVED'),
    ('2025-04-29', 1, 2,  9, 'RESERVED'),
    ('2025-04-28', 1, 2,  9, 'RESERVED'),
    ('2025-04-28', 1, 1,  8, 'RESERVED'),
    ('2025-04-27', 1, 1,  1, 'RESERVED'),
    ('2025-04-27', 1, 2,  2, 'RESERVED'),
    ('2025-04-26', 1, 1,  3, 'RESERVED'),
    ('2025-04-26', 1, 2,  4, 'RESERVED'),
    ('2025-04-25', 1, 1,  5, 'RESERVED'),
    ('2025-04-25', 1, 2,  6, 'RESERVED'),
    ('2025-04-24', 1, 1,  7, 'RESERVED');

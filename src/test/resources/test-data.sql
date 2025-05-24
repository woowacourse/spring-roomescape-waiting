INSERT INTO MEMBER (NAME, EMAIL, PASSWORD, ROLE)
VALUES
    ('리버', 'river@email.com', 'qwer!', 'ADMIN'),
    ('유저1', 'user1@email.com', 'qwer!', 'USER'),
    ('유저2', 'user2@email.com', 'qwer!', 'USER');

INSERT INTO RESERVATION_TIME (START_AT)
VALUES
    ('10:00'),
    ('15:00'),
    ('16:00');

INSERT INTO THEME (NAME, DESCRIPTION, THUMBNAIL)
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

INSERT INTO RESERVATION (MEMBER_ID, DATE, TIME_ID, THEME_ID)
VALUES
    (1, '2025-04-30', 1, 11),
    (1, '2025-04-30', 2, 11),
    (1, '2025-04-29', 1, 11),
    (1, '2025-04-29', 2,  9),
    (1, '2025-04-28', 2,  9),
    (1, '2025-04-28', 1,  8),
    (1, '2025-04-27', 1,  1),
    (1, '2025-04-27', 2,  2),
    (1, '2025-04-26', 1,  3),
    (1, '2025-04-26', 2,  4),
    (1, '2025-04-25', 1,  5),
    (1, '2025-04-25', 2,  6),
    (1, '2025-04-24', 1,  7);


INSERT INTO RESERVATION_WAITING (RESERVATION_ID, MEMBER_ID)
VALUES
    (2, 2),
    (2, 1),
    (3, 2),
    (3, 3),
    (3, 1)

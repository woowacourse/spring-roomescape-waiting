INSERT INTO USERS (name, role, email, password)
VALUES ('어드민', 'ADMIN', 'admin@email.com', 'password'),
       ('포포', 'USER', 'popo@email.com', 'password'),
       ('브라운', 'USER', 'brown@email.com', 'password');

INSERT INTO RESERVATION_TIME (start_at)
VALUES ('10:00'),
       ('12:00'),
       ('14:00'),
       ('16:00'),
       ('18:00'),
       ('20:00');

INSERT INTO THEME (name, description, thumbnail)
VALUES ('레벨1 탈출', '우테코 레벨1을 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨2 탈출', '우테코 레벨2를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨3 탈출', '우테코 레벨3을 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨4 탈출', '우테코 레벨4를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨5 탈출', '우테코 레벨를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO RESERVATION (user_id, date, time_id, theme_id, status)
VALUES (1, '2025-05-04', 1, 1, 'RESERVED'),
       (1, '2025-05-04', 5, 2, 'RESERVED'),
       (1, '2025-05-04', 6, 2, 'RESERVED'),
       (2, '2025-05-05', 3, 3, 'RESERVED'),
       (2, '2025-05-05', 1, 3, 'RESERVED'),
       (2, '2025-05-04', 2, 3, 'RESERVED'),
       (3, '2025-05-04', 1, 4, 'RESERVED'),
       (3, '2025-05-04', 1, 5, 'RESERVED');

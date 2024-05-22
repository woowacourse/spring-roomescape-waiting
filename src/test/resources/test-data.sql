DELETE
FROM reservation;

DELETE
FROM member;

DELETE
FROM time_slot;

DELETE
FROM theme;

ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 1;

ALTER TABLE member
    ALTER COLUMN id RESTART WITH 1;

ALTER TABLE time_slot
    ALTER COLUMN id RESTART WITH 1;

ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 1;

INSERT INTO member(name, email, password, role)
VALUES ('daon', 'test@email.com', '1234', 'ADMIN'),
       ('ash', 'test1@email.com', '1234', 'USER'),
       ('reddy', 'test2@email.com', '1234', 'USER'),
       ('rush', 'test3@email.com', '1234', 'ADMIN');

INSERT INTO time_slot(start_at)
VALUES ('10:00'),
       ('11:00'),
       ('12:00'),
       ('13:00');

INSERT INTO theme(name, description, thumbnail)
VALUES ('방탈출1', '1번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출2', '2번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출3', '3번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출4', '4번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출5', '5번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출6', '6번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출7', '7번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출8', '8번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출9', '9번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출10', '10번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출11', '11번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출12', '12번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출13', '13번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출14', '14번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('방탈출15', '15번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation(member_id, date, time_id, theme_id, status)
VALUES (1, CURRENT_DATE - 1, 1, 5, 'BOOKING'),
       (3, CURRENT_DATE - 1, 1, 5, 'PENDING'),
       (1, CURRENT_DATE - 1, 2, 5, 'BOOKING'),
       (4, CURRENT_DATE - 1, 2, 5, 'PENDING'),
       (3, CURRENT_DATE - 1, 2, 5, 'PENDING'),
       (1, CURRENT_DATE - 1, 3, 5, 'BOOKING'),
       (2, CURRENT_DATE - 1, 3, 5, 'PENDING'),
       (3, CURRENT_DATE - 1, 3, 5, 'PENDING'),
       (4, CURRENT_DATE - 1, 3, 5, 'PENDING'),
       (1, CURRENT_DATE - 2, 1, 5, 'BOOKING'),
       (2, CURRENT_DATE - 2, 1, 5, 'PENDING'),
       (3, CURRENT_DATE - 2, 1, 5, 'PENDING'),
       (4, CURRENT_DATE - 2, 1, 5, 'PENDING'),
       (1, CURRENT_DATE - 2, 2, 5, 'BOOKING'),
       (2, CURRENT_DATE - 2, 2, 5, 'PENDING'),
       (1, CURRENT_DATE - 2, 3, 4, 'BOOKING'),
       (1, CURRENT_DATE - 3, 1, 4, 'BOOKING'),
       (1, CURRENT_DATE - 3, 2, 4, 'BOOKING'),
       (1, CURRENT_DATE - 3, 3, 4, 'BOOKING'),
       (1, CURRENT_DATE - 4, 1, 3, 'BOOKING'),
       (1, CURRENT_DATE - 4, 2, 3, 'BOOKING'),
       (1, CURRENT_DATE - 4, 3, 3, 'BOOKING'),
       (2, CURRENT_DATE - 6, 2, 8, 'BOOKING'),
       (2, CURRENT_DATE - 6, 3, 9, 'BOOKING'),
       (2, CURRENT_DATE - 7, 1, 10, 'BOOKING'),
       (2, CURRENT_DATE - 7, 2, 11, 'BOOKING'),
       (2, CURRENT_DATE - 10, 3, 4, 'BOOKING'),
       (2, CURRENT_DATE - 10, 1, 4, 'BOOKING');

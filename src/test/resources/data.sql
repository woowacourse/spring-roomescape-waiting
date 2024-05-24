INSERT INTO time_slot(start_at)
VALUES ('00:00'),
       ('11:00'),
       ('12:00'),
       ('13:00');

INSERT INTO member(name, email, password, role)
VALUES ('admin', 'testDB@email.com', '1234', 'ADMIN'),
       ('user', 'test2DB@email.com', '1234', 'USER');

INSERT INTO theme(name, description, thumbnail)
VALUES ('레벨1 탈출', '우테코 레벨2를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨2 탈출', '우테코 레벨3를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨3 탈출', '우테코 레벨4를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation(member_id, date, time_id, theme_id, status)
VALUES (1, '2024-05-01', 1, 1, 'BOOKING'),
       (1, '2024-05-18', 1, 1, 'BOOKING'),
       (1, '2024-05-02', 1, 1, 'BOOKING'),
       (2, '2024-05-19', 3, 3, 'BOOKING'),
       (2, '2024-05-18', 3, 3, 'BOOKING'),
       (2, '2024-05-17', 3, 3, 'BOOKING'),
       (2, '2099-04-30', 1, 1, 'BOOKING');

INSERT INTO waiting(member_id, date, time_id, theme_id, status)
VALUES (2, '2024-05-01', 1, 1, 'WAITING'),
       (2, '2024-05-02', 1, 1, 'WAITING'),
       (2, '2024-05-18', 1, 1, 'WAITING'),
       (1, '2024-05-19', 3, 3, 'WAITING'),
       (1, '2024-05-18', 3, 3, 'WAITING'),
       (1, '2024-05-17', 3, 3, 'WAITING');


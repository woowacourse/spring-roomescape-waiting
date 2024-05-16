INSERT INTO time_slot(start_at)
VALUES ('10:00'),
       ('11:00'),
       ('12:00'),
       ('13:00');

INSERT INTO member(name, email, password, role)
VALUES ('어드민', 'testDB@email.com', '1234', 'ADMIN'),
       ('사용자', 'test2DB@email.com', '1234', 'USER');

INSERT INTO theme(name, description, thumbnail)
VALUES ('레벨1 탈출', '우테코 레벨2를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨2 탈출', '우테코 레벨3를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨3 탈출', '우테코 레벨4를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation(member_id, date, time_id, theme_id, status)
VALUES (1, CURRENT_DATE - 1, 3, 2, 'BOOKING'),
       (1, CURRENT_DATE - 1, 2, 2, 'BOOKING'),
       (1, CURRENT_DATE - 10, 2, 2, 'BOOKING'),
       (2, CURRENT_DATE - 10, 1, 1, 'BOOKING'),
       (2, CURRENT_DATE - 30, 3, 3, 'BOOKING'),
       (2, CURRENT_DATE - 60, 3, 3, 'BOOKING'),
       (2, CURRENT_DATE + 365, 1, 1, 'BOOKING');



INSERT INTO reservation_time(start_at)
VALUES ('10:00'),
       ('12:00');

INSERT INTO member(name, email, password, role)
VALUES ('wiib', 'asd@gmail.com', '1!2@3#', 'ADMIN'),
       ('zzzz', 'asdasd@gmail.com', '1!2@3#', 'USER');

INSERT INTO theme(name, description, thumbnail)
VALUES ('theme1', 'desc1',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('theme2', 'desc2',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('theme3', 'desc3',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation(date, time_id, theme_id, member_id, status, priority)
VALUES (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 1, 1, 'RESERVED', 0),
       (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 2, 2, 1, 'RESERVED', 0),
       (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 3, 2, 'RESERVED', 0),
       (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 2, 3, 2, 'RESERVED', 0);

INSERT INTO member(name, email, password, role)
VALUES ('Arthur', 'admin@gmail.com', 'asd', 'ADMIN'),
       ('Wiib', 'user@gmail.com', 'asd', 'USER'),
       ('TECO', 'TECO@gmail.com', 'asd', 'USER');

INSERT INTO theme(name, description, thumbnail)
VALUES ('theme1', 'desc1',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('theme2', 'desc2',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('theme3', 'desc3',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time(start_at)
VALUES ('10:00'),
       ('22:00');

INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 1, 1), -- 1
       (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 2, 1), -- 2
       (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 3, 1), -- 3
       (TIMESTAMPADD(WEEK, 1, CURRENT_DATE), 2, 1, 1),  -- 4
       (TIMESTAMPADD(WEEK, 1, CURRENT_DATE), 2, 2, 1),  -- 5
       (TIMESTAMPADD(WEEK, 1, CURRENT_DATE), 2, 3, 1),  -- 6

       (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 2, 1, 2), -- 7
       (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 2, 2, 2), -- 8
       (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 2, 3, 2), -- 9
       (TIMESTAMPADD(WEEK, 1, CURRENT_DATE), 1, 1, 2),  -- 10
       (TIMESTAMPADD(WEEK, 1, CURRENT_DATE), 1, 2, 2),  -- 11
       (TIMESTAMPADD(WEEK, 1, CURRENT_DATE), 1, 3, 2); -- 12

INSERT INTO reservation_waiting(member_id, reservation_id, priority)
VALUES (1, 7, 2),
       (3, 7, 1),
       (2, 5, 1);

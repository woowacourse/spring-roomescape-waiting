INSERT INTO theme(name, description, thumbnail)
VALUES ('theme1', 'desc1',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('theme2', 'desc2',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('theme3', 'desc3',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time(start_at)
VALUES ('10:00'),
       ('23:00');

INSERT INTO member(name, email, password, role)
VALUES ('관리자', 'admin@a.com', '123a!', 'ADMIN'),
       ('수달', 'a@a.com', '123a!', 'ADMIN'),
       ('트레', 'b@b.com', '123b!', 'USER'),
       ('테니', 'c@c.com', '123c!', 'ADMIN'),
       ('우주', 'space@woowahan.com', '123', 'USER'),
       ('에버', 'ever@woowahan.com', '123', 'ADMIN'),
       ('도도', 'dodo@woowahan.com', '123', 'USER'),
       ('제리', 'jerry@woowahan.com', '123', 'ADMIN');

INSERT INTO reservation(member_id, date, time_id, theme_id)
VALUES (1, TIMESTAMPADD(DAY, -1, CURRENT_DATE), 1, 1),
       (2, TIMESTAMPADD(DAY, -2, CURRENT_DATE), 1, 1),
       (3, TIMESTAMPADD(DAY, -3, CURRENT_DATE), 1, 1),
       (4, TIMESTAMPADD(DAY, -4, CURRENT_DATE), 1, 3);

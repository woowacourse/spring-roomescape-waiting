insert into member (email, password, name, role)
values ('user@user.com', 'user', '유저', 'USER'),
       ('admin@admin.com', 'admin', '어드민', 'ADMIN'),
       ('user@user2.com', 'user2', '유저2', 'USER'),
       ('admin@admin2.com', 'admin2', '어드민2', 'ADMIN');

insert into theme (name, description, thumbnail)
values ('theme1', 'description', 'thumbnail'),
       ('theme2', 'description', 'thumbnail'),
       ('theme3', 'description', 'thumbnail'),
       ('theme4', 'description', 'thumbnail'),
       ('theme5', 'description', 'thumbnail'),
       ('theme6', 'description', 'thumbnail'),
       ('theme7', 'description', 'thumbnail'),
       ('theme8', 'description', 'thumbnail'),
       ('theme9', 'description', 'thumbnail'),
       ('theme10', 'description', 'thumbnail'),
       ('theme11', 'description', 'thumbnail'),
       ('theme12', 'description', 'thumbnail'),
       ('theme13', 'description', 'thumbnail')
;

-- 예약 시간
insert into reservation_time (start_at)
values ('10:00'),
       ('10:10'),
       ('10:20'),
       ('10:30'),
       ('10:40'),
       ('10:50'),
       ('11:00'),
       ('11:10'),
       ('11:20'),
       ('11:30'),
       ('11:40'),
       ('11:50'),
       ('12:00'),
       ('12:10'),
       ('12:20'),
       ('12:30'),
       ('12:40'),
       ('12:50');

-- 예약 개수 차등두기
insert into reservation (member_id, theme_id, date, reservation_time_id, status)
values
    -- theme_id = 1 (10개 예약)
    (1, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 1, 'RESERVED'),
    (2, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 2, 'RESERVED'),
    (3, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 3, 'RESERVED'),
    (4, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 4, 'RESERVED'),
    (1, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 5, 'RESERVED'),
    (2, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 6, 'RESERVED'),
    (3, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 7, 'RESERVED'),
    (4, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 8, 'RESERVED'),
    (1, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 9, 'RESERVED'),
    (2, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 1, 'RESERVED'),

    -- theme_id = 2 (9개 예약)
    (1, 2, '2025-05-07', 1, 'RESERVED'),
    (2, 2, '2025-05-07', 2, 'RESERVED'),
    (3, 2, '2025-05-07', 3, 'RESERVED'),
    (4, 2, '2025-05-07', 4, 'RESERVED'),
    (1, 2, '2025-05-07', 5, 'RESERVED'),
    (2, 2, '2025-05-07', 6, 'RESERVED'),
    (3, 2, '2025-05-07', 7, 'RESERVED'),
    (4, 2, '2025-05-07', 8, 'RESERVED'),
    (4, 2, '2025-05-07', 9, 'RESERVED'),

    -- theme_id = 3 (8개 예약)
    (1, 3, '2025-05-06', 1, 'RESERVED'),
    (2, 3, '2025-05-06', 2, 'RESERVED'),
    (3, 3, '2025-05-06', 3, 'RESERVED'),
    (4, 3, '2025-05-06', 4, 'RESERVED'),
    (1, 3, '2025-05-06', 5, 'RESERVED'),
    (2, 3, '2025-05-06', 6, 'RESERVED'),
    (3, 3, '2025-05-06', 7, 'RESERVED'),
    (4, 3, '2025-05-06', 8, 'RESERVED'),

    -- theme_id = 4 (7개 예약)
    (1, 4, '2025-05-05', 3, 'RESERVED'),
    (2, 4, '2025-05-05', 4, 'RESERVED'),
    (3, 4, '2025-05-05', 5, 'RESERVED'),
    (4, 4, '2025-05-05', 6, 'RESERVED'),
    (1, 4, '2025-05-05', 7, 'RESERVED'),
    (2, 4, '2025-05-05', 8, 'RESERVED'),
    (3, 4, '2025-05-05', 9, 'RESERVED'),

    -- theme_id = 5 (6개 예약)
    (1, 5, '2025-05-04', 5, 'RESERVED'),
    (2, 5, '2025-05-04', 6, 'RESERVED'),
    (3, 5, '2025-05-04', 7, 'RESERVED'),
    (4, 5, '2025-05-04', 8, 'RESERVED'),
    (1, 5, '2025-05-04', 9, 'RESERVED'),
    (2, 5, '2025-05-04', 10, 'RESERVED'),

    -- theme_id = 6 (5개 예약)
    (1, 6, '2025-05-03', 1, 'RESERVED'),
    (2, 6, '2025-05-03', 2, 'RESERVED'),
    (3, 6, '2025-05-03', 3, 'RESERVED'),
    (4, 6, '2025-05-03', 4, 'RESERVED'),
    (1, 6, '2025-05-03', 5, 'RESERVED'),

    -- theme_id = 7 (4개 예약)
    (1, 7, '2025-05-02', 6, 'RESERVED'),
    (2, 7, '2025-05-02', 7, 'RESERVED'),
    (3, 7, '2025-05-02', 8, 'RESERVED'),
    (4, 7, '2025-05-02', 9, 'RESERVED'),

    -- theme_id = 8 (3개 예약)
    (1, 8, '2025-05-01', 1, 'RESERVED'),
    (2, 8, '2025-05-01', 2, 'RESERVED'),
    (3, 8, '2025-05-01', 3, 'RESERVED'),

    -- theme_id = 9 (2개 예약)
    (1, 9, '2025-04-30', 4, 'RESERVED'),
    (2, 9, '2025-04-30', 5, 'RESERVED'),

    -- theme_id = 10 (1개 예약)
    (3, 10, '2025-04-30', 8, 'RESERVED')

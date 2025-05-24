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
       ('theme10', 'description', 'thumbnail');

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
       ('12:00');

-- 예약
insert into reservation (member_id, theme_id, date, reservation_time_id)
values
    (1, 1, TIMESTAMPADD(DAY, +1, CURRENT_DATE()), 1),

    (1, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 1),
    (2, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 2),
    (3, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 3),
    (4, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 4),
    (1, 1, TIMESTAMPADD(DAY, -6, CURRENT_DATE()), 5),
    (2, 1, TIMESTAMPADD(DAY, -5, CURRENT_DATE()), 6),
    (3, 1, TIMESTAMPADD(DAY, -4, CURRENT_DATE()), 7),
    (4, 1, TIMESTAMPADD(DAY, -3, CURRENT_DATE()), 8),
    (1, 1, TIMESTAMPADD(DAY, -2, CURRENT_DATE()), 9),
    (2, 1, TIMESTAMPADD(DAY, -1, CURRENT_DATE()), 1);

-- 예약 대기
insert into waiting (member_id, theme_id, date, reservation_time_id)
values
    (2, 1, TIMESTAMPADD(DAY, +1, CURRENT_DATE()), 1);
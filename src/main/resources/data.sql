insert into member (email, password, name, role, created_at, modified_at)
values ('user@user.com', 'user', '유저', 'USER', CURRENT_TIME, CURRENT_TIME),
       ('admin@admin.com', 'admin', '어드민', 'ADMIN', CURRENT_TIME, CURRENT_TIME),
       ('user@user2.com', 'user2', '유저2', 'USER', CURRENT_TIME, CURRENT_TIME),
       ('admin@admin2.com', 'admin2', '어드민2', 'ADMIN', CURRENT_TIME, CURRENT_TIME);

insert into theme (name, description, thumbnail, created_at, modified_at)
values ('theme1', 'description', 'thumbnail', CURRENT_TIME, CURRENT_TIME),
       ('theme2', 'description', 'thumbnail', CURRENT_TIME, CURRENT_TIME),
       ('theme3', 'description', 'thumbnail', CURRENT_TIME, CURRENT_TIME),
       ('theme4', 'description', 'thumbnail', CURRENT_TIME, CURRENT_TIME),
       ('theme5', 'description', 'thumbnail', CURRENT_TIME, CURRENT_TIME),
       ('theme6', 'description', 'thumbnail', CURRENT_TIME, CURRENT_TIME),
       ('theme7', 'description', 'thumbnail', CURRENT_TIME, CURRENT_TIME),
       ('theme8', 'description', 'thumbnail', CURRENT_TIME, CURRENT_TIME),
       ('theme9', 'description', 'thumbnail', CURRENT_TIME, CURRENT_TIME),
       ('theme10', 'description', 'thumbnail', CURRENT_TIME, CURRENT_TIME);

-- 예약 시간
insert into reservation_time (start_at, created_at, modified_at)
values ('10:00', CURRENT_TIME, CURRENT_TIME),
       ('10:10', CURRENT_TIME, CURRENT_TIME),
       ('10:20', CURRENT_TIME, CURRENT_TIME),
       ('10:30', CURRENT_TIME, CURRENT_TIME),
       ('10:40', CURRENT_TIME, CURRENT_TIME),
       ('10:50', CURRENT_TIME, CURRENT_TIME),
       ('11:00', CURRENT_TIME, CURRENT_TIME),
       ('11:10', CURRENT_TIME, CURRENT_TIME),
       ('11:20', CURRENT_TIME, CURRENT_TIME),
       ('11:30', CURRENT_TIME, CURRENT_TIME),
       ('11:40', CURRENT_TIME, CURRENT_TIME),
       ('11:50', CURRENT_TIME, CURRENT_TIME),
       ('12:00', CURRENT_TIME, CURRENT_TIME);

-- 예약
insert into reservation (member_id, theme_id, date, reservation_time_id, created_at, modified_at)
values
    (1, 1, TIMESTAMPADD(DAY, +1, CURRENT_DATE()), 1, CURRENT_TIME, CURRENT_TIME),
    (1, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 1, CURRENT_TIME, CURRENT_TIME),
    (2, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 2, CURRENT_TIME, CURRENT_TIME),
    (3, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 3, CURRENT_TIME, CURRENT_TIME),
    (4, 1, TIMESTAMPADD(DAY, -7, CURRENT_DATE()), 4, CURRENT_TIME, CURRENT_TIME),
    (1, 1, TIMESTAMPADD(DAY, -6, CURRENT_DATE()), 5, CURRENT_TIME, CURRENT_TIME),
    (2, 1, TIMESTAMPADD(DAY, -5, CURRENT_DATE()), 6, CURRENT_TIME, CURRENT_TIME),
    (3, 1, TIMESTAMPADD(DAY, -4, CURRENT_DATE()), 7, CURRENT_TIME, CURRENT_TIME),
    (4, 1, TIMESTAMPADD(DAY, -3, CURRENT_DATE()), 8, CURRENT_TIME, CURRENT_TIME),
    (1, 1, TIMESTAMPADD(DAY, -2, CURRENT_DATE()), 9, CURRENT_TIME, CURRENT_TIME),
    (2, 1, TIMESTAMPADD(DAY, -1, CURRENT_DATE()), 1, CURRENT_TIME, CURRENT_TIME);

-- 예약 대기
insert into waiting (member_id, theme_id, date, reservation_time_id, created_at, modified_at)
values
    (2, 1, TIMESTAMPADD(DAY, +1, CURRENT_DATE()), 1, CURRENT_TIME, CURRENT_TIME);
SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation;
ALTER TABLE  reservation ALTER COLUMN id RESTART;
TRUNCATE TABLE reservation_time;
ALTER TABLE  reservation_time ALTER COLUMN id RESTART;
TRUNCATE TABLE member;
ALTER TABLE  member ALTER COLUMN id RESTART;
TRUNCATE TABLE theme;
ALTER TABLE  theme ALTER COLUMN id RESTART;
SET REFERENTIAL_INTEGRITY TRUE;

insert into member (name, email, password, role)
values ('찰리', 'gomding@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       ('비토', 'bito@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       ('회원', 'member@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       ('운영자', 'admin@wooteco.com', 'wootecoCrew6!', 'ADMIN');

insert into reservation_time (start_at)
values ('10:00'),
       ('11:00'),
       ('12:00'),
       ('13:00');

insert into theme (name, description, thumbnail)
values ('name1', 'description1', 'thumbnail1'),
       ('name2', 'description2', 'thumbnail2'),
       ('name3', 'description3', 'thumbnail3'),
       ('name4', 'description4', 'thumbnail4'),
       ('name5', 'description5', 'thumbnail5'),
       ('name6', 'description6', 'thumbnail6');

insert into reservation (date, member_id, time_id, theme_id, status)
values ('2099-04-29', 2, 1, 1, 'RESERVATION'),
       ('2099-04-29', 2, 2, 1, 'RESERVATION'),
       ('2099-04-29', 1, 3, 1, 'RESERVATION');

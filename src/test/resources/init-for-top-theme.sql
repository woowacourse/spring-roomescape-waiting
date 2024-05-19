drop table if exists reservation;
drop table if exists reservation_time;
drop table if exists member;
drop table if exists theme;

CREATE TABLE reservation_time
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    start_at VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    thumbnail VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE member
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    date VARCHAR(255) NOT NULL,
    member_id BIGINT NOT NULL,
    time_id BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    status VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (member_id) REFERENCES member (id)
);

insert into theme (name, description, thumbnail)
values  ('name1',
         'description1',
         'thumbnail1'),
        ('name2',
         'description2',
         'thumbnail2'),
        ('name3',
         'description3',
         'thumbnail3'),
        ('name4',
         'description4',
         'thumbnail4'),
        ('name5',
         'description5',
         'thumbnail5'),
        ('name6',
         'description6',
         'thumbnail6'),
        ('name7',
         'description7',
         'thumbnail7'),
        ('name8',
         'description8',
         'thumbnail8'),
        ('name9',
         'description9',
         'thumbnail9'),
        ('name10',
         'description10',
         'thumbnail10'),
        ('name11',
         'description11',
         'thumbnail11'),
        ('name12',
         'description12',
         'thumbnail12'),
        ('name13',
         'description13',
         'thumbnail13');

insert into reservation_time (start_at)
values ('10:00'),
       ('11:00'),
       ('12:00'),
       ('13:00'),
       ('14:00'),
       ('15:00'),
       ('16:00'),
       ('17:00'),
       ('18:00'),
       ('19:00'),
       ('20:00'),
       ('21:00'),
       ('22:00');

insert into member (name, email, password, role)
values ('수달', 'sudal@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       ('이상', 'leesang@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       ('회원', 'member@wooteco.com', 'wootecoCrew6!', 'BASIC');

insert into reservation (date, member_id, time_id, theme_id)
values (DATEADD(DAY, -5, CURRENT_DATE), 1, 1, 1),
       (DATEADD(DAY, -5, CURRENT_DATE), 1, 2, 1),
       (DATEADD(DAY, -5, CURRENT_DATE), 1, 3, 1),
       (DATEADD(DAY, -5, CURRENT_DATE), 1, 4, 1),
       (DATEADD(DAY, -5, CURRENT_DATE), 1, 1, 2),
       (DATEADD(DAY, -5, CURRENT_DATE), 1, 2, 2),
       (DATEADD(DAY, -5, CURRENT_DATE), 2, 3, 2),
       (DATEADD(DAY, -5, CURRENT_DATE), 2, 1, 3),
       (DATEADD(DAY, -5, CURRENT_DATE), 2, 2, 3),
       (DATEADD(DAY, -5, CURRENT_DATE), 2, 1, 4),
       (DATEADD(DAY, -5, CURRENT_DATE), 2, 1, 5),
       (DATEADD(DAY, -5, CURRENT_DATE), 2, 1, 6),
       (DATEADD(DAY, -5, CURRENT_DATE), 3, 1, 7),
       (DATEADD(DAY, -5, CURRENT_DATE), 3, 1, 8),
       (DATEADD(DAY, -5, CURRENT_DATE), 3, 1, 9);

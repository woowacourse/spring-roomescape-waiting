drop table reservation;
drop table reservation_time;
drop table theme;
drop table member;

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
    status VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (member_id) REFERENCES member (id)
);

insert into member (name, email, password, role)
values ('수달', 'sudal@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       ('이상', 'leesang@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       ('회원', 'member@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       ('운영자', 'admin@wooteco.com', 'wootecoCrew6!', 'ADMIN');

insert into reservation_time (start_at)
values ('12:00'),
       ('13:00'),
       ('14:00'),
       ('15:00');

insert into theme (name, description, thumbnail)
values ('테마1', '테마1 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       ('테마2', '테마2 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       ('테마3', '테마3 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       ('테마4', '테마4 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       ('테마5', '테마5 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       ('테마6', '테마6 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png');

insert into reservation (date, member_id, time_id, theme_id, status)
values ('2099-04-29', 2, 1, 1, 'RESERVATION'),
       ('2099-04-29', 2, 2, 1, 'RESERVATION'),
       ('2099-04-29', 1, 3, 1, 'RESERVATION');

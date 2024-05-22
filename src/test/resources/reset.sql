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

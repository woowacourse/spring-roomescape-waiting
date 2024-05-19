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

insert into reservation (member_id, date, time_id, theme_id)
values (1, DATEADD(DAY, 1, CURRENT_DATE), 1, 2),
       (3, DATEADD(DAY, 1, CURRENT_DATE), 1, 4),
       (3, DATEADD(DAY, 1, CURRENT_DATE), 1, 1),
       (4, DATEADD(DAY, 1, CURRENT_DATE), 2, 1),
       (2, DATEADD(DAY, 1, CURRENT_DATE), 2, 2),
       (4, DATEADD(DAY, 1, CURRENT_DATE), 2, 3),
       (4, DATEADD(DAY, 1, CURRENT_DATE), 2, 4),
       (1, DATEADD(DAY, 1, CURRENT_DATE), 3, 1),
       (3, DATEADD(DAY, 1, CURRENT_DATE), 3, 2),
       (1, DATEADD(DAY, 1, CURRENT_DATE), 3, 3),
       (1, DATEADD(DAY, 1, CURRENT_DATE), 3, 4),
       (2, DATEADD(DAY, 1, CURRENT_DATE), 4, 4),
       (2, DATEADD(DAY, 1, CURRENT_DATE), 4, 5),
       (2, DATEADD(DAY, 1, CURRENT_DATE), 4, 1);

insert into reservation (member_id, date, time_id, theme_id)
values (1, DATEADD(DAY, -1, CURRENT_DATE), 1, 2),
       (1, DATEADD(DAY, -1, CURRENT_DATE), 1, 3),
       (3, DATEADD(DAY, -1, CURRENT_DATE), 1, 1),
       (3, DATEADD(DAY, -1, CURRENT_DATE), 1, 4),
       (4, DATEADD(DAY, -1, CURRENT_DATE), 2, 1),
       (2, DATEADD(DAY, -1, CURRENT_DATE), 2, 2),
       (4, DATEADD(DAY, -1, CURRENT_DATE), 2, 3),
       (4, DATEADD(DAY, -1, CURRENT_DATE), 2, 4),
       (1, DATEADD(DAY, -1, CURRENT_DATE), 3, 1),
       (2, DATEADD(DAY, -1, CURRENT_DATE), 3, 2),
       (3, DATEADD(DAY, -1, CURRENT_DATE), 3, 3),
       (1, DATEADD(DAY, -1, CURRENT_DATE), 3, 4),
       (2, DATEADD(DAY, -1, CURRENT_DATE), 4, 4),
       (2, DATEADD(DAY, -1, CURRENT_DATE), 4, 5),
       (2, DATEADD(DAY, -1, CURRENT_DATE), 4, 2),
       (2, DATEADD(DAY, -2, CURRENT_DATE), 3, 2),
       (2, DATEADD(DAY, -3, CURRENT_DATE), 3, 3),
       (2, DATEADD(DAY, -4, CURRENT_DATE), 3, 1);

insert into member (name, email, password, role)
values ('수달', 'sudal@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       ('이상', 'leesang@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       ('회원', 'member@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       ('운영자', 'admin@wooteco.com', 'wootecoCrew6!', 'ADMIN');

insert into reservation_time (start_at)
values ('12:00:00'),
       ('13:00:00'),
       ('14:00:00'),
       ('15:00:00');

insert into theme (name, description, thumbnail)
values ('테마1', '테마1 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       ('테마2', '테마2 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       ('테마3', '테마3 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       ('테마4', '테마4 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       ('테마5', '테마5 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       ('테마6', '테마6 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png');

insert into reservation (member_id, date, time_id, theme_id)
values (1, '2024-12-24', 1, 2),
       (2, '2024-12-25', 2, 2),
       (3, '2024-12-25', 3, 3),
       (4, '2024-12-25', 2, 3),
       (1, '2024-12-26', 3, 3),
       (2, '2024-12-27', 4, 4),
       (3, '2024-12-28', 1, 4),
       (4, '2024-12-28', 2, 4),
       (1, '2024-12-28', 3, 4),
       (2, '2024-12-28', 4, 5),
       (3, '2024-12-29', 1, 1),
       (4, '2024-12-29', 2, 1),
       (1, '2024-12-29', 3, 1),
       (2, '2024-12-29', 4, 1);

insert into reservation (member_id, date, time_id, theme_id)
values (1, '2024-4-26', 1, 2),
       (2, '2024-4-26', 2, 2),
       (3, '2024-4-26', 3, 3),
       (4, '2024-4-27', 2, 3),
       (1, '2024-4-27', 3, 3),
       (2, '2024-4-28', 4, 4),
       (3, '2024-4-28', 1, 4),
       (4, '2024-4-28', 2, 4),
       (1, '2024-5-1', 3, 4),
       (2, '2024-5-1', 4, 5),
       (3, '2024-5-1', 1, 1),
       (4, '2024-5-1', 2, 1),
       (1, '2024-5-2', 3, 1),
       (2, '2024-5-2', 4, 1);

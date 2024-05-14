insert into member (id, name, email, password, role)
values (1, '수달', 'sudal@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       (2, '이상', 'leesang@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       (3, '회원', 'member@wooteco.com', 'wootecoCrew6!', 'BASIC'),
       (4, '운영자', 'admin@wooteco.com', 'wootecoCrew6!', 'ADMIN');

insert into reservation_time (id, start_at)
values (1, '12:00:00'),
       (2, '13:00:00'),
       (3, '14:00:00'),
       (4, '15:00:00');

insert into theme (id, name, description, thumbnail)
values (1, '테마1', '테마1 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       (2, '테마2', '테마2 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       (3, '테마3', '테마3 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       (4, '테마4', '테마4 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       (5, '테마5', '테마5 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       (6, '테마6', '테마6 설명', 'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png');

insert into reservation (member_id, date, time_id, theme_id)
values (1, '2024-12-24', 1, 1),
       (2, '2024-12-25', 2, 1),
       (3, '2024-12-25', 3, 2),
       (4, '2024-12-25', 2, 2),
       (1, '2024-12-26', 3, 2),
       (2, '2024-12-27', 4, 3),
       (3, '2024-12-28', 1, 3),
       (4, '2024-12-28', 2, 3),
       (1, '2024-12-28', 3, 3),
       (2, '2024-12-28', 4, 4),
       (3, '2024-12-29', 1, 1),
       (4, '2024-12-29', 2, 1),
       (1, '2024-12-29', 3, 1),
       (2, '2024-12-29', 4, 1);

insert into reservation (member_id, date, time_id, theme_id)
values (1, '2024-4-26', 1, 1),
       (2, '2024-4-26', 2, 1),
       (3, '2024-4-26', 3, 2),
       (4, '2024-4-27', 2, 2),
       (1, '2024-4-27', 3, 2),
       (2, '2024-4-28', 4, 3),
       (3, '2024-4-28', 1, 3),
       (4, '2024-4-28', 2, 3),
       (1, '2024-5-1', 3, 3),
       (2, '2024-5-1', 4, 4),
       (3, '2024-5-1', 1, 1),
       (4, '2024-5-1', 2, 1),
       (1, '2024-5-2', 3, 1),
       (2, '2024-5-2', 4, 1);

insert into member (id, name, email, password, role)
values (1, '아루', 'test@test1.com', '12341234', 'MEMBER'),
       (2, '이상', 'test@test2.com', '12341234', 'MEMBER'),
       (3, '수달', 'test@test3.com', '12341234', 'MEMBER'),
       (4, '오리', 'test@test4.com', '12341234', 'MEMBER'),
       (5, '제이미', 'test@test5.com', '12341234', 'MEMBER'),
       (6, '비밥', 'test@test6.com', '12341234', 'MEMBER'),
       (7, '웨지', 'test@test7.com', '12341234', 'MEMBER')
;

insert into reservation_time (id, start_at)
values (1, '12:00:00'),
       (2, '13:00:00'),
       (3, '14:00:00'),
       (4, '15:00:00')
;

insert into theme (id, name, description, thumbnail_url)
values (1, '테마1', '테마1 설명',
        'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       (2, '테마2', '테마2 설명',
        'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       (3, '테마3', '테마3 설명',
        'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       (4, '테마4', '테마4 설명',
        'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       (5, '테마5', '테마5 설명',
        'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png'),
       (6, '테마6', '테마6 설명',
        'https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png')
;

insert into reservation (id, member_id, date, time_id, theme_id, created_at, status)
values (1, 1, '1999-12-24', 1, 1, '1999-01-01T12:00:00', 'BOOKED'),
       (2, 2, '1999-12-25', 2, 2, '1999-01-01T12:00:00', 'BOOKED'),
       (3, 2, '1999-12-25', 3, 2, '1999-01-01T12:00:00', 'BOOKED'),
       (4, 2, '1999-12-25', 2, 3, '1999-01-01T12:00:00', 'BOOKED'),
       (5, 2, '1999-12-26', 3, 3, '1999-01-01T12:00:00', 'BOOKED'),
       (6, 1, '1999-12-27', 4, 3, '1999-01-01T12:00:00', 'BOOKED'),
       (7, 3, '1999-12-28', 1, 3, '1999-01-01T12:00:00', 'BOOKED'),
       (8, 3, '1999-12-28', 2, 4, '1999-01-01T12:00:00', 'BOOKED'),
       (9, 4, '1999-12-28', 3, 4, '1999-01-01T12:00:00', 'BOOKED'),
       (10, 4, '1999-12-28', 4, 4, '1999-01-01T12:00:00', 'BOOKED'),
       (11, 5, '1999-12-29', 1, 4, '1999-01-01T12:00:00', 'BOOKED'),
       (12, 6, '1999-12-29', 2, 4, '1999-01-01T12:00:00', 'BOOKED'),
       (13, 6, '1999-12-29', 3, 4, '1999-01-01T12:00:00', 'BOOKED'),
       (14, 7, '1999-12-29', 4, 4, '1999-01-01T12:00:00', 'BOOKED')
;

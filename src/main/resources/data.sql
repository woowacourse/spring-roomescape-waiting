insert into member (id, name, email, password)
values (1, '아루', 'test@test1.com', '12341234'),
       (2, '이상', 'test@test2.com', '12341234'),
       (3, '수달', 'test@test3.com', '12341234'),
       (4, '오리', 'test@test4.com', '12341234'),
       (5, '제이미', 'test@test5.com', '12341234'),
       (6, '비밥', 'test@test6.com', '12341234'),
       (7, '웨지', 'test@test7.com', '12341234'),
       (8, '시소', 'test@test8.com', '12341234')
;

insert into member_role (member_id, role)
values (1, 'MEMBER'),
       (2, 'MEMBER'),
       (3, 'MEMBER'),
       (4, 'MEMBER'),
       (5, 'MEMBER'),
       (6, 'MEMBER'),
       (7, 'MEMBER'),
       (8, 'ADMIN')
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

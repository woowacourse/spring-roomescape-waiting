INSERT INTO member (id, email, password, name, role)
values (1, 't1@t1.com', 't1', '재즈', 'MEMBER'),
       (2, 't2@t2.com', 't2', '러너덕', 'MEMBER'),
       (3, 't3@t3.com', 't3', '워니', 'MEMBER'),
       (4, 't4@t4.com', 't4', '브리', 'MEMBER'),
       (5, 't5@t5.com', 't5', '구구', 'MEMBER'),
       (6, 't6@t6.com', 't5', '네오', 'MEMBER'),
       (7, 't7@t7.com', 't5', '브라운', 'MEMBER'),
       (8, 'tt@tt.com', 'tt', '어드민', 'ADMIN');

INSERT INTO reservation_time (id, start_at)
values (1, '12:00'),
       (2, '16:00'),
       (3, '20:00');

INSERT INTO theme (id, name, description, thumbnail)
values (1, '테마1', '테마1입니다', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       (2, '테마2', '테마2입니다', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       (3, '테마3', '테마3입니다', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation (id, member_id, theme_id, date, reservation_time_id, reservation_status)
values (1, 1, 1, '2024-05-16', 2, 'CONFIRMED'),
       (2, 2, 2, '2024-05-17', 2, 'CONFIRMED'),
       (3, 3, 3, '2024-05-17', 1, 'CONFIRMED'),
       (4, 4, 3, '2024-05-18', 3, 'CONFIRMED'),
       (5, 5, 2, '2024-05-18', 1, 'CONFIRMED'),
       (6, 6, 3, '2024-05-18', 1, 'CONFIRMED'),
       (7, 7, 3, '2024-05-19', 2, 'CONFIRMED'),
       (8, 2, 2, '2024-05-25', 2, 'CONFIRMED'),
       (9, 3, 2, '2024-05-25', 2, 'WAITING'),
       (10, 4, 2, '2024-05-25', 2, 'WAITING'),
       (11, 1, 2, '2024-05-25', 2, 'WAITING'),
       (12, 5, 3, '2024-05-26', 1, 'CONFIRMED'),
       (13, 6, 3, '2024-05-26', 1, 'WAITING'),
       (14, 1, 3, '2024-05-26', 1, 'WAITING'),
       (15, 1, 2, '2024-05-27', 1, 'CONFIRMED'),
       (16, 1, 3, '2024-05-28', 1, 'CONFIRMED');

ALTER TABLE member
    ALTER COLUMN id RESTART WITH 9;
ALTER TABLE reservation_time
    ALTER COLUMN id RESTART WITH 4;
ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 4;
ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 17;

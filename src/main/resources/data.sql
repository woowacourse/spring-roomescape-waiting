insert into reservation_time (id, start_at)
values (NEXT VALUE FOR TIME_ID_SEQUENCE, '12:00');
insert into reservation_time (id, start_at)
values (NEXT VALUE FOR TIME_ID_SEQUENCE, '13:00');
insert into reservation_time (id, start_at)
values (NEXT VALUE FOR TIME_ID_SEQUENCE, '14:00');
insert into reservation_time (id, start_at)
values (NEXT VALUE FOR TIME_ID_SEQUENCE, '15:00');
insert into reservation_time (id, start_at)
values (NEXT VALUE FOR TIME_ID_SEQUENCE, '16:00');

insert into theme (id, name, description, thumbnail)
values (NEXT VALUE FOR THEME_ID_SEQUENCE, '테마1', '테마1 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (id, name, description, thumbnail)
values (NEXT VALUE FOR THEME_ID_SEQUENCE, '테마2', '테마2 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (id, name, description, thumbnail)
values (NEXT VALUE FOR THEME_ID_SEQUENCE, '테마3', '테마3 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (id, name, description, thumbnail)
values (NEXT VALUE FOR THEME_ID_SEQUENCE, '테마4', '테마4 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (id, name, description, thumbnail)
values (NEXT VALUE FOR THEME_ID_SEQUENCE, '테마5', '테마5 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (id, name, description, thumbnail)
values (NEXT VALUE FOR THEME_ID_SEQUENCE, '테마6', '테마6 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (id, name, description, thumbnail)
values (NEXT VALUE FOR THEME_ID_SEQUENCE, '테마7', '테마7 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (id, name, description, thumbnail)
values (NEXT VALUE FOR THEME_ID_SEQUENCE, '테마8', '테마8 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (id, name, description, thumbnail)
values (NEXT VALUE FOR THEME_ID_SEQUENCE, '테마9', '테마9 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (id, name, description, thumbnail)
values (NEXT VALUE FOR THEME_ID_SEQUENCE, '테마10', '테마10 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (id, name, description, thumbnail)
values (NEXT VALUE FOR THEME_ID_SEQUENCE, '테마11', '테마11 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

-- MemberId 시퀀스 생성
insert into member (id, name, email, password, role)
values (NEXT VALUE FOR MEMBER_ID_SEQUENCE, '포스티', 'posty@woowa.com', '12341234', 'MEMBER');
insert into member (id, name, email, password, role)
values (NEXT VALUE FOR MEMBER_ID_SEQUENCE, '밍곰', 'minggom@woowa.com', '12341234', 'MEMBER');
insert into member (id, name, email, password, role)
values (NEXT VALUE FOR MEMBER_ID_SEQUENCE, '로키', 'roky@woowa.com', '12341234', 'ADMIN');

-- 포스티 예약 목록
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 1, 1, 3, 1, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 1, 2, 3, 1, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 1, 3, 3, 1, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 1, 1, 1, 1, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 1, 2, 1, 1, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 1, 1, 2, 1, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 1, 1, 4, 1, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 1, 1, 5, 1, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 1, 1, 6, 1, 'CONFIRMED');

-- 밍곰 예약 목록
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 2, 1, 1, 2, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 2, 2, 2, 2, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 2, 3, 3, 2, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 1, 1, 7, 2, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 1, 1, 8, 2, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 1, 1, 9, 2, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values (CURRENT_DATE - 1, 1, 10, 2, 'CONFIRMED');

-- 포스티 예약 대기 목록
insert into waiting (date, time_id, theme_id, member_id)
values (CURRENT_DATE - 2, 1, 1, 1);
insert into waiting (date, time_id, theme_id, member_id)
values (CURRENT_DATE - 2, 2, 2, 1);
insert into waiting (date, time_id, theme_id, member_id)
values (CURRENT_DATE - 2, 3, 3, 1);

-- 로키 예약 대기 목록
insert into waiting (date, time_id, theme_id, member_id)
values (CURRENT_DATE - 2, 1, 1, 3);

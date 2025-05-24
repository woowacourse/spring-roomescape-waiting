insert into reservation_time (start_at)
values ('12:00');
insert into reservation_time (start_at)
values ('13:00');
insert into reservation_time (start_at)
values ('14:00');

insert into theme (name, description, thumbnail)
values ('테마1', '테마1 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (name, description, thumbnail)
values ('테마2', '테마2 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (name, description, thumbnail)
values ('테마3', '테마3 입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

insert into member (name, email, password, role)
values ('포스티', 'posty@woowa.com', '12341234', 'MEMBER');
insert into member (name, email, password, role)
values ('밍곰', 'minggom@woowa.com', '12341234', 'MEMBER');
insert into member (name, email, password, role)
values ('로키', 'roky@woowa.com', '12341234', 'ADMIN');

-- 포스티 예약 목록
insert into reservation (date, time_id, theme_id, member_id, status)
values ('2025-05-10', 1, 3, 1, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values ('2025-05-10', 2, 3, 1, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values ('2025-05-10', 3, 3, 1, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values ('2025-05-10', 1, 1, 1, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values ('2025-05-10', 2, 1, 1, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values ('2025-05-10', 1, 2, 1, 'CONFIRMED');

-- 밍곰 예약 목록
insert into reservation (date, time_id, theme_id, member_id, status)
values ('2025-05-11', 1, 1, 2, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values ('2025-05-11', 2, 2, 2, 'CONFIRMED');
insert into reservation (date, time_id, theme_id, member_id, status)
values ('2025-05-11', 3, 3, 2, 'CONFIRMED');

-- 포스티 예약 대기 목록
insert into waiting (date, time_id, theme_id, member_id)
values ('2025-05-11', 1, 1, 1);
insert into waiting (date, time_id, theme_id, member_id)
values ('2025-05-11', 2, 2, 1);
insert into waiting (date, time_id, theme_id, member_id)
values ('2025-05-11', 3, 3, 1);

-- 로키 예약 대기 목록
insert into waiting (date, time_id, theme_id, member_id)
values ('2025-05-11', 1, 1, 3);

-- 회원
insert into member (id, name, role, email, password) values (100, '미미', 'USER', 'test1@example.com', 'password1');
insert into member (id, name, role, email, password) values (101, '노랑', 'USER', 'test2@example.com', 'password2');

-- 테마
insert into theme (id, name, description, thumbnail) values (100, '테마1', '설명1', '썸네일1');

-- 시간
insert into reservation_time (id, start_at) values (100, '10:00');
insert into reservation_time (id, start_at) values (101, '12:00');

-- 예약 (예약 존재 테스트용)
insert into reservation (id, member_id, theme_id, date, reservation_time_id) values (100, 100, 100, '2025-05-13', 100);
insert into reservation (id, member_id, theme_id, date, reservation_time_id) values (101, 101, 100, '2025-05-15', 100);
insert into reservation (id, member_id, theme_id, date, reservation_time_id) values (102, 101, 100, '2025-05-11', 100);

-- 예약 대기
insert into waiting (id, member_id, theme_id, date, reservation_time_id, created_at) values (100, 100, 100, '2025-05-01', 100, '2025-04-30 10:00:00');
insert into waiting (id, member_id, theme_id, date, reservation_time_id, created_at) values (101, 101, 100, '2025-05-01', 101, '2025-04-30 10:00:00');
insert into waiting (id, member_id, theme_id, date, reservation_time_id, created_at) values (102, 100, 100, '2025-05-11', 100, '2025-04-30 10:00:00');
insert into waiting (id, member_id, theme_id, date, reservation_time_id, created_at) values (103, 101, 100, '2025-05-11', 101, '2025-04-30 10:00:00');
insert into waiting (id, member_id, theme_id, date, reservation_time_id, created_at) values (110, 101, 100, '2025-05-09', 101, '2025-04-30 10:00:00');

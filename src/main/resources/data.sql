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
values ('밍곰', 'minggom@woowa.com', '12341234', 'ADMIN');

-- 포스티의 예약
insert into reservation (date, time_id, theme_id, member_id)
values (DATEADD(day, 1, CURRENT_DATE), 1, 3, 1);
insert into reservation (date, time_id, theme_id, member_id)
values (DATEADD(day, 1, CURRENT_DATE), 2, 3, 1);
insert into reservation (date, time_id, theme_id, member_id)
values (DATEADD(day, 1, CURRENT_DATE), 3, 3, 1);

-- 밍곰의 예약
insert into reservation (date, time_id, theme_id, member_id)
values (DATEADD(day, 1, CURRENT_DATE), 1, 1, 2);
insert into reservation (date, time_id, theme_id, member_id)
values (DATEADD(day, 1, CURRENT_DATE), 2, 1, 2);
insert into reservation (date, time_id, theme_id, member_id)
values (DATEADD(day, 1, CURRENT_DATE), 1, 2, 2);

-- 포스티의 대기 예약
insert into waiting (date, time_id, theme_id, member_id, created_at)
values (DATEADD(day, 1, CURRENT_DATE), 1, 1, 1, CURRENT_TIMESTAMP());
insert into waiting (date, time_id, theme_id, member_id, created_at)
values (DATEADD(day, 1, CURRENT_DATE), 2, 1, 1, CURRENT_TIMESTAMP());

-- 밍곰의 대기 예약
insert into waiting (date, time_id, theme_id, member_id, created_at)
values (DATEADD(day, 1, CURRENT_DATE), 2, 3, 2, CURRENT_TIMESTAMP());
insert into waiting (date, time_id, theme_id, member_id, created_at)
values (DATEADD(day, 1, CURRENT_DATE), 3, 3, 2, CURRENT_TIMESTAMP());

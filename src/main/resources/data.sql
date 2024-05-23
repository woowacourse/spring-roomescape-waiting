insert into reservation_time(start_at) values('15:00');
insert into reservation_time(start_at) values('16:00');
insert into reservation_time(start_at) values('17:00');
insert into reservation_time(start_at) values('18:00');

insert into theme(name, description, thumbnail) values('테스트1', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme(name, description, thumbnail) values('테스트2', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme(name, description, thumbnail) values('테스트3', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme(name, description, thumbnail) values('테스트4', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

insert into member(name, email, password, role) values('어드민', 'a@a.a', 'a', 'ADMIN');
insert into member(name, email, password, role) values ('1호', '1@1.1', '1', 'MEMBER');
insert into member(name, email, password, role) values('2호', '2@2.2', '2', 'MEMBER');
insert into member(name, email, password, role) values('3호', '3@3.3', '3', 'MEMBER');
insert into member(name, email, password, role) values('4호', '4@4.4', '4', 'MEMBER');

-- 예약
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(1, DATEADD('DAY', -1, CURRENT_DATE()) - 1 , 1, 1, 'CONFIRMED', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(2, DATEADD('DAY', -2, CURRENT_DATE()) -2 , 3, 2, 'CONFIRMED', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(3, DATEADD('DAY', -3, CURRENT_DATE()), 2, 2, 'CONFIRMED', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(4, DATEADD('DAY', -4, CURRENT_DATE()), 1, 2, 'CONFIRMED', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(5, DATEADD('DAY', -5, CURRENT_DATE()), 1, 3, 'CONFIRMED', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(2, DATEADD('DAY', 7, CURRENT_DATE()), 2, 4, 'CONFIRMED', CURRENT_TIMESTAMP());

-- 예약 대기
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(3, DATEADD('DAY', 7, CURRENT_DATE()), 2, 4, 'WAITING', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(4, DATEADD('DAY', 7, CURRENT_DATE()), 2, 4, 'WAITING', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(5, DATEADD('DAY', 7, CURRENT_DATE()), 2, 4, 'WAITING', CURRENT_TIMESTAMP());

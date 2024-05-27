-- 관리자가 특정 조건에 해당되는 예약을 조회하는 테스트에서만 사용되는 데이터입니다.
insert into reservation_time(start_at) values('15:00');

insert into theme(name, description, thumbnail) values('테스트1', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme(name, description, thumbnail) values('테스트2', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

insert into member(name, email, password, role) values('어드민', 'a@a.a', 'a', 'ADMIN');
insert into member(name, email, password, role) values ('1호', '1@1.1', '1', 'MEMBER');

-- 예약
-- 시간은 같은 시간으로, 날짜는 어제부터 7일 전까지
-- memberId = 1인 멤버는 3개의 예약, memberId = 2인 멤버는 4개의 예약이 있음
-- themeId = 1인 테마는 4개의 예약, themeId = 2인 테마는 3개의 예약이 있음
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(1, DATEADD('DAY', -1, CURRENT_DATE()), 1, 1, 'CONFIRMED', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(1, DATEADD('DAY', -2, CURRENT_DATE()), 1, 1, 'CONFIRMED', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(1, DATEADD('DAY', -3, CURRENT_DATE()), 1, 1, 'CONFIRMED', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(2, DATEADD('DAY', -4, CURRENT_DATE()), 1, 1, 'CONFIRMED', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(2, DATEADD('DAY', -5, CURRENT_DATE()), 1, 2, 'CONFIRMED', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(2, DATEADD('DAY', -6, CURRENT_DATE()), 1, 2, 'CONFIRMED', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(2, DATEADD('DAY', -7, CURRENT_DATE()), 1, 2, 'CONFIRMED', CURRENT_TIMESTAMP());

-- 예약 대기
-- 예약 대기는 조회되면 안됨.
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(1, DATEADD('DAY', 7, CURRENT_DATE()), 1, 1, 'WAITING', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(1, DATEADD('DAY', 8, CURRENT_DATE()), 1, 1, 'WAITING', CURRENT_TIMESTAMP());
insert into reservation(member_id, date, time_id, theme_id, status, created_at) values(2, DATEADD('DAY', 9, CURRENT_DATE()), 1, 1, 'WAITING', CURRENT_TIMESTAMP());

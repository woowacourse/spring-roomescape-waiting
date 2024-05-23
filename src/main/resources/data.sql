insert into reservation_time(start_at) values('15:00');
insert into reservation_time(start_at) values('16:00');
insert into reservation_time(start_at) values('17:00');
insert into reservation_time(start_at) values('18:00');

insert into theme(name, description, thumbnail) values('테스트1', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme(name, description, thumbnail) values('테스트2', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme(name, description, thumbnail) values('테스트3', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme(name, description, thumbnail) values('테스트4', '테스트중', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

insert into member(name, email, password, role) values('명오', 'myoungO@gmail.com', 'myoungO', 'ADMIN');
insert into member(name, email, password, role) values ('제제', 'zeze@gmail.com', 'zeze', 'ADMIN');
insert into member(name, email, password, role) values('썬', 'myoungO@gmail.com', 'sun', 'MEMBER');
insert into member(name, email, password, role) values('아서', 'hyunta@gmail.com', 'hyunta', 'MEMBER');
insert into member(name, email, password, role) values('솔라', 'sola@gmail.com', 'sola', 'MEMBER');

insert into reservation(member_id, date, time_id, theme_id) values(1, DATEADD('DAY', -1, CURRENT_DATE()) - 1 , 1, 1);
insert into reservation(member_id, date, time_id, theme_id) values(2, DATEADD('DAY', -2, CURRENT_DATE()) -2 , 3, 2);
insert into reservation(member_id, date, time_id, theme_id) values(3, DATEADD('DAY', -3, CURRENT_DATE()), 2, 2);
insert into reservation(member_id, date, time_id, theme_id) values(4, DATEADD('DAY', -4, CURRENT_DATE()), 1, 2);
insert into reservation(member_id, date, time_id, theme_id) values(5, DATEADD('DAY', -5, CURRENT_DATE()), 1, 3);


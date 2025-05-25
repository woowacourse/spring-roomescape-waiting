insert into member (name, email, password, role)
values ('admin', 'admin', 'admin', 'ADMIN');
insert into member (name, email, password, role)
values ('name1', 'email1@domain.com', 'email1', 'MEMBER');
insert into member (name, email, password, role)
values ('name2', 'email2@domain.com', 'email2', 'MEMBER');
insert into member (name, email, password, role)
values ('르브론', 'lebron@domain.com', 'lebron', 'MEMBER');
insert into member (name, email, password, role)
values ('커리', 'curry@domain.com', 'curry', 'MEMBER');


insert into theme (name, description, thumbnail)
values ('theme1', 'desc1', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (name, description, thumbnail)
values ('theme2', 'desc2', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (name, description, thumbnail)
values ('theme3', 'desc3', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (name, description, thumbnail)
values ('theme4', 'desc4', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
insert into theme (name, description, thumbnail)
values ('theme5', 'desc5', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

insert into reservation_time (start_at)
values ('09:00');
insert into reservation_time (start_at)
values ('10:00');
insert into reservation_time (start_at)
values ('11:00');
insert into reservation_time (start_at)
values ('12:00');
insert into reservation_time (start_at)
values ('13:00');
insert into reservation_time (start_at)
values ('14:00');

insert into reservation (theme_id, member_id, date, reservation_time_id)
values (1, 1, '2025-05-25', 1);
insert into reservation (theme_id, member_id, date, reservation_time_id)
values (1, 1, '2025-05-22', 1);
insert into reservation (theme_id, member_id, date, reservation_time_id)
values (1, 1, '2025-05-20', 1);
insert into reservation (theme_id, member_id, date, reservation_time_id)
values (2, 1, '2025-05-19', 1);
insert into reservation (theme_id, member_id, date, reservation_time_id)
values (2, 2, '2025-05-18', 2);
insert into reservation (theme_id, member_id, date, reservation_time_id)
values (3, 2, '2025-05-17', 2);
insert into reservation (theme_id, member_id, date, reservation_time_id)
values (4, 2, '2025-05-23', 2);
insert into reservation (theme_id, member_id, date, reservation_time_id)
values (5, 2, '2025-05-25', 2);

insert into waiting (theme_id, member_id, date, reservation_time_id, create_at)
values (1, 2, '2025-05-25', 1, '2025-05-24T20:00:00');
insert into waiting (theme_id, member_id, date, reservation_time_id, create_at)
values (1, 3, '2025-05-25', 1, '2025-05-24T20:01:00');
insert into waiting (theme_id, member_id, date, reservation_time_id, create_at)
values (1, 4, '2025-05-25', 1, '2025-05-24T20:02:00');
insert into waiting (theme_id, member_id, date, reservation_time_id, create_at)
values (1, 5, '2025-05-25', 1, '2025-05-24T20:03:00');

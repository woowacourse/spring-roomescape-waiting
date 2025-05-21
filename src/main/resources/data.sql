insert into member (name, email, password, role)
values ('name1', 'email1@domain.com', 'email1', 'MEMBER');
insert into member (name, email, password, role)
values ('name2', 'email2@domain.com', 'email2', 'MEMBER');
insert into member (name, email, password, role)
values ('admin', 'admin@domain.com', 'admin', 'ADMIN');

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

insert into time_slot (start_at)
values ('09:00');
insert into time_slot (start_at)
values ('11:00');
insert into time_slot (start_at)
values ('16:00');

insert into reservation (theme_id, member_id, date, time_slot_id)
values (1, 1, '2025-05-13', 1);
insert into reservation (theme_id, member_id, date, time_slot_id)
values (1, 1, '2025-05-12', 1);
insert into reservation (theme_id, member_id, date, time_slot_id)
values (1, 1, '2025-05-11', 1);
insert into reservation (theme_id, member_id, date, time_slot_id)
values (2, 1, '2025-05-13', 1);
insert into reservation (theme_id, member_id, date, time_slot_id)
values (2, 2, '2025-05-12', 2);
insert into reservation (theme_id, member_id, date, time_slot_id)
values (3, 2, '2025-05-11', 2);
insert into reservation (theme_id, member_id, date, time_slot_id)
values (4, 2, '2025-05-13', 2);
insert into reservation (theme_id, member_id, date, time_slot_id)
values (5, 2, '2025-05-12', 2);

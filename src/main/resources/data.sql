insert into member(name, email, password, role)
values ('어드민', 'admin@gmail.com', '123', 'ADMIN');

insert into member(name, email, password, role)
values ('멤버1', 'one@gmail.com', '123', 'MEMBER');

insert into member(name, email, password, role)
values ('멤버2', 'two@gmail.com', '123', 'MEMBER');

insert into member(name, email, password, role)
values ('멤버3', 'three@gmail.com', '123', 'MEMBER');

insert into member(name, email, password, role)
values ('멤버4', 'four@gmail.com', '123', 'MEMBER');

insert into member(name, email, password, role)
values ('멤버5', 'five@gmail.com', '123', 'MEMBER');


insert into theme(name, description, thumbnail)
values ('테마1', '테마1입니당 ^0^', 'https://file.miricanvas.com/template_thumb/2021/07/02/13/20/k4t92g5ntu46etia/thumb.jpg');

insert into theme(name, description, thumbnail)
values ('테마2', '테마2입니당 ^0^', 'https://file.miricanvas.com/template_thumb/2021/07/02/13/20/k4t92g5ntu46etia/thumb.jpg');

insert into theme(name, description, thumbnail)
values ('테마3', '테마3입니당 ^0^', 'https://file.miricanvas.com/template_thumb/2021/07/02/13/20/k4t92g5ntu46etia/thumb.jpg');

insert into theme(name, description, thumbnail)
values ('테마4', '테마4입니당 ^0^', 'https://file.miricanvas.com/template_thumb/2021/07/02/13/20/k4t92g5ntu46etia/thumb.jpg');

insert into theme(name, description, thumbnail)
values ('테마5', '테마5입니당 ^0^', 'https://file.miricanvas.com/template_thumb/2021/07/02/13/20/k4t92g5ntu46etia/thumb.jpg');


insert into reservation_time(start_at)
values ('11:00:00');

insert into reservation_time(start_at)
values ('13:00:00');

insert into reservation_time(start_at)
values ('15:00:00');

insert into reservation_time(start_at)
values ('17:00:00');

insert into reservation_time(start_at)
values ('19:00:00');


insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-10', 'RESERVATION', '1', '4', '2');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-11', 'RESERVATION', '2', '3', '2');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-12', 'RESERVATION', '3', '3', '2');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-13', 'RESERVATION', '3', '2', '3');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-13', 'RESERVATION', '4', '1', '3');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-14', 'RESERVATION', '2', '1', '4');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-14', 'RESERVATION', '4', '4', '4');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-14', 'RESERVATION', '1', '2', '5');


insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-10', 'RESERVATION_WAIT', '1', '4', '3');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-10', 'RESERVATION_WAIT', '1', '4', '4');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-10', 'RESERVATION_WAIT', '1', '4', '5');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-11', 'RESERVATION_WAIT', '2', '3', '3');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-11', 'RESERVATION_WAIT', '2', '3', '5');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-13', 'RESERVATION_WAIT', '3', '2', '4');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-13', 'RESERVATION_WAIT', '4', '1', '2');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-13', 'RESERVATION_WAIT', '4', '1', '4');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-13', 'RESERVATION_WAIT', '4', '1', '5');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-14', 'RESERVATION_WAIT', '4', '4', '2');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-14', 'RESERVATION_WAIT', '4', '4', '3');

insert into reservation(date, status, time_id, theme_id, member_id)
values ('2025-05-14', 'RESERVATION_WAIT', '1', '2', '2');

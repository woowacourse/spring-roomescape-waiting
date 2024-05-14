insert into member(name, email, password, role)
values ('어드민', 'admin@gmail.com', '123456', 'ADMIN');

insert into member(name, email, password, role)
values ('도도', 'dodo@gmail.com', '123123', 'MEMBER');

insert into member(name, email, password, role)
values ('미르', 'mir@gmail.com', '123456', 'MEMBER');

insert into theme(name, description, thumbnail)
values ('테마1', '테마1입니당 ^0^', 'https://file.miricanvas.com/template_thumb/2021/07/02/13/20/k4t92g5ntu46etia/thumb.jpg');

insert into theme(name, description, thumbnail)
values ('테마2', '테마2입니당 ^0^', 'https://file.miricanvas.com/template_thumb/2021/07/02/13/20/k4t92g5ntu46etia/thumb.jpg');

insert into theme(name, description, thumbnail)
values ('테마3', '테마3입니당 ^0^', 'https://file.miricanvas.com/template_thumb/2021/07/02/13/20/k4t92g5ntu46etia/thumb.jpg');

insert into reservation_time(start_at)
values ('15:00:00');

insert into reservation_time(start_at)
values ('16:00:00');

insert into reservation_time(start_at)
values ('17:00:00');


insert into reservation(date, time_id, theme_id, member_id)
values ('2024-05-01', '1', '1', '1');

insert into reservation(date, time_id, theme_id, member_id)
values ('2024-05-01', '2', '1', '1');

insert into reservation(date, time_id, theme_id, member_id)
values ('2024-05-01', '3', '1', '2');

insert into reservation(date, time_id, theme_id, member_id)
values ('2024-04-30', '1', '2', '2');

insert into reservation(date, time_id, theme_id, member_id)
values ('2024-04-30', '2', '2', '1');

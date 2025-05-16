insert into member (id, email, password, name, role)
values (1, 'user@user.com', 'user', '유저', 'USER'),
       (2, 'admin@admin.com', 'admin', '어드민', 'ADMIN');

ALTER SEQUENCE member_seq RESTART WITH 3;

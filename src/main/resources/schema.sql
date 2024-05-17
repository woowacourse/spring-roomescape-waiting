create table if not exists member
(
    id       bigint      not null auto_increment,
    email    varchar(50) not null,
    name     varchar(20) not null,
    password varchar(64) not null,
    primary key (id)
);

create table if not exists role
(
    member_id bigint      not null,
    role      varchar(10) not null,
    primary key (member_id),
    foreign key (member_id) references member (id)
);

create table if not exists theme
(
    id          bigint       not null auto_increment,
    name        varchar(20)  not null,
    description varchar(200) not null,
    thumbnail   varchar(200) not null,
    primary key (id)
);

create table if not exists reservation_time
(
    id       bigint not null auto_increment,
    start_at time   not null,
    primary key (id)
);

create table if not exists reservation
(
    id         bigint    not null auto_increment,
    member_id  bigint    not null,
    date       date      not null,
    time_id    bigint    not null,
    theme_id   bigint    not null,
    created_at timestamp not null,
    primary key (id),
    foreign key (member_id) references member (id),
    foreign key (time_id) references reservation_time (id),
    foreign key (theme_id) references theme (id)
);

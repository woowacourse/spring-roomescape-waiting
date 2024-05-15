create table if not exists member
(
    id          bigint       not null auto_increment,
    name        varchar(255) not null,
    email       varchar(255) not null,
    password    varchar(255) not null,
    role        enum('BASIC', 'ADMIN') default 'BASIC',
    primary key (id)
);

create table if not exists theme
(
    id          bigint       not null auto_increment,
    name        varchar(255) not null,
    description varchar(255) not null,
    thumbnail   varchar(255) not null,
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
    id          bigint       not null auto_increment,
    member_id   bigint       not null,
    date        date         not null,
    time_id     bigint       not null,
    theme_id    bigint       not null,
    primary key (id),
    foreign key (time_id) references reservation_time (id),
    foreign key (theme_id) references theme (id),
    foreign key (member_id) references member (id)
);

set referential_integrity false;

truncate table reservation_detail;
truncate table reservation_time;
truncate table theme;
truncate table members;
truncate table reservation;

alter table reservation_detail
    alter column id restart with 1;
alter table reservation_time
    alter column id restart with 1;
alter table theme
    alter column id restart with 1;
alter table members
    alter column id restart with 1;
alter table reservation
    alter column id restart with 1;

set referential_integrity true;

set referential_integrity false;
truncate table reservation_status;
truncate table reservation;
truncate table theme;
truncate table reservation_time;
truncate table member;

alter table reservation alter column id restart with 1;
alter table theme alter column id restart with 1;
alter table reservation_time alter column id restart with 1;
alter table member alter column id restart with 1;
set referential_integrity true;

set referential_integrity false;
truncate table theme restart identity;
truncate table reservation_time restart identity;
truncate table member restart identity;
truncate table reservation restart identity;
set referential_integrity true;

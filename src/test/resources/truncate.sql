SET referential_integrity false;

TRUNCATE TABLE reservation;
TRUNCATE TABLE reservation_time;
TRUNCATE TABLE theme;
TRUNCATE TABLE member;

ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 1;
ALTER TABLE reservation_time
    ALTER COLUMN id RESTART WITH 1;
ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 1;
ALTER TABLE member
    ALTER COLUMN id RESTART WITH 1;

SET referential_integrity true;
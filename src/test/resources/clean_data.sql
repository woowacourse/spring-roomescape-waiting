DELETE
FROM reservation;
DELETE
FROM reservation_wait;
DELETE
FROM reservation_time;
DELETE
FROM theme;
DELETE
FROM member;

ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 1;
ALTER TABLE reservation_wait
    ALTER COLUMN id RESTART WITH 1;
ALTER TABLE reservation_time
    ALTER COLUMN id RESTART WITH 1;
ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 1;
ALTER TABLE member
    ALTER COLUMN id RESTART WITH 1;

DELETE
FROM member_reservation;
DELETE
FROM reservation_detail;
DELETE
FROM reservation_time;
DELETE
FROM theme;
DELETE
FROM member;

ALTER TABLE member_reservation
    ALTER COLUMN id RESTART WITH 1;
ALTER TABLE reservation_detail
    ALTER COLUMN id RESTART WITH 1;
ALTER TABLE reservation_time
    ALTER COLUMN id RESTART WITH 1;
ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 1;
ALTER TABLE member
    ALTER COLUMN id RESTART WITH 1;

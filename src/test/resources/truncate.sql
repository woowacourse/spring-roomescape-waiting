DELETE FROM reservation_detail;
DELETE FROM reservation_waiting;
DELETE FROM reservation;
DELETE FROM member;
DELETE FROM time;
DELETE FROM theme;

ALTER TABLE reservation_detail ALTER COLUMN id RESTART;
ALTER TABLE reservation_waiting ALTER COLUMN id RESTART;
ALTER TABLE reservation ALTER COLUMN id RESTART;
ALTER TABLE member ALTER COLUMN id RESTART;
ALTER TABLE time ALTER COLUMN id RESTART;
ALTER TABLE theme ALTER COLUMN id RESTART;

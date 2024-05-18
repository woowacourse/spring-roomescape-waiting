SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE member_reservation;
TRUNCATE TABLE member;
TRUNCATE TABLE reservation;
TRUNCATE TABLE reservation_time;
TRUNCATE TABLE theme;

ALTER TABLE member_reservation ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE member ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE reservation ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE reservation_time ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE theme ALTER COLUMN ID RESTART WITH 1;

INSERT INTO member(name, email, password, role)
VALUES ('초코칩', 'dev.chocochip@gmail.com', '$2a$10$DORK.bYhWWXTEiWjwy9mxu.vodUPmuyiBeiShqRnSRcA1.buwN06K', 'USER');
INSERT INTO member(name, email, password, role)
VALUES ('관리자', 'admin@roomescape.com', '$2a$10$g6o5hc6cd2osCIArbIMKFOxnre8qIEDJ5bPzdabiHvcNBxQPEQoQ2', 'ADMIN');

SET REFERENTIAL_INTEGRITY TRUE;

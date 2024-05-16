SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE reservation;
TRUNCATE TABLE reservation_time;
TRUNCATE TABLE theme;
TRUNCATE TABLE member;

ALTER TABLE reservation ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE reservation_time ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE theme ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE member ALTER COLUMN ID RESTART WITH 1;

SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO member(name, email, password, role) VALUES('guest', 'guest@email.com', 'guest123', 'GUEST');
INSERT INTO member(name, email, password, role) VALUES('guest2', 'guest2@email.com', 'guest123', 'GUEST');

ALTER TABLE member ALTER COLUMN id RESTART WITH 1;
ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;
ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1;
ALTER TABLE theme ALTER COLUMN id RESTART WITH 1;
ALTER TABLE waiting ALTER COLUMN id RESTART WITH 1;

INSERT INTO reservation_time(start_at)
VALUES ('10:00');
INSERT INTO reservation_time(start_at)
VALUES ('11:00');

INSERT INTO theme(name, description, thumbnail)
VALUES ('이름1', '설명1', '썸네일1');
INSERT INTO theme(name, description, thumbnail)
VALUES ('이름2', '설명2', '썸네일2');

INSERT INTO member(name, email, password, role)
VALUES ('운영진', 'admin@naver.com', '1234', 'ADMIN');
INSERT INTO member(name, email, password, role)
VALUES ('홍길동', 'member@naver.com', '1234', 'MEMBER');

INSERT INTO waiting(status, rank)
VALUES ('BOOKED', null);
INSERT INTO waiting(status, rank)
VALUES ('BOOKED', null);

INSERT INTO reservation(date, time_id, theme_id, member_id, waiting_id)
VALUES ('2999-05-01', 1, 1, 1, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id, waiting_id)
VALUES ('2999-05-01', 2, 2, 2, 2);

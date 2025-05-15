INSERT INTO MEMBER(name, username, password, role) VALUES ('ad', 'ad@ad.com', '$2a$10$VtdQedu.URsd2YQ67xVN5uG5.bw31RLkbYrH9auYGdCiyu9xqrJ3G', 'ADMIN');
INSERT INTO MEMBER(name, username, password, role) VALUES ('aa', 'aa@aa.com', '$2a$10$VtdQedu.URsd2YQ67xVN5uG5.bw31RLkbYrH9auYGdCiyu9xqrJ3G', 'USER');

INSERT INTO THEME(name, description, thumbnail)
VALUES ('asdf', 'asdf', 'asdf');
INSERT INTO THEME(name, description, thumbnail)
VALUES ('asdf2', 'asdf', 'asdf');
INSERT INTO THEME(name, description, thumbnail)
VALUES ('asdf2', 'asdf', 'asdf');

INSERT INTO RESERVATION_TIME(time)
VALUES ('10:00');
INSERT INTO RESERVATION_TIME(time)
VALUES ('23:59');
INSERT INTO RESERVATION_TIME(time)
VALUES ('16:59');
INSERT INTO RESERVATION_TIME(time)
VALUES ('15:59');
INSERT INTO RESERVATION_TIME(time)
VALUES ('20:59');

INSERT INTO RESERVATION(name, date, time_id, theme_id)
VALUES ('asdf1', '2025-05-13', 1, 1);
INSERT INTO RESERVATION(name, date, time_id, theme_id)
VALUES ('asdf2', '2025-05-13', 1, 2);
INSERT INTO RESERVATION(name, date, time_id, theme_id)
VALUES ('asdf3', '2025-05-13', 2, 1);
INSERT INTO RESERVATION(name, date, time_id, theme_id)
VALUES ('asdf4', '2025-05-12', 3, 2);
INSERT INTO RESERVATION(name, date, time_id, theme_id)
VALUES ('asdf5', '2025-05-12', 2, 1);
INSERT INTO RESERVATION(name, date, time_id, theme_id)
VALUES ('asdf5', '2025-05-10', 2, 3);
INSERT INTO RESERVATION(name, date, time_id, theme_id)
VALUES ('asdf5', '2025-07-10', 1, 1);
INSERT INTO RESERVATION(name, date, time_id, theme_id)
VALUES ('asdf5', '2025-07-10', 1, 2);
INSERT INTO RESERVATION(name, date, time_id, theme_id)
VALUES ('asdf5', '2025-07-10', 2, 2);

INSERT INTO RESERVE_TICKET(member_id, reservation_id)
VALUES (1, 1);
INSERT INTO RESERVE_TICKET(member_id, reservation_id)
VALUES (1, 2);
INSERT INTO RESERVE_TICKET(member_id, reservation_id)
VALUES (1, 3);
INSERT INTO RESERVE_TICKET(member_id, reservation_id)
VALUES (1, 4);
INSERT INTO RESERVE_TICKET(member_id, reservation_id)
VALUES (1, 6);
INSERT INTO RESERVE_TICKET(member_id, reservation_id)
VALUES (1, 5);
INSERT INTO RESERVE_TICKET(member_id, reservation_id)
VALUES (1, 7);
INSERT INTO RESERVE_TICKET(member_id, reservation_id)
VALUES (1, 8);
INSERT INTO RESERVE_TICKET(member_id, reservation_id)
VALUES (1, 9);


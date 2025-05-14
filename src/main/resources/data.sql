INSERT INTO MEMBER(name, username, password, role) VALUES ('praisebak', 'praisebak@naver.com', '$2a$10$4DDpj4fav96RdKr/uG8A0etFZb4/NSZpf1Km/MA6gyZpoEcD4q6i.', 'ADMIN');

INSERT INTO THEME(name,description,thumbnail) VALUES ('asdf', 'asdf', 'asdf');
INSERT INTO THEME(name,description,thumbnail) VALUES ('asdf2','asdf','asdf');
INSERT INTO THEME(name,description,thumbnail) VALUES ('asdf2','asdf','asdf');

INSERT INTO RESERVATION_TIME(time) VALUES ('10:00');
INSERT INTO RESERVATION_TIME(time) VALUES ('23:59');
INSERT INTO RESERVATION_TIME(time) VALUES ('16:59');
INSERT INTO RESERVATION_TIME(time) VALUES ('15:59');
INSERT INTO RESERVATION_TIME(time) VALUES ('20:59');

INSERT INTO RESERVATION(name,date,time_id,theme_id) VALUES ('asdf', '2024-05-14', 1,1);
INSERT INTO RESERVATION(name,date,time_id,theme_id) VALUES ('asdf2', '2024-05-13', 1,2);
INSERT INTO RESERVATION(name,date,time_id,theme_id) VALUES ('asdf3', '2024-05-12', 2,3);
INSERT INTO RESERVATION(name,date,time_id,theme_id) VALUES ('asdf4', '2024-05-11', 3,2);
INSERT INTO RESERVATION(name,date,time_id,theme_id) VALUES ('asdf5', '2024-05-10', 2,1);
INSERT INTO RESERVATION(name,date,time_id,theme_id) VALUES ('asdf5', '2025-07-10', 2,1);
INSERT INTO RESERVATION(name,date,time_id,theme_id) VALUES ('asdf5', '2025-07-10', 1,1);
INSERT INTO RESERVATION(name,date,time_id,theme_id) VALUES ('asdf5', '2025-07-10', 1,2);
INSERT INTO RESERVATION(name,date,time_id,theme_id) VALUES ('asdf5', '2025-07-10', 2,2);

INSERT INTO RESERVATION_MEMBER_IDS(member_id,reservation_id) VALUES(1,1);
INSERT INTO RESERVATION_MEMBER_IDS(member_id,reservation_id) VALUES(1,2);
INSERT INTO RESERVATION_MEMBER_IDS(member_id,reservation_id) VALUES(1,3);
INSERT INTO RESERVATION_MEMBER_IDS(member_id,reservation_id) VALUES(1,4);
INSERT INTO RESERVATION_MEMBER_IDS(member_id,reservation_id) VALUES(1,6);
INSERT INTO RESERVATION_MEMBER_IDS(member_id,reservation_id) VALUES(1,5);
INSERT INTO RESERVATION_MEMBER_IDS(member_id,reservation_id) VALUES(1,7);
INSERT INTO RESERVATION_MEMBER_IDS(member_id,reservation_id) VALUES(1,8);
INSERT INTO RESERVATION_MEMBER_IDS(member_id,reservation_id) VALUES(1,9);


INSERT INTO theme (theme_name, description, thumbnail) VALUES ('자바', '자바에 대한 모든 것', 'https://cdn.iconscout.com/icon/free/png-256/java-43-569305.png');

INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00');


INSERT INTO member (name, password, email, role) VALUES ('admin', 'asd', 'admin@woowa.com', 'ADMIN');
INSERT INTO member (name, password, email, role) VALUES ('user1', 'asd', 'user1@woowa.com', 'MEMBER');
INSERT INTO member (name, password, email, role) VALUES ('user2', 'asd', 'user2@woowa.com', 'MEMBER');
INSERT INTO member (name, password, email, role) VALUES ('user3', 'asd', 'user3@woowa.com', 'MEMBER');

INSERT INTO reservation (member_id, theme_id, reservation_time_id, date) VALUES (1, 1, 1, CURRENT_DATE());
INSERT INTO reservation (member_id, theme_id, reservation_time_id, date) VALUES (1, 1, 1, CURRENT_DATE() + 1);
INSERT INTO reservation (member_id, theme_id, reservation_time_id, date) VALUES (1, 1, 1, CURRENT_DATE() + 2);
INSERT INTO reservation (member_id, theme_id, reservation_time_id, date) VALUES (2, 1, 2, CURRENT_DATE());
INSERT INTO reservation (member_id, theme_id, reservation_time_id, date) VALUES (2, 1, 2, CURRENT_DATE() + 1);
INSERT INTO reservation (member_id, theme_id, reservation_time_id, date) VALUES (2, 1, 2, CURRENT_DATE() + 2);

INSERT INTO waiting (member_id, theme_id, reservation_time_id, date) VALUES (2, 1, 1, CURRENT_DATE());
INSERT INTO waiting (member_id, theme_id, reservation_time_id, date) VALUES (3, 1, 1, CURRENT_DATE());
INSERT INTO waiting (member_id, theme_id, reservation_time_id, date) VALUES (4, 1, 1, CURRENT_DATE());

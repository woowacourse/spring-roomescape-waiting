INSERT INTO theme(name, description, thumbnail) VALUES ('테마1', '설명1', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time(start_at) VALUES ('10:00');
INSERT INTO reservation_time(start_at) VALUES ('11:00');
INSERT INTO reservation_time(start_at) VALUES ('12:00');

INSERT INTO member(name, email, password, role) VALUES('리니', 'lini@email.com', 'lini123', 'GUEST');

INSERT INTO reservation(date, time_id, member_id, theme_id, status) VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 1, 1, `RESERVED`);

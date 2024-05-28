INSERT INTO theme(name, description, thumbnail) VALUES ('테마1', '설명1', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time(start_at) VALUES ('10:00');
INSERT INTO reservation_time(start_at) VALUES ('11:00');
INSERT INTO reservation_time(start_at) VALUES ('12:00');

INSERT INTO member(name, email, password, role) VALUES('리니', 'lini@email.com', 'lini123', 'GUEST');
INSERT INTO member(name, email, password, role) VALUES('admin', 'admin@email.com', 'admin123', 'ADMIN');
INSERT INTO member(name, email, password, role) VALUES('guest', 'guest@email.com', 'guest123', 'GUEST');

INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 1);

INSERT INTO reservation(detail_id, member_id, status) VALUES (1, 1, 'RESERVED');

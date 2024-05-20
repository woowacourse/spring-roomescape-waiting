INSERT INTO theme(name, description, thumbnail) VALUES ('테마1', '설명1', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('테마2', '설명2', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('테마3', '설명3', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time(start_at) VALUES ('10:00');

INSERT INTO member(name, email, password, role) VALUES('리니', 'lini@email.com', 'lini123', 'GUEST');

INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 1);

INSERT INTO reservation(reservation_detail_id, member_id, status) VALUES (1, 1, 'WAITING');

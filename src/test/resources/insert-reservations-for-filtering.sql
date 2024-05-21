INSERT INTO theme(name, description, thumbnail) VALUES ('테마1', '설명1', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('테마2', '설명2', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('테마3', '설명3', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('테마4', '설명4', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('테마5', '설명5', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time(start_at) VALUES ('10:00');
INSERT INTO reservation_time(start_at) VALUES ('11:00');
INSERT INTO reservation_time(start_at) VALUES ('12:00');

INSERT INTO member(name, email, password, role) VALUES('리니', 'lini@email.com', 'lini123', 'GUEST');
INSERT INTO member(name, email, password, role) VALUES('릴리', 'lily@email.com', 'lily123', 'GUEST');
INSERT INTO member(name, email, password, role) VALUES('토미', 'tomi@email.com', 'tomi123', 'GUEST');

INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -3, CURRENT_DATE), 2, 2);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -5, CURRENT_DATE), 3, 3);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -7, CURRENT_DATE), 1, 4);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 5);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -3, CURRENT_DATE), 2, 1);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -5, CURRENT_DATE), 3, 2);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -7, CURRENT_DATE), 1, 3);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 4);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -3, CURRENT_DATE), 2, 5);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -5, CURRENT_DATE), 3, 1);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -7, CURRENT_DATE), 1, 2);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 3);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -3, CURRENT_DATE), 2, 4);
INSERT INTO reservation_detail(date, time_id, theme_id) VALUES (DATEADD('DAY', -5, CURRENT_DATE), 3, 5);

INSERT INTO reservation(detail_id, member_id, status) VALUES (1, 1, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (2, 2, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (3, 3, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (4, 1, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (5, 2, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (6, 3, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (7, 1, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (8, 2, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (9, 3, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (10, 1, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (11, 2, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (12, 3, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (13, 1, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (14, 2, 'RESERVED');
INSERT INTO reservation(detail_id, member_id, status) VALUES (15, 3, 'RESERVED');

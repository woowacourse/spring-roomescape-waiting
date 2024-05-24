INSERT INTO theme(name, description, thumbnail) VALUES ('테마1', '설명1', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('테마2', '설명2', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time(start_at) VALUES ('10:00');
INSERT INTO reservation_time(start_at) VALUES ('12:00');

INSERT INTO member(name, email, password, role) VALUES('페드로', 'pedro@email.com', 'pedro', 'MEMBER');
INSERT INTO member(name, email, password, role) VALUES('리니', 'lini@email.com', 'lini123', 'MEMBER');
INSERT INTO member(name, email, password, role) VALUES('미르', 'duho@email.com', 'duho123', 'GUEST');

-- 테마1: 2-1-3 / 테마2: 3-2-1
INSERT INTO WAITING(member_id, theme_id, time_id, visit_date, created_at) VALUES (3, 1, 1, CURRENT_DATE, DATEADD('HOUR', -1, CURRENT_TIMESTAMP));
INSERT INTO WAITING(member_id, theme_id, time_id, visit_date, created_at) VALUES (1, 1, 1, CURRENT_DATE, DATEADD('HOUR', -2, CURRENT_TIMESTAMP));
INSERT INTO WAITING(member_id, theme_id, time_id, visit_date, created_at) VALUES (2, 1, 1, CURRENT_DATE, DATEADD('HOUR', -3, CURRENT_TIMESTAMP));
INSERT INTO WAITING(member_id, theme_id, time_id, visit_date, created_at) VALUES (1, 2, 2, CURRENT_DATE, DATEADD('HOUR', -1, CURRENT_TIMESTAMP));
INSERT INTO WAITING(member_id, theme_id, time_id, visit_date, created_at) VALUES (2, 2, 2, CURRENT_DATE, DATEADD('HOUR', -2, CURRENT_TIMESTAMP));
INSERT INTO WAITING(member_id, theme_id, time_id, visit_date, created_at) VALUES (3, 2, 2, CURRENT_DATE, DATEADD('HOUR', -3, CURRENT_TIMESTAMP));

INSERT INTO reservation(visit_date, time_id, member_id, theme_id) VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 1, 2);
INSERT INTO reservation(visit_date, time_id, member_id, theme_id) VALUES (DATEADD('DAY', -1, CURRENT_DATE), 2, 1, 1);

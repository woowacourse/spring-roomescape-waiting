INSERT INTO theme (id, name, description, thumbnail)
VALUES (1, '테마1', '재밌음', '/image/default.jpg');
INSERT INTO theme (id, name, description, thumbnail)
VALUES (2, '테마2', '무서움', '/image/default.jpg');
INSERT INTO theme (id, name, description, thumbnail)
VALUES (3, '테마3', '놀라움', '/image/default.jpg');

INSERT INTO reservation_time (id, start_at)
VALUES (1, '10:00');
INSERT INTO reservation_time (id, start_at)
VALUES (2, '11:00');
INSERT INTO reservation_time (id, start_at)
VALUES (3, '12:00');

INSERT INTO member (id, name, email, password, role)
VALUES (1, '코기', 'ind07152@naver.com', 'asd', 'user');
INSERT INTO member (id, name, email, password, role)
VALUES (2, '율무', 'ind07162@naver.com', 'asd', 'user');
INSERT INTO member (id, name, email, password, role)
VALUES (3, 'ADMIN', 'admin@naver.com', '1234', 'admin');

INSERT INTO reservation ( member_id, date, time_id, theme_id, status, created_at)
VALUES (1, DATEADD('DAY', 1, CURRENT_DATE), 2, 1, 'RESERVED', '2025-04-22 14:30:00');
INSERT INTO reservation ( member_id, date, time_id, theme_id, status, created_at)
VALUES (2, DATEADD('DAY', 1, CURRENT_DATE), 1, 2, 'RESERVED', '2025-04-22 14:30:00');
INSERT INTO reservation ( member_id, date, time_id, theme_id, status, created_at)
VALUES (2, DATEADD('DAY', 1, CURRENT_DATE), 2, 1, 'WAITED', '2025-04-22 14:30:02');

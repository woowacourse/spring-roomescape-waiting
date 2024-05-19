INSERT INTO member (name, email, password) VALUES ('클로버', 'clover@gmail.com', 'password');
INSERT INTO member (name, email, password) VALUES ('페드로', 'pedro@gmail.com', 'password');
INSERT INTO member (name, email, password, role) VALUES ('관리자', 'admin@gmail.com', 'password', 'ADMIN');

INSERT INTO reservation_time (start_at) VALUES ('10:00:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00:00');
INSERT INTO theme (name, description, thumbnail) VALUES ( '공포', '완전 무서운 테마', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg' );
INSERT INTO theme (name, description, thumbnail) VALUES ( '힐링', '완전 힐링되는 테마', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg' );
INSERT INTO theme (name, description, thumbnail) VALUES ( '힐링2', '완전 힐링되는 테마2', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg' );
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 2, '2099-12-31', 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 1, '2099-12-31', 1, 2);

INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 2, FORMATDATETIME(DATEADD('DAY', -3, NOW()), 'yyyy-MM-dd'), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 1, FORMATDATETIME(DATEADD('DAY', -3, NOW()), 'yyyy-MM-dd'), 1, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 2, FORMATDATETIME(DATEADD('DAY', -2, NOW()), 'yyyy-MM-dd'), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 1, FORMATDATETIME(DATEADD('DAY', -4, NOW()), 'yyyy-MM-dd'), 1, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 1, FORMATDATETIME(DATEADD('DAY', -2, NOW()), 'yyyy-MM-dd'), 1, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES ( 1, FORMATDATETIME(DATEADD('DAY', -2, NOW()), 'yyyy-MM-dd'), 1, 3);

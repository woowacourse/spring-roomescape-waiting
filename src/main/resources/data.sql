INSERT INTO member (name, email, password) VALUES ('클로버', 'clover@gmail.com', 'password');
INSERT INTO member (name, email, password) VALUES ('페드로', 'pedro@gmail.com', 'password');
INSERT INTO member (name, email, password, role) VALUES ('관리자', 'admin@gmail.com', 'password', 'ADMIN');

INSERT INTO reservation_time (start_at) VALUES ('10:00:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00:00');

INSERT INTO theme (name, description, thumbnail) VALUES ( '공포', '완전 무서운 테마', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg' );
INSERT INTO theme (name, description, thumbnail) VALUES ( '힐링', '완전 힐링되는 테마', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg' );
INSERT INTO theme (name, description, thumbnail) VALUES ( '힐링2', '완전 힐링되는 테마2', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg' );

INSERT INTO reservation (date, time_id, theme_id) VALUES ( '2099-12-31', 1, 1);
INSERT INTO reservation (date, time_id, theme_id) VALUES ( '2099-12-31', 1, 2);
INSERT INTO reservation (date, time_id, theme_id) VALUES ( FORMATDATETIME(DATEADD('DAY', -3, NOW()), 'yyyy-MM-dd'), 1, 1);
INSERT INTO reservation (date, time_id, theme_id) VALUES ( FORMATDATETIME(DATEADD('DAY', -3, NOW()), 'yyyy-MM-dd'), 1, 2);
INSERT INTO reservation (date, time_id, theme_id) VALUES ( FORMATDATETIME(DATEADD('DAY', -2, NOW()), 'yyyy-MM-dd'), 1, 1);
INSERT INTO reservation (date, time_id, theme_id) VALUES ( FORMATDATETIME(DATEADD('DAY', -4, NOW()), 'yyyy-MM-dd'), 1, 2);
INSERT INTO reservation (date, time_id, theme_id) VALUES ( FORMATDATETIME(DATEADD('DAY', -2, NOW()), 'yyyy-MM-dd'), 1, 2);
INSERT INTO reservation (date, time_id, theme_id) VALUES ( FORMATDATETIME(DATEADD('DAY', -2, NOW()), 'yyyy-MM-dd'), 1, 3);

INSERT INTO member_reservation (member_id, reservation_id) VALUES ( 2, 1 );
INSERT INTO member_reservation (member_id, reservation_id) VALUES ( 1, 2 );
INSERT INTO member_reservation (member_id, reservation_id) VALUES ( 2, 3 );
INSERT INTO member_reservation (member_id, reservation_id) VALUES ( 1, 4 );
INSERT INTO member_reservation (member_id, reservation_id) VALUES ( 2, 5 );
INSERT INTO member_reservation (member_id, reservation_id) VALUES ( 1, 6 );
INSERT INTO member_reservation (member_id, reservation_id) VALUES ( 1, 7 );
INSERT INTO member_reservation (member_id, reservation_id) VALUES ( 1, 8 );

INSERT INTO member_reservation (member_id, reservation_id, status ) VALUES ( 1, 3, 'WAITING' );
INSERT INTO member_reservation (member_id, reservation_id, status ) VALUES ( 3, 3, 'WAITING' );

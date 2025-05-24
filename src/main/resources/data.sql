INSERT INTO reservation_time(start_at)
VALUES ('10:00');
INSERT INTO reservation_time(start_at)
VALUES ('11:00');
INSERT INTO reservation_time(start_at)
VALUES ('12:00');
INSERT INTO reservation_time(start_at)
VALUES ('13:00');
INSERT INTO reservation_time(start_at)
VALUES ('14:00');

INSERT INTO theme(name, description, thumbnail)
VALUES ('테마1', '테마1입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마2', '테마2입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마3', '테마3입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마4', '테마4입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마5', '테마5입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마6', '테마6입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마7', '테마7입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마8', '테마8입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마9', '테마9입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마10', '테마10입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마11', '테마11입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO member (name, email, password, role)
VALUES ('사용자1', 'aaa@gmail.com', '1234', 'USER');
INSERT INTO member(name, email, password, role)
VALUES ('사용자2', 'bbb@gmail.com', '1234', 'USER');
INSERT INTO member(name, email, password, role)
VALUES ('사용자3', 'ccc@gmail.com', '1234', 'USER');
INSERT INTO member(name, email, password, role)
VALUES ('어드민', 'admin@gmail.com', '1234', 'ADMIN');


INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 1, 11);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 2, 2, 11);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 3, 3, 11);

INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 1, 4, 9);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 2, 5, 9);

INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 1, 1);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 2, 2, 2);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 3, 3, 3);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 1, 4, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 2, 5, 5);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 3, 1, 6);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 1, 2, 7);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 2, 3, 8);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -8, CURRENT_DATE), 3, 4, 10);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -8, CURRENT_DATE), 1, 5, 10);

INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', +1, CURRENT_DATE), 1, 1, 1);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', +1, CURRENT_DATE), 1, 2, 1);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', +1, CURRENT_DATE), 1, 3, 1);

INSERT INTO waiting(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', +1, CURRENT_DATE), 1, 1, 1);

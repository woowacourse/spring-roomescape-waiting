INSERT INTO reservation_time(start_at)
VALUES ('10:00');
INSERT INTO reservation_time(start_at)
VALUES ('15:00');

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

INSERT INTO member (name, email, password)
VALUES ('사용자1', 'aaa@gmail.com', '1234');
INSERT INTO member(name, email, password)
VALUES ('사용자2', 'bbb@gmail.com', '1234');
INSERT INTO member(name, email, password)
VALUES ('사용자3', 'ccc@gmail.com', '1234');
INSERT INTO member(name, email, password, role)
VALUES ('어드민', 'admin@gmail.com', '1234', 'ADMIN');

INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-30', 1, 1, 11);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-30', 1, 2, 11);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-29', 1, 1, 11);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-29', 1, 2, 9);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-28', 1, 2, 9);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-28', 1, 1, 8);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-27', 1, 1, 1);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-27', 1, 2, 2);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-26', 1, 1, 3);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-26', 1, 2, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-25', 1, 1, 5);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-25', 1, 2, 6);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-24', 1, 1, 7);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-24', 1, 2, 10);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-7', 1, 2, 10);

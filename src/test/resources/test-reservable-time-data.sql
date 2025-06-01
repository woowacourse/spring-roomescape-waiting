INSERT INTO reservation_time(start_at)
VALUES ('10:00');
INSERT INTO reservation_time(start_at)
VALUES ('11:00');
INSERT INTO reservation_time(start_at)
VALUES ('12:00');

INSERT INTO theme(name, description, thumbnail)
VALUES ('테마1', '테마1입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마2', '테마2입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마3', '테마3입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO member (name, email, password)
VALUES ('사용자1', 'aaa@gmail.com', '1234');
INSERT INTO member(name, email, password)
VALUES ('사용자2', 'bbb@gmail.com', '1234');
INSERT INTO member(name, email, password)
VALUES ('사용자3', 'ccc@gmail.com', '1234');

-- 테마1 -> 5월 1일에 timeId1, timeId2 예약 됨
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-01', 1, 1, 1);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-01', 1, 2, 1);

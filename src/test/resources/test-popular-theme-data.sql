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
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마4', '테마4입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO member (name, email, password)
VALUES ('사용자1', 'aaa@gmail.com', '1234');
INSERT INTO member(name, email, password)
VALUES ('사용자2', 'bbb@gmail.com', '1234');
INSERT INTO member(name, email, password)
VALUES ('사용자3', 'ccc@gmail.com', '1234');

-- 테마1은 0501 ~ 0503 동안 3번 예약됨
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-01', 1, 1, 1);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-02', 1, 1, 1);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-03', 1, 1, 1);

-- 테마2는 0501 ~ 0503 동안 2번 예약됨
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-01', 2, 1, 2);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-02', 2, 1, 2);

-- 테마3은 0501 ~ 0503 동안 1번 예약됨
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-01', 3, 1, 3);

-- 테마3은 0401 ~ 0404 동안 4번 예약됨
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-01', 1, 1, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-02', 1, 1, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-03', 1, 1, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-04', 1, 1, 4);

INSERT INTO theme(name, description, thumbnail)
VALUES ('테마1', '테마1입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마2', '테마2입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마3', '테마3입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마4', '테마4입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

-- 2 -> 1 -> 3 인기 테마

-- 테마1은 0501 ~ 0503 동안 2번 예약됨
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-11', 1, 1, 1);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-12', 1, 1, 1);

-- 테마2는 0501 ~ 0503 동안 3번 예약됨
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-11', 2, 1, 2);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-12', 2, 1, 2);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-13', 1, 1, 2);

-- 테마3은 0501 ~ 0503 동안 1번 예약됨
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-05-11', 3, 1, 3);

-- 테마4는 0401 ~ 0404 동안 4번 예약됨
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-11', 1, 1, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-12', 1, 1, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-13', 1, 1, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES ('2025-04-14', 1, 1, 4);

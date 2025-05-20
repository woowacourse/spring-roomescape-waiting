INSERT INTO theme(name, description, thumbnail)
VALUES ('테마1', '테마1입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마2', '테마2입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마3', '테마3입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('테마4', '테마4입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

-- 2 -> 1 -> 3 인기 테마

-- 테마1은 최근 일주일 동안 2번 예약됨
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 1, 1);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 1, 1, 1);

-- 테마2는 최근 일주일 동안 3번 예약됨
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 2, 1, 2);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 2, 1, 2);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 1, 2);

-- 테마3은 최근 일주일 동안 1번 예약됨
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 3, 1, 3);

-- 테마4는 4번 예약됐지만, 최근 일주일보다 과거의 시점임
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -8, CURRENT_DATE), 1, 1, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -9, CURRENT_DATE), 1, 1, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -10, CURRENT_DATE), 1, 1, 4);
INSERT INTO reservation(date, member_id, time_id, theme_id)
VALUES (DATEADD('DAY', -11, CURRENT_DATE), 1, 1, 4);
INSERT INTO reservation_time(start_at)
VALUES ('10:00:00');

INSERT INTO theme(name, description, thumbnail)
VALUES ('레벨2 탈출', '우테코 레벨2 탈출기!', 'https://img.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('레벨3 탈출', '우테코 레벨3 탈출기!', 'https://img.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('레벨4 탈출', '우테코 레벨4 탈출기!', 'https://img.jpg');

INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (1, CURRENT_DATE - 1, 1, 3);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (1, CURRENT_DATE - 2, 1, 3);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (1, CURRENT_DATE - 3, 1, 3);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (1, CURRENT_DATE - 4, 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (1, CURRENT_DATE - 5, 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (1, CURRENT_DATE - 6, 1, 2);

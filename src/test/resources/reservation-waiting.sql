-- ID 1~3: 예약 대기
INSERT INTO reservation_waiting (member_id, date, time_id, theme_id) VALUES (2, DATEADD('DAY', -1,  CURRENT_DATE), 1, 1);
INSERT INTO reservation_waiting (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY',  12, CURRENT_DATE), 2, 1);
INSERT INTO reservation_waiting (member_id, date, time_id, theme_id) VALUES (3, DATEADD('DAY', -1,  CURRENT_DATE), 1, 1);

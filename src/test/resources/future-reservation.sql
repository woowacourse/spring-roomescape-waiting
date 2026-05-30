-- ID 11~12: 미래 예약
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY', 11, CURRENT_DATE), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, DATEADD('DAY', 12, CURRENT_DATE), 2, 1);
-- ID 1~10: 과거 예약
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY', -1,  CURRENT_DATE), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, DATEADD('DAY', -2,  CURRENT_DATE), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (3, DATEADD('DAY', -3,  CURRENT_DATE), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY', -4,  CURRENT_DATE), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, DATEADD('DAY', -5,  CURRENT_DATE), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY', -1,  CURRENT_DATE), 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, DATEADD('DAY', -2,  CURRENT_DATE), 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (3, DATEADD('DAY', -3,  CURRENT_DATE), 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY', -1,  CURRENT_DATE), 3, 3);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY', -10, CURRENT_DATE), 1, 4);

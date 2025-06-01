INSERT INTO waiting(date, member_id, time_id, theme_id, created_at)
VALUES (DATEADD('DAY', 1, CURRENT_DATE), 1, 1, 1, DATEADD('DAY', -1, CURRENT_DATE));
INSERT INTO waiting(date, member_id, time_id, theme_id, created_at)
VALUES (DATEADD('DAY', 1, CURRENT_DATE), 2, 1, 1, CURRENT_DATE);

INSERT INTO reservation(date, member_id, time_id, theme_id) -- 예약1
VALUES (DATEADD('DAY', 1, CURRENT_DATE), 2, 1, 1);
INSERT INTO reservation(date, member_id, time_id, theme_id) -- 예약2
VALUES (DATEADD('DAY', 1, CURRENT_DATE), 2, 2, 2);
INSERT INTO reservation(date, member_id, time_id, theme_id) -- 예약3
VALUES (DATEADD('DAY', 1, CURRENT_DATE), 1, 3, 3);

INSERT INTO waiting(member_id, reservation_id, created_at) -- 예약2에 대한 사용자 1의 예약 대기
VALUES (1, 2, NOW());
INSERT INTO waiting(member_id, reservation_id, created_at) -- 예약3에 대한 사용자 2의 예약 대기
VALUES (2, 3, NOW());

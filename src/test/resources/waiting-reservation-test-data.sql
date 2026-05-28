INSERT INTO reservation_date (play_day)
VALUES (CURRENT_DATE + 10);

INSERT INTO reservation_time (start_at)
VALUES ('22:00');

INSERT INTO reservation_time (start_at)
VALUES ('12:00');

INSERT INTO theme (name, content, url)
VALUES ('테스트테마', '설명', 'url');

INSERT INTO reservation (name, date_id, time_id, theme_id)
VALUES ('기존예약자', 1, 1, 1);

INSERT INTO waiting_reservation (name, date_id, time_id, theme_id, created_at)
VALUES ('기존대기자', 1, 1, 1, CURRENT_TIMESTAMP);

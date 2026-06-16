INSERT INTO theme (name, description, thumbnail_image_url, price)
VALUES ('공포', '어마무시한 공포 테마', 'https://theme.com/image.png', 30000); -- id 1

INSERT INTO reservation_time (start_at, status)
VALUES ('10:00:00', 'ACTIVE'), -- id 1
       ('11:00:00', 'ACTIVE');
-- id 2

-- 변경 대상이 되는 기존 예약: 내일 / 공포 / 10:00
INSERT INTO reservation (date, theme_id, time_id)
VALUES (DATEADD('DAY', 1, CURRENT_DATE), 1, 1); -- id 1

INSERT INTO reservation_entry (name, reservation_id, status, created_at)
VALUES ('라텔', 1, 'RESERVED', CURRENT_TIMESTAMP); -- id 1

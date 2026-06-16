SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE reservation_slot RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE reservation_date RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO reservation_date (date, is_active)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), TRUE);

INSERT INTO reservation_time (start_at, is_active)
VALUES ('09:00:00', TRUE);

INSERT INTO theme (name, description, thumbnail_url, is_active, amount)
VALUES ('테마1', '설명1', 'https://example.com/theme1.png', TRUE, 1000);

INSERT INTO reservation_slot (date_id, time_id, theme_id)
SELECT
    (SELECT id FROM reservation_date WHERE date = DATEADD('DAY', -1, CURRENT_DATE)),
    (SELECT id FROM reservation_time WHERE start_at = '09:00:00'),
    (SELECT id FROM theme WHERE name = '테마1');

INSERT INTO reservation (name, slot_id, reserved_at, status)
SELECT
    'member',
    rs.id,
    DATEADD('DAY', -2, CURRENT_TIMESTAMP),
    'RESERVED'
FROM reservation_slot rs
WHERE rs.date_id  = (SELECT id FROM reservation_date WHERE date = DATEADD('DAY', -1, CURRENT_DATE))
  AND rs.time_id  = (SELECT id FROM reservation_time WHERE start_at = '09:00:00')
  AND rs.theme_id = (SELECT id FROM theme WHERE name = '테마1');

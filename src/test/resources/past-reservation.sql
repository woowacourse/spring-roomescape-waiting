SET
    REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE reservation_date RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
SET
    REFERENTIAL_INTEGRITY TRUE;

INSERT INTO reservation_date (date, is_active)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), TRUE);

INSERT INTO reservation_time (start_at, is_active)
VALUES ('09:00:00', TRUE);

INSERT INTO theme (name, description, thumbnail_url, is_active)
VALUES ('테마1', '설명1', 'https://example.com/theme1.png', TRUE);

INSERT INTO reservation (member_id, date_id, time_id, theme_id, waiting_order, status)
VALUES (SELECT id FROM member WHERE name = 'member',
        SELECT id FROM reservation_date WHERE date = DATEADD('DAY', - 1, CURRENT_DATE),
        SELECT id FROM reservation_time WHERE start_at = '09:00:00',
        SELECT id FROM theme WHERE name = '테마1',
        0,
        'RESERVED');

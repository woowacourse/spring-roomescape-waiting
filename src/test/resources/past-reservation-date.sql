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
VALUES ('10:00:00', TRUE);

INSERT INTO theme (name, description, thumbnail_url, is_active, amount)
VALUES ('잠겨버린 연구실', '제한 시간 안에 단서를 찾아 연구실을 탈출해야 합니다.', 'https://images.unsplash.com/photo-1518005020951-eccb494ad742', TRUE, 25000);

INSERT INTO reservation_slot (date_id, time_id, theme_id)
SELECT rd.id, rt.id, t.id
FROM reservation_date rd
         CROSS JOIN reservation_time rt
         CROSS JOIN theme t
WHERE rd.date     = DATEADD('DAY', -1, CURRENT_DATE)
  AND rt.start_at = '10:00:00'
  AND t.name      = '잠겨버린 연구실';

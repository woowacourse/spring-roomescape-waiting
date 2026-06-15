SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE waiting RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO theme (name, thumbnail_url, description)
VALUES ('시간 조회 테마', 'https://picsum.photos/seed/time-query/400/300', '예약 가능 시간 조회 테스트용 테마');

INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('14:00');

INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('reserved_user', '2026-06-05', 1, 1);

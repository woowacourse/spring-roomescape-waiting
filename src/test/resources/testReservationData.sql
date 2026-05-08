SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO theme (name, thumbnail_url, description)
VALUES ('공포의 저택', 'https://picsum.photos/seed/horror/400/300', '어둠 속에 숨겨진 공포를 체험하세요');

INSERT INTO theme (name, thumbnail_url, description)
VALUES ('예약없는테마', 'https://picsum.photos/seed/empty/400/300', '예약이 없는 테마');

INSERT INTO reservation_time (start_at)
VALUES ('10:00');

INSERT INTO reservation_time (start_at)
VALUES ('12:00');

INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user_a', '2026-04-28', 1, 1);

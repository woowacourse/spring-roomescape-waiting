SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE reservation_waiting RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO theme (name, thumbnail_url, description)
VALUES ('기본 테마', 'https://picsum.photos/seed/horror/400/300', '기본 테마입니다.');

INSERT INTO reservation_time (start_at) VALUES ('10:00');

-- 미래 예약 1 (예약 생성 실패 롤백 테스트용, id=1)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user_a', '2026-06-05', 1, 1);

-- 잘못된 예약 대기 (예약 생성 실패시 롤백 발생 테스트용)
INSERT INTO reservation_waiting (name, date, time_id, theme_id, created_at)
VALUES ('', '2026-06-05', 1, 1, '2026-04-28 10:30:00');

-- 미래 예약 2 (DB 예외 발생 롤백 테스트용, id=2)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user_b', '2026-06-06', 1, 1);

-- 미래 예약 2에 대한 예약 대기 (DB 예외 롤백 발생 테스트용)
INSERT INTO reservation_waiting (name, date, time_id, theme_id, created_at)
VALUES ('user_c', '2026-06-06', 1, 1, '2026-04-30 10:30:00');

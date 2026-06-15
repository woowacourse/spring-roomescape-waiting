SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE waiting RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO theme (name, thumbnail_url, description)
VALUES ('공포의 저택', 'https://picsum.photos/seed/horror/400/300', '어둠 속에 숨겨진 공포를 체험하세요');

INSERT INTO theme (name, thumbnail_url, description)
VALUES ('예약없는테마', 'https://picsum.photos/seed/empty/400/300', '예약이 없는 테마');

INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00');
INSERT INTO reservation_time (start_at) VALUES ('13:00');

-- 과거 예약 (fixed clock: 2026-05-05)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user_a', '2026-04-28', 1, 1);

-- 미래 예약 (취소/변경 테스트 대상, id=2)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user_b', '2026-06-05', 2, 1);

-- 중복 검증용 (user_b가 2026-06-05/time_id=1로 변경 시 409, id=3)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user_c', '2026-06-05', 1, 1);

-- 과거 예약 대기 (id=1: 취소 불가 테스트용)
INSERT INTO waiting (name, date, time_id, theme_id, created_at)
VALUES ('user_d', '2026-04-28', 1, 1, '2026-04-27 10:30:00');

-- 미래 예약 대기 (id=2: user_d, 2026-06-05/time2/theme1 → 취소 가능 테스트용)
INSERT INTO waiting (name, date, time_id, theme_id, created_at)
VALUES ('user_d', '2026-06-05', 2, 1, '2026-06-05 10:30:00');

-- 내 예약 & 대기 조회 테스트용
-- user_e: 2026-06-05/time1/theme1 대기 1번 (id=3)
INSERT INTO waiting (name, date, time_id, theme_id, created_at)
VALUES ('user_e', '2026-06-05', 1, 1, '2026-06-01 09:00:00');

-- user_b: 2026-06-05/time1/theme1 대기 2번 (id=4) → 예약(id=2) + 대기 동시 보유
INSERT INTO waiting (name, date, time_id, theme_id, created_at)
VALUES ('user_b', '2026-06-05', 1, 1, '2026-06-01 10:00:00');

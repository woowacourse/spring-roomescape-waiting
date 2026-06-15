SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE waiting RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

-- theme (11개 - 10위권 밖 케이스도 포함)
INSERT INTO theme (name, thumbnail_url, description)
VALUES ('공포의 저택', 'https://picsum.photos/seed/horror/400/300', '어둠 속에 숨겨진 공포를 체험하세요'),
       ('우주 탐험대', 'https://picsum.photos/seed/space/400/300', '은하계를 누비는 우주 탐험'),
       ('탐정 사무소', 'https://picsum.photos/seed/detective/400/300', '단서를 모아 사건을 해결하라'),
       ('마법사의 탑', 'https://picsum.photos/seed/magic/400/300', '마법이 살아 숨쉬는 신비의 탑'),
       ('해적선', 'https://picsum.photos/seed/pirate/400/300', '보물을 찾아 망망대해를 항해'),
       ('고대 신전', 'https://picsum.photos/seed/temple/400/300', '잊혀진 문명의 비밀을 파헤쳐라'),
       ('좀비 연구소', 'https://picsum.photos/seed/zombie/400/300', '바이러스 확산을 막아라'),
       ('타임머신', 'https://picsum.photos/seed/time/400/300', '과거와 미래를 넘나드는 시간 여행'),
       ('사막의 오아시스', 'https://picsum.photos/seed/desert/400/300', '사막 한가운데 숨겨진 비밀'),
       ('폐광', 'https://picsum.photos/seed/mine/400/300', '버려진 광산 속 미스터리'),
       ('유령 호텔', 'https://picsum.photos/seed/ghost/400/300', '체크아웃할 수 없는 호텔');

-- reservation_time
INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('14:00');

-- reservation (집계 기간: 04-28 ~ 05-04)
-- 예상 순위: 1(7건) > 2(6건) > 3(5건) > 4(4건) > 5(4건) > 6(3건) > 7(3건) > 8(2건) > 9(2건) > 10(1건) / 11(0건)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES
-- 테마1: 7건
('user_a', '2026-04-28', 1, 1),
('user_b', '2026-04-29', 1, 1),
('user_c', '2026-04-30', 1, 1),
('user_d', '2026-05-01', 1, 1),
('user_e', '2026-05-02', 1, 1),
('user_f', '2026-05-03', 1, 1),
('user_g', '2026-05-04', 1, 1),
-- 테마2: 6건
('user_a', '2026-04-28', 2, 2),
('user_b', '2026-04-29', 2, 2),
('user_c', '2026-04-30', 2, 2),
('user_d', '2026-05-01', 2, 2),
('user_e', '2026-05-02', 2, 2),
('user_f', '2026-05-03', 2, 2),
-- 테마3: 5건
('user_a', '2026-04-28', 1, 3),
('user_b', '2026-04-30', 1, 3),
('user_c', '2026-05-01', 1, 3),
('user_d', '2026-05-03', 1, 3),
('user_e', '2026-05-04', 1, 3),
-- 테마4: 4건
('user_a', '2026-04-29', 2, 4),
('user_b', '2026-05-01', 2, 4),
('user_c', '2026-05-03', 2, 4),
('user_d', '2026-05-04', 2, 4),
-- 테마5: 4건
('user_a', '2026-04-28', 1, 5),
('user_b', '2026-04-30', 1, 5),
('user_c', '2026-05-02', 1, 5),
('user_d', '2026-05-04', 1, 5),
-- 테마6: 3건
('user_a', '2026-04-29', 2, 6),
('user_b', '2026-05-02', 2, 6),
('user_c', '2026-05-04', 2, 6),
-- 테마7: 3건
('user_a', '2026-04-28', 1, 7),
('user_b', '2026-05-01', 1, 7),
('user_c', '2026-05-03', 1, 7),
-- 테마8: 2건
('user_a', '2026-04-30', 2, 8),
('user_b', '2026-05-02', 2, 8),
-- 테마9: 2건
('user_a', '2026-05-01', 1, 9),
('user_b', '2026-05-04', 1, 9),
-- 테마10: 1건 (10위)
('user_a', '2026-05-03', 2, 10),
-- 테마11: 0건 (집계 기간 외 데이터 - 범위 필터 검증용)
('user_a', '2026-04-27', 1, 11),
('user_b', '2026-05-05', 1, 11),
('future_user', '2026-06-05', 1, 7);

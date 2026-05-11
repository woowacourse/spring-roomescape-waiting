-- 조회 기간: 2026-04-29 ~ 2026-05-05
-- 기대 순위:
-- Theme 1: 기간 내 예약 10개
-- Theme 2: 기간 내 예약 9개
-- Theme 3: 기간 내 예약 8개
-- Theme 4: 기간 내 예약 7개
-- Theme 5: 기간 내 예약 6개
-- Theme 6: 기간 내 예약 5개
-- Theme 7: 기간 내 예약 4개
-- Theme 8: 기간 내 예약 3개
-- Theme 9: 기간 내 예약 2개
-- Theme 10: 기간 내 예약 1개
-- Theme 11: 기간 밖 예약만 있음
-- Theme 12: 예약 없음

INSERT INTO theme (id, name, description, thumbnail)
VALUES (1, 'Theme 1', 'Popular theme rank 1', 'https://example.com/theme-1.png'),
       (2, 'Theme 2', 'Popular theme rank 2', 'https://example.com/theme-2.png'),
       (3, 'Theme 3', 'Popular theme rank 3', 'https://example.com/theme-3.png'),
       (4, 'Theme 4', 'Popular theme rank 4', 'https://example.com/theme-4.png'),
       (5, 'Theme 5', 'Popular theme rank 5', 'https://example.com/theme-5.png'),
       (6, 'Theme 6', 'Popular theme rank 6', 'https://example.com/theme-6.png'),
       (7, 'Theme 7', 'Popular theme rank 7', 'https://example.com/theme-7.png'),
       (8, 'Theme 8', 'Popular theme rank 8', 'https://example.com/theme-8.png'),
       (9, 'Theme 9', 'Popular theme rank 9', 'https://example.com/theme-9.png'),
       (10, 'Theme 10', 'Popular theme rank 10', 'https://example.com/theme-10.png'),
       (11, 'Theme 11', 'Out of range reservations only', 'https://example.com/theme-11.png'),
       (12, 'Theme 12', 'No reservations', 'https://example.com/theme-12.png');

INSERT INTO reservation_time (id, start_at)
VALUES (1, '10:00:00'),
       (2, '12:00:00'),
       (3, '14:00:00'),
       (4, '16:00:00'),
       (5, '18:00:00'),
       (6, '20:00:00');

-- Theme 1: 기간 내 예약 10개
INSERT INTO reservation (id, name, date, time_id, theme_id)
VALUES (1, 'theme-1-guest-1', '2026-04-29', 1, 1),
       (2, 'theme-1-guest-2', '2026-04-29', 2, 1),
       (3, 'theme-1-guest-3', '2026-04-30', 1, 1),
       (4, 'theme-1-guest-4', '2026-04-30', 2, 1),
       (5, 'theme-1-guest-5', '2026-05-01', 1, 1),
       (6, 'theme-1-guest-6', '2026-05-01', 2, 1),
       (7, 'theme-1-guest-7', '2026-05-02', 1, 1),
       (8, 'theme-1-guest-8', '2026-05-03', 1, 1),
       (9, 'theme-1-guest-9', '2026-05-04', 1, 1),
       (10, 'theme-1-guest-10', '2026-05-05', 1, 1);

-- Theme 2: 기간 내 예약 9개
INSERT INTO reservation (id, name, date, time_id, theme_id)
VALUES (11, 'theme-2-guest-1', '2026-04-29', 1, 2),
       (12, 'theme-2-guest-2', '2026-04-29', 2, 2),
       (13, 'theme-2-guest-3', '2026-04-30', 1, 2),
       (14, 'theme-2-guest-4', '2026-04-30', 2, 2),
       (15, 'theme-2-guest-5', '2026-05-01', 1, 2),
       (16, 'theme-2-guest-6', '2026-05-01', 2, 2),
       (17, 'theme-2-guest-7', '2026-05-02', 1, 2),
       (18, 'theme-2-guest-8', '2026-05-03', 1, 2),
       (19, 'theme-2-guest-9', '2026-05-04', 1, 2);

-- Theme 3: 기간 내 예약 8개
INSERT INTO reservation (id, name, date, time_id, theme_id)
VALUES (20, 'theme-3-guest-1', '2026-04-29', 1, 3),
       (21, 'theme-3-guest-2', '2026-04-29', 2, 3),
       (22, 'theme-3-guest-3', '2026-04-30', 1, 3),
       (23, 'theme-3-guest-4', '2026-04-30', 2, 3),
       (24, 'theme-3-guest-5', '2026-05-01', 1, 3),
       (25, 'theme-3-guest-6', '2026-05-01', 2, 3),
       (26, 'theme-3-guest-7', '2026-05-02', 1, 3),
       (27, 'theme-3-guest-8', '2026-05-03', 1, 3);

-- Theme 4: 기간 내 예약 7개
INSERT INTO reservation (id, name, date, time_id, theme_id)
VALUES (28, 'theme-4-guest-1', '2026-04-29', 1, 4),
       (29, 'theme-4-guest-2', '2026-04-29', 2, 4),
       (30, 'theme-4-guest-3', '2026-04-30', 1, 4),
       (31, 'theme-4-guest-4', '2026-04-30', 2, 4),
       (32, 'theme-4-guest-5', '2026-05-01', 1, 4),
       (33, 'theme-4-guest-6', '2026-05-01', 2, 4),
       (34, 'theme-4-guest-7', '2026-05-02', 1, 4);

-- Theme 5: 기간 내 예약 6개
INSERT INTO reservation (id, name, date, time_id, theme_id)
VALUES (35, 'theme-5-guest-1', '2026-04-29', 1, 5),
       (36, 'theme-5-guest-2', '2026-04-29', 2, 5),
       (37, 'theme-5-guest-3', '2026-04-30', 1, 5),
       (38, 'theme-5-guest-4', '2026-04-30', 2, 5),
       (39, 'theme-5-guest-5', '2026-05-01', 1, 5),
       (40, 'theme-5-guest-6', '2026-05-01', 2, 5);

-- Theme 6: 기간 내 예약 5개
INSERT INTO reservation (id, name, date, time_id, theme_id)
VALUES (41, 'theme-6-guest-1', '2026-04-29', 1, 6),
       (42, 'theme-6-guest-2', '2026-04-29', 2, 6),
       (43, 'theme-6-guest-3', '2026-04-30', 1, 6),
       (44, 'theme-6-guest-4', '2026-04-30', 2, 6),
       (45, 'theme-6-guest-5', '2026-05-01', 1, 6);

-- Theme 7: 기간 내 예약 4개
INSERT INTO reservation (id, name, date, time_id, theme_id)
VALUES (46, 'theme-7-guest-1', '2026-04-29', 1, 7),
       (47, 'theme-7-guest-2', '2026-04-29', 2, 7),
       (48, 'theme-7-guest-3', '2026-04-30', 1, 7),
       (49, 'theme-7-guest-4', '2026-04-30', 2, 7);

-- Theme 8: 기간 내 예약 3개
INSERT INTO reservation (id, name, date, time_id, theme_id)
VALUES (50, 'theme-8-guest-1', '2026-04-29', 1, 8),
       (51, 'theme-8-guest-2', '2026-04-29', 2, 8),
       (52, 'theme-8-guest-3', '2026-04-30', 1, 8);

-- Theme 9: 기간 내 예약 2개
INSERT INTO reservation (id, name, date, time_id, theme_id)
VALUES (53, 'theme-9-guest-1', '2026-04-29', 1, 9),
       (54, 'theme-9-guest-2', '2026-04-29', 2, 9);

-- Theme 10: 기간 내 예약 1개
INSERT INTO reservation (id, name, date, time_id, theme_id)
VALUES (55, 'theme-10-guest-1', '2026-04-29', 1, 10);

-- Theme 11: 기간 밖 예약만 있음
INSERT INTO reservation (id, name, date, time_id, theme_id)
VALUES (56, 'theme-11-out-1', '2026-04-28', 1, 11),
       (57, 'theme-11-out-2', '2026-05-06', 1, 11),
       (58, 'theme-11-out-3', '2026-05-06', 2, 11);

-- Theme 12: 예약 없음

ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 13;
ALTER TABLE reservation_time
    ALTER COLUMN id RESTART WITH 7;
ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 59;

INSERT INTO theme (id, name, description, thumbnail_url)
VALUES
    (1, '미술관의 밤', '고요한 미술관에서 단서를 모아 탈출하는 추리 테마', 'https://example.com/themes/museum-night.png'),
    (2, '심해 연구소', '해저 연구소의 사고 원인을 추적하는 SF 테마', 'https://example.com/themes/deep-sea-lab.png'),
    (3, '폐병원 탈출', '버려진 병원에서 탈출하는 공포 테마', 'https://example.com/themes/hospital.png'),
    (4, '한밤의 서점', '비밀 통로가 숨겨진 서점에서 단서를 수집하는 미스터리 테마', 'https://example.com/themes/bookstore.png'),
    (5, '빙하 기지', '얼어붙은 관측 기지에서 생존 루트를 찾는 서바이벌 테마', 'https://example.com/themes/glacier-base.png'),
    (6, '황금 사원', '사원의 봉인을 해제하고 보물을 찾는 어드벤처 테마', 'https://example.com/themes/golden-temple.png'),
    (7, '달 기지 탈출', '산소가 부족한 달 기지에서 귀환선을 가동하는 SF 테마', 'https://example.com/themes/moon-base.png'),
    (8, '무도회장의 유령', '유령이 남긴 암호를 풀어야 하는 고딕 추리 테마', 'https://example.com/themes/ballroom-ghost.png'),
    (9, '사막 열차', '사막 한가운데 멈춘 열차에서 범인을 찾는 추리 테마', 'https://example.com/themes/desert-train.png'),
    (10, '해커의 방', '침입당한 서버룸에서 시스템을 복구하는 현대 스릴러 테마', 'https://example.com/themes/hacker-room.png');

ALTER TABLE theme ALTER COLUMN id RESTART WITH 11;

INSERT INTO reservation_time (id, start_at)
VALUES
    (1, '10:00:00'),
    (2, '11:30:00');

ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 3;


INSERT INTO reservation_slot (id, date, theme_id, time_id)
VALUES
    (1, DATE '2026-05-05', 1, 1),
    (2, DATE '2026-05-04', 1, 1),
    (3, DATE '2026-05-03', 1, 2),
    (4, DATE '2026-05-02', 1, 2),
    (5, DATE '2026-05-01', 1, 1),

    (6, DATE '2026-05-05', 2, 1),
    (7, DATE '2026-05-04', 2, 1),
    (8, DATE '2026-05-03', 2, 2),
    (9, DATE '2026-05-02', 2, 2),

    (10, DATE '2026-05-05', 3, 1),
    (11, DATE '2026-05-04', 3, 1),
    (12, DATE '2026-05-03', 3, 2),

    (13, DATE '2026-05-05', 4, 1),
    (14, DATE '2026-05-04', 4, 2),

    (15, DATE '2026-05-05', 5, 1),
    (16, DATE '2026-05-04', 5, 2),

    (17, DATE '2026-05-05', 6, 1),
    (18, DATE '2026-05-05', 7, 1),
    (19, DATE '2026-05-05', 8, 1),
    (20, DATE '2026-05-05', 9, 1),
    (21, DATE '2026-05-05', 10, 1),

    (22, DATE '2026-05-12', 1, 1),
    (23, DATE '2026-05-12', 2, 1);

ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 24;

INSERT INTO reservation (id, name, slot_id)
VALUES
    (1, '브라운', 1),
    (2, '코니', 2),
    (3, '샐리', 3),
    (4, '문', 4),
    (5, '제시카', 5),

    (6, '제임스', 6),
    (7, '레오', 7),
    (8, '루카', 8),
    (9, '앤디', 9),

    (10, '레너드', 10),
    (11, '초코', 11),
    (12, '브이', 12),

    (13, '에디', 13),
    (14, '리아', 14),

    (15, '마크', 15),
    (16, '니나', 16),

    (17, '제이', 17),
    (18, '하나', 18),
    (19, '오웬', 19),
    (20, '소라', 20),
    (21, '태오', 21),

    (22, '미래예약1', 22),
    (23, '미래예약2', 23);

ALTER TABLE reservation ALTER COLUMN id RESTART WITH 24;

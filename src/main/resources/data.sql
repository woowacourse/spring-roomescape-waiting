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


INSERT INTO reservation (id, name, date, theme_id, time_id)
VALUES
    (1, '브라운', DATEADD('DAY', -1, CURRENT_DATE), 1, 1),
    (2, '코니', DATEADD('DAY', -2, CURRENT_DATE), 1, 1),
    (3, '샐리', DATEADD('DAY', -3, CURRENT_DATE), 1, 2),
    (4, '문', DATEADD('DAY', -4, CURRENT_DATE), 1, 2),
    (5, '제시카', DATEADD('DAY', -5, CURRENT_DATE), 1, 1),

    (6, '제임스', DATEADD('DAY', -6, CURRENT_DATE), 2, 1),
    (7, '레오', DATEADD('DAY', -7, CURRENT_DATE), 2, 1),
    (8, '루카', DATEADD('DAY', -1, CURRENT_DATE), 2, 2),
    (9, '앤디', DATEADD('DAY', -2, CURRENT_DATE), 2, 2),

    (10, '레너드', DATEADD('DAY', -3, CURRENT_DATE), 3, 1),
    (11, '초코', DATEADD('DAY', -4, CURRENT_DATE), 3, 1),
    (12, '브이', DATEADD('DAY', -5, CURRENT_DATE), 3, 2),

    (13, '에디', DATEADD('DAY', -6, CURRENT_DATE), 4, 1),
    (14, '리아', DATEADD('DAY', -7, CURRENT_DATE), 4, 2),

    (15, '마크', DATEADD('DAY', -1, CURRENT_DATE), 5, 1),
    (16, '니나', DATEADD('DAY', -2, CURRENT_DATE), 5, 2),

    (17, '제이', DATEADD('DAY', -3, CURRENT_DATE), 6, 1),
    (18, '하나', DATEADD('DAY', -4, CURRENT_DATE), 7, 1),
    (19, '오웬', DATEADD('DAY', -5, CURRENT_DATE), 8, 1),
    (20, '소라', DATEADD('DAY', -6, CURRENT_DATE), 9, 1),
    (21, '태오', DATEADD('DAY', -7, CURRENT_DATE), 10, 1),

    (22, '미래예약1', DATEADD('DAY', +1, CURRENT_DATE), 1, 1),
    (23, '미래예약2', DATEADD('DAY', +1, CURRENT_DATE), 2, 1);

ALTER TABLE reservation ALTER COLUMN id RESTART WITH 24;

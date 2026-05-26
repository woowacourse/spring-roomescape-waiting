INSERT INTO users (id, name, username, password, role)
VALUES (1, '예약자', 'reserver@test.com', '$2a$10$dummyhash', 'MEMBER');

ALTER TABLE users
    ALTER COLUMN id RESTART WITH 10;

INSERT INTO store (id, name)
VALUES (1, '강남점');

ALTER TABLE store
    ALTER COLUMN id RESTART WITH 10;

INSERT INTO theme (id, name, description, thumbnail_image_url)
VALUES (1, '우테코 방탈출', '우테코 시그니처 테마입니다.', 'https://example.com/thumbnails/theme1.png'),
       (2, '저주받은 인형', '공포 장르 테마입니다.', 'https://example.com/thumbnails/theme2.png'),
       (3, '미스터리 살인사건', '추리 장르 테마입니다.', 'https://example.com/thumbnails/theme3.png'),
       (4, '우주 탐험', '우주 배경 SF 테마입니다.', 'https://example.com/thumbnails/theme4.png'),
       (5, '중세 성의 비밀', '중세 판타지 테마입니다.', 'https://example.com/thumbnails/theme5.png'),
       (6, '마법사의 탑', '마법 세계 테마입니다.', 'https://example.com/thumbnails/theme6.png'),
       (7, '좀비 아포칼립스', '좀비 서바이벌 테마입니다.', 'https://example.com/thumbnails/theme7.png'),
       (8, '탐정 사무소', '노아르 추리 테마입니다.', 'https://example.com/thumbnails/theme8.png'),
       (9, '해저 탐험', '심해 탐험 테마입니다.', 'https://example.com/thumbnails/theme9.png'),
       (10, '시간 여행', '타임루프 테마입니다.', 'https://example.com/thumbnails/theme10.png'),
       (11, '지하 던전', '다크 판타지 테마입니다.', 'https://example.com/thumbnails/theme11.png'),
       (12, '마피아 하우스', '사회적 추리 테마입니다.', 'https://example.com/thumbnails/theme12.png'),
       (13, '빙하 기지', '빙하 SF 테마입니다.', 'https://example.com/thumbnails/theme13.png'),
       (14, '화산 탈출', '재난 탈출 테마입니다.', 'https://example.com/thumbnails/theme14.png'),
       (15, '사막의 신전', '고대 문명 테마입니다.', 'https://example.com/thumbnails/theme15.png');

ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 20;

INSERT INTO reservation_time (id, start_at)
VALUES (1, '10:00:00'),
       (2, '11:00:00'),
       (3, '12:00:00'),
       (4, '13:00:00');

ALTER TABLE reservation_time
    ALTER COLUMN id RESTART WITH 10;

-- 테마별 예약 수: 1위(10) ~ 15위(1), 인기 테마 랭킹 테스트용
INSERT INTO reservation (id, user_id, theme_id, date, time_id, store_id, status)
VALUES
    -- 테마 1: 10건
    (1, 1, 1, '2026-05-01', 1, 1, 'RESERVED'),
    (2, 1, 1, '2026-05-01', 2, 1, 'RESERVED'),
    (3, 1, 1, '2026-05-01', 3, 1, 'RESERVED'),
    (4, 1, 1, '2026-05-01', 4, 1, 'RESERVED'),
    (5, 1, 1, '2026-05-02', 1, 1, 'RESERVED'),
    (6, 1, 1, '2026-05-02', 2, 1, 'RESERVED'),
    (7, 1, 1, '2026-05-02', 3, 1, 'RESERVED'),
    (8, 1, 1, '2026-05-02', 4, 1, 'RESERVED'),
    (9, 1, 1, '2026-05-03', 1, 1, 'RESERVED'),
    (10, 1, 1, '2026-05-03', 2, 1, 'RESERVED'),
    -- 테마 2: 9건
    (11, 1, 2, '2026-05-01', 1, 1, 'RESERVED'),
    (12, 1, 2, '2026-05-01', 2, 1, 'RESERVED'),
    (13, 1, 2, '2026-05-01', 3, 1, 'RESERVED'),
    (14, 1, 2, '2026-05-01', 4, 1, 'RESERVED'),
    (15, 1, 2, '2026-05-02', 1, 1, 'RESERVED'),
    (16, 1, 2, '2026-05-02', 2, 1, 'RESERVED'),
    (17, 1, 2, '2026-05-02', 3, 1, 'RESERVED'),
    (18, 1, 2, '2026-05-02', 4, 1, 'RESERVED'),
    (19, 1, 2, '2026-05-03', 1, 1, 'RESERVED'),
    -- 테마 3: 8건
    (20, 1, 3, '2026-05-01', 1, 1, 'RESERVED'),
    (21, 1, 3, '2026-05-01', 2, 1, 'RESERVED'),
    (22, 1, 3, '2026-05-01', 3, 1, 'RESERVED'),
    (23, 1, 3, '2026-05-01', 4, 1, 'RESERVED'),
    (24, 1, 3, '2026-05-02', 1, 1, 'RESERVED'),
    (25, 1, 3, '2026-05-02', 2, 1, 'RESERVED'),
    (26, 1, 3, '2026-05-02', 3, 1, 'RESERVED'),
    (27, 1, 3, '2026-05-02', 4, 1, 'RESERVED'),
    -- 테마 4: 7건
    (28, 1, 4, '2026-05-01', 1, 1, 'RESERVED'),
    (29, 1, 4, '2026-05-01', 2, 1, 'RESERVED'),
    (30, 1, 4, '2026-05-01', 3, 1, 'RESERVED'),
    (31, 1, 4, '2026-05-01', 4, 1, 'RESERVED'),
    (32, 1, 4, '2026-05-02', 1, 1, 'RESERVED'),
    (33, 1, 4, '2026-05-02', 2, 1, 'RESERVED'),
    (34, 1, 4, '2026-05-02', 3, 1, 'RESERVED'),
    -- 테마 5: 6건
    (35, 1, 5, '2026-05-01', 1, 1, 'RESERVED'),
    (36, 1, 5, '2026-05-01', 2, 1, 'RESERVED'),
    (37, 1, 5, '2026-05-01', 3, 1, 'RESERVED'),
    (38, 1, 5, '2026-05-01', 4, 1, 'RESERVED'),
    (39, 1, 5, '2026-05-02', 1, 1, 'RESERVED'),
    (40, 1, 5, '2026-05-02', 2, 1, 'RESERVED'),
    -- 테마 6: 5건
    (41, 1, 6, '2026-05-01', 1, 1, 'RESERVED'),
    (42, 1, 6, '2026-05-01', 2, 1, 'RESERVED'),
    (43, 1, 6, '2026-05-01', 3, 1, 'RESERVED'),
    (44, 1, 6, '2026-05-01', 4, 1, 'RESERVED'),
    (45, 1, 6, '2026-05-02', 1, 1, 'RESERVED'),
    -- 테마 7: 4건
    (46, 1, 7, '2026-05-03', 1, 1, 'RESERVED'),
    (47, 1, 7, '2026-05-03', 2, 1, 'RESERVED'),
    (48, 1, 7, '2026-05-03', 3, 1, 'RESERVED'),
    (49, 1, 7, '2026-05-03', 4, 1, 'RESERVED'),
    -- 테마 8: 4건
    (50, 1, 8, '2026-05-04', 1, 1, 'RESERVED'),
    (51, 1, 8, '2026-05-04', 2, 1, 'RESERVED'),
    (52, 1, 8, '2026-05-04', 3, 1, 'RESERVED'),
    (53, 1, 8, '2026-05-04', 4, 1, 'RESERVED'),
    -- 테마 9: 3건
    (54, 1, 9, '2026-05-05', 1, 1, 'RESERVED'),
    (55, 1, 9, '2026-05-05', 2, 1, 'RESERVED'),
    (56, 1, 9, '2026-05-05', 3, 1, 'RESERVED'),
    -- 테마 10: 3건
    (57, 1, 10, '2026-05-05', 2, 1, 'RESERVED'),
    (58, 1, 10, '2026-05-05', 3, 1, 'RESERVED'),
    (59, 1, 10, '2026-05-05', 4, 1, 'RESERVED'),
    -- 테마 11: 2건
    (60, 1, 11, '2026-05-06', 1, 1, 'RESERVED'),
    (61, 1, 11, '2026-05-06', 2, 1, 'RESERVED'),
    -- 테마 12: 2건
    (62, 1, 12, '2026-05-06', 3, 1, 'RESERVED'),
    (63, 1, 12, '2026-05-06', 4, 1, 'RESERVED'),
    -- 테마 13: 2건
    (64, 1, 13, '2026-05-04', 1, 1, 'RESERVED'),
    (65, 1, 13, '2026-05-04', 2, 1, 'RESERVED'),
    -- 테마 14: 1건
    (66, 1, 14, '2026-05-03', 3, 1, 'RESERVED'),
    -- 테마 15: 1건
    (67, 1, 15, '2026-05-03', 4, 1, 'RESERVED'),
    -- 기간 외 예약 (from=today.minusDays(7)=2026-04-29 보다 이전) - 인기 테마 집계에서 제외되어야 함
    -- 테마 13~15에 기간 외 예약을 많이 추가해 기간 필터 동작 검증
    (68, 1, 13, '2026-04-25', 1, 1, 'RESERVED'),
    (69, 1, 13, '2026-04-25', 2, 1, 'RESERVED'),
    (70, 1, 13, '2026-04-25', 3, 1, 'RESERVED'),
    (71, 1, 13, '2026-04-25', 4, 1, 'RESERVED'),
    (72, 1, 13, '2026-04-26', 1, 1, 'RESERVED'),
    (73, 1, 13, '2026-04-26', 2, 1, 'RESERVED'),
    (74, 1, 14, '2026-04-25', 1, 1, 'RESERVED'),
    (75, 1, 14, '2026-04-25', 2, 1, 'RESERVED'),
    (76, 1, 14, '2026-04-25', 3, 1, 'RESERVED'),
    (77, 1, 14, '2026-04-25', 4, 1, 'RESERVED'),
    (78, 1, 15, '2026-04-26', 1, 1, 'RESERVED'),
    (79, 1, 15, '2026-04-26', 2, 1, 'RESERVED'),
    (80, 1, 15, '2026-04-26', 3, 1, 'RESERVED');

ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 100;
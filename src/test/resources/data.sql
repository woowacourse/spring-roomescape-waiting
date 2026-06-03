-- 인기 테마 랭킹 테스트용 시드 데이터.
-- 날짜는 실행 시점 기준 상대값(DATEADD)으로 두어, 항상 인기 테마 집계 윈도우(today-7 ~ today-1)와의 관계가 고정된다.
-- 테스트에서 @Transactional 롤백으로 격리하기 위해 ALTER TABLE(RESTART)은 두지 않는다(DDL은 암묵적 커밋을 유발).
INSERT INTO users (id, name, username, password, role)
VALUES (1, '예약자', 'reserver@test.com', '$2a$10$dummyhash', 'MEMBER');

INSERT INTO store (id, name)
VALUES (1, '강남점');

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

INSERT INTO reservation_time (id, start_at)
VALUES (1, '10:00:00'),
       (2, '11:00:00'),
       (3, '12:00:00'),
       (4, '13:00:00');

-- 테마별 예약 수: 1위(10) ~ 15위(1), 인기 테마 랭킹 테스트용
-- date는 DATEADD('DAY', -N, CURRENT_DATE): N=1~6이면 윈도우(today-7~today-1) 내, N>=8이면 윈도우 밖.
INSERT INTO reservation (id, user_id, theme_id, date, time_id, store_id, status)
VALUES
    -- 테마 1: 10건
    (1, 1, 1, DATEADD('DAY', -6, CURRENT_DATE), 1, 1, 'RESERVED'),
    (2, 1, 1, DATEADD('DAY', -6, CURRENT_DATE), 2, 1, 'RESERVED'),
    (3, 1, 1, DATEADD('DAY', -6, CURRENT_DATE), 3, 1, 'RESERVED'),
    (4, 1, 1, DATEADD('DAY', -6, CURRENT_DATE), 4, 1, 'RESERVED'),
    (5, 1, 1, DATEADD('DAY', -5, CURRENT_DATE), 1, 1, 'RESERVED'),
    (6, 1, 1, DATEADD('DAY', -5, CURRENT_DATE), 2, 1, 'RESERVED'),
    (7, 1, 1, DATEADD('DAY', -5, CURRENT_DATE), 3, 1, 'RESERVED'),
    (8, 1, 1, DATEADD('DAY', -5, CURRENT_DATE), 4, 1, 'RESERVED'),
    (9, 1, 1, DATEADD('DAY', -4, CURRENT_DATE), 1, 1, 'RESERVED'),
    (10, 1, 1, DATEADD('DAY', -4, CURRENT_DATE), 2, 1, 'RESERVED'),
    -- 테마 2: 9건
    (11, 1, 2, DATEADD('DAY', -6, CURRENT_DATE), 1, 1, 'RESERVED'),
    (12, 1, 2, DATEADD('DAY', -6, CURRENT_DATE), 2, 1, 'RESERVED'),
    (13, 1, 2, DATEADD('DAY', -6, CURRENT_DATE), 3, 1, 'RESERVED'),
    (14, 1, 2, DATEADD('DAY', -6, CURRENT_DATE), 4, 1, 'RESERVED'),
    (15, 1, 2, DATEADD('DAY', -5, CURRENT_DATE), 1, 1, 'RESERVED'),
    (16, 1, 2, DATEADD('DAY', -5, CURRENT_DATE), 2, 1, 'RESERVED'),
    (17, 1, 2, DATEADD('DAY', -5, CURRENT_DATE), 3, 1, 'RESERVED'),
    (18, 1, 2, DATEADD('DAY', -5, CURRENT_DATE), 4, 1, 'RESERVED'),
    (19, 1, 2, DATEADD('DAY', -4, CURRENT_DATE), 1, 1, 'RESERVED'),
    -- 테마 3: 8건
    (20, 1, 3, DATEADD('DAY', -6, CURRENT_DATE), 1, 1, 'RESERVED'),
    (21, 1, 3, DATEADD('DAY', -6, CURRENT_DATE), 2, 1, 'RESERVED'),
    (22, 1, 3, DATEADD('DAY', -6, CURRENT_DATE), 3, 1, 'RESERVED'),
    (23, 1, 3, DATEADD('DAY', -6, CURRENT_DATE), 4, 1, 'RESERVED'),
    (24, 1, 3, DATEADD('DAY', -5, CURRENT_DATE), 1, 1, 'RESERVED'),
    (25, 1, 3, DATEADD('DAY', -5, CURRENT_DATE), 2, 1, 'RESERVED'),
    (26, 1, 3, DATEADD('DAY', -5, CURRENT_DATE), 3, 1, 'RESERVED'),
    (27, 1, 3, DATEADD('DAY', -5, CURRENT_DATE), 4, 1, 'RESERVED'),
    -- 테마 4: 7건
    (28, 1, 4, DATEADD('DAY', -6, CURRENT_DATE), 1, 1, 'RESERVED'),
    (29, 1, 4, DATEADD('DAY', -6, CURRENT_DATE), 2, 1, 'RESERVED'),
    (30, 1, 4, DATEADD('DAY', -6, CURRENT_DATE), 3, 1, 'RESERVED'),
    (31, 1, 4, DATEADD('DAY', -6, CURRENT_DATE), 4, 1, 'RESERVED'),
    (32, 1, 4, DATEADD('DAY', -5, CURRENT_DATE), 1, 1, 'RESERVED'),
    (33, 1, 4, DATEADD('DAY', -5, CURRENT_DATE), 2, 1, 'RESERVED'),
    (34, 1, 4, DATEADD('DAY', -5, CURRENT_DATE), 3, 1, 'RESERVED'),
    -- 테마 5: 6건
    (35, 1, 5, DATEADD('DAY', -6, CURRENT_DATE), 1, 1, 'RESERVED'),
    (36, 1, 5, DATEADD('DAY', -6, CURRENT_DATE), 2, 1, 'RESERVED'),
    (37, 1, 5, DATEADD('DAY', -6, CURRENT_DATE), 3, 1, 'RESERVED'),
    (38, 1, 5, DATEADD('DAY', -6, CURRENT_DATE), 4, 1, 'RESERVED'),
    (39, 1, 5, DATEADD('DAY', -5, CURRENT_DATE), 1, 1, 'RESERVED'),
    (40, 1, 5, DATEADD('DAY', -5, CURRENT_DATE), 2, 1, 'RESERVED'),
    -- 테마 6: 5건
    (41, 1, 6, DATEADD('DAY', -6, CURRENT_DATE), 1, 1, 'RESERVED'),
    (42, 1, 6, DATEADD('DAY', -6, CURRENT_DATE), 2, 1, 'RESERVED'),
    (43, 1, 6, DATEADD('DAY', -6, CURRENT_DATE), 3, 1, 'RESERVED'),
    (44, 1, 6, DATEADD('DAY', -6, CURRENT_DATE), 4, 1, 'RESERVED'),
    (45, 1, 6, DATEADD('DAY', -5, CURRENT_DATE), 1, 1, 'RESERVED'),
    -- 테마 7: 4건
    (46, 1, 7, DATEADD('DAY', -4, CURRENT_DATE), 1, 1, 'RESERVED'),
    (47, 1, 7, DATEADD('DAY', -4, CURRENT_DATE), 2, 1, 'RESERVED'),
    (48, 1, 7, DATEADD('DAY', -4, CURRENT_DATE), 3, 1, 'RESERVED'),
    (49, 1, 7, DATEADD('DAY', -4, CURRENT_DATE), 4, 1, 'RESERVED'),
    -- 테마 8: 4건
    (50, 1, 8, DATEADD('DAY', -3, CURRENT_DATE), 1, 1, 'RESERVED'),
    (51, 1, 8, DATEADD('DAY', -3, CURRENT_DATE), 2, 1, 'RESERVED'),
    (52, 1, 8, DATEADD('DAY', -3, CURRENT_DATE), 3, 1, 'RESERVED'),
    (53, 1, 8, DATEADD('DAY', -3, CURRENT_DATE), 4, 1, 'RESERVED'),
    -- 테마 9: 3건
    (54, 1, 9, DATEADD('DAY', -2, CURRENT_DATE), 1, 1, 'RESERVED'),
    (55, 1, 9, DATEADD('DAY', -2, CURRENT_DATE), 2, 1, 'RESERVED'),
    (56, 1, 9, DATEADD('DAY', -2, CURRENT_DATE), 3, 1, 'RESERVED'),
    -- 테마 10: 3건
    (57, 1, 10, DATEADD('DAY', -2, CURRENT_DATE), 2, 1, 'RESERVED'),
    (58, 1, 10, DATEADD('DAY', -2, CURRENT_DATE), 3, 1, 'RESERVED'),
    (59, 1, 10, DATEADD('DAY', -2, CURRENT_DATE), 4, 1, 'RESERVED'),
    -- 테마 11: 2건
    (60, 1, 11, DATEADD('DAY', -1, CURRENT_DATE), 1, 1, 'RESERVED'),
    (61, 1, 11, DATEADD('DAY', -1, CURRENT_DATE), 2, 1, 'RESERVED'),
    -- 테마 12: 2건
    (62, 1, 12, DATEADD('DAY', -1, CURRENT_DATE), 3, 1, 'RESERVED'),
    (63, 1, 12, DATEADD('DAY', -1, CURRENT_DATE), 4, 1, 'RESERVED'),
    -- 테마 13: 2건
    (64, 1, 13, DATEADD('DAY', -3, CURRENT_DATE), 1, 1, 'RESERVED'),
    (65, 1, 13, DATEADD('DAY', -3, CURRENT_DATE), 2, 1, 'RESERVED'),
    -- 테마 14: 1건
    (66, 1, 14, DATEADD('DAY', -4, CURRENT_DATE), 3, 1, 'RESERVED'),
    -- 테마 15: 1건
    (67, 1, 15, DATEADD('DAY', -4, CURRENT_DATE), 4, 1, 'RESERVED'),
    -- 기간 외 예약 (today-7 보다 이전) - 인기 테마 집계에서 제외되어야 함
    -- 테마 13~15에 기간 외 예약을 많이 추가해 기간 필터 동작 검증
    (68, 1, 13, DATEADD('DAY', -12, CURRENT_DATE), 1, 1, 'RESERVED'),
    (69, 1, 13, DATEADD('DAY', -12, CURRENT_DATE), 2, 1, 'RESERVED'),
    (70, 1, 13, DATEADD('DAY', -12, CURRENT_DATE), 3, 1, 'RESERVED'),
    (71, 1, 13, DATEADD('DAY', -12, CURRENT_DATE), 4, 1, 'RESERVED'),
    (72, 1, 13, DATEADD('DAY', -11, CURRENT_DATE), 1, 1, 'RESERVED'),
    (73, 1, 13, DATEADD('DAY', -11, CURRENT_DATE), 2, 1, 'RESERVED'),
    (74, 1, 14, DATEADD('DAY', -12, CURRENT_DATE), 1, 1, 'RESERVED'),
    (75, 1, 14, DATEADD('DAY', -12, CURRENT_DATE), 2, 1, 'RESERVED'),
    (76, 1, 14, DATEADD('DAY', -12, CURRENT_DATE), 3, 1, 'RESERVED'),
    (77, 1, 14, DATEADD('DAY', -12, CURRENT_DATE), 4, 1, 'RESERVED'),
    (78, 1, 15, DATEADD('DAY', -11, CURRENT_DATE), 1, 1, 'RESERVED'),
    (79, 1, 15, DATEADD('DAY', -11, CURRENT_DATE), 2, 1, 'RESERVED'),
    (80, 1, 15, DATEADD('DAY', -11, CURRENT_DATE), 3, 1, 'RESERVED');

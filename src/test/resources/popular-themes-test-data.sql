INSERT INTO reservation_time (id, start_at)
VALUES (1, '10:00:00'),
       (2, '11:20:00'),
       (3, '13:00:00'),
       (4, '14:20:00'),
       (5, '16:00:00'),
       (6, '17:20:00'),
       (7, '19:00:00'),
       (8, '20:20:00');

INSERT INTO theme (id, name, description, thumbnail_url, price)
VALUES (1, '잃어버린 시간의 방', '멈춰버린 시계탑에서 사라진 시간을 되찾는 미스터리 테마', 'https://example.com/images/time-room.jpg', 10000),
       (2, '심야 병동', '폐쇄된 병원에서 벌어지는 기묘한 사건을 추적하는 공포 테마', 'https://example.com/images/midnight-hospital.jpg', 10000),
       (3, '마법사의 서재', '고대 마법서에 숨겨진 주문을 찾아 봉인을 해제하는 판타지 테마', 'https://example.com/images/wizard-library.jpg', 10000),
       (4, '해적선의 보물', '저주받은 해적선 안에서 전설의 보물을 찾아 탈출하는 어드벤처 테마', 'https://example.com/images/pirate-ship.jpg', 10000),
       (5, '비밀 연구소', '정체불명의 실험체가 깨어난 연구소에서 탈출하는 SF 테마', 'https://example.com/images/secret-lab.jpg', 10000),
       (6, '탐정 사무소의 마지막 사건', '명탐정이 남긴 단서를 따라 범인을 찾아내는 추리 테마', 'https://example.com/images/detective-office.jpg', 10000),
       (7, '고대 유적의 저주', '사라진 탐험대를 찾아 고대 유적 깊숙이 들어가는 모험 테마', 'https://example.com/images/ancient-ruins.jpg', 10000),
       (8, '지하철 0호선', '존재하지 않는 노선에 갇힌 승객들의 비밀을 파헤치는 스릴러 테마', 'https://example.com/images/subway-zero.jpg', 10000),
       (9, '달빛 아래의 저택', '오래된 저택에서 반복되는 기묘한 밤의 진실을 밝히는 미스터리 테마', 'https://example.com/images/moonlight-mansion.jpg', 10000),
       (10, '우주 정거장 알파', '고장 난 우주 정거장에서 산소가 떨어지기 전에 탈출하는 SF 테마', 'https://example.com/images/space-alpha.jpg', 10000),
       (11, '마녀의 숲', '깊은 숲속 마녀의 오두막에서 숨겨진 계약서를 찾는 판타지 테마', 'https://example.com/images/witch-forest.jpg', 10000),
       (12, '사라진 열차', '한밤중 흔적 없이 사라진 열차의 비밀을 추적하는 추리 테마', 'https://example.com/images/missing-train.jpg', 10000);

DROP TABLE IF EXISTS reservation_seed;

CREATE TABLE reservation_seed
(
    id               BIGINT,
    customer_name    VARCHAR(10),
    reservation_date DATE,
    time_id          BIGINT,
    theme_id         BIGINT
);

INSERT INTO reservation_seed (id, customer_name, reservation_date, time_id, theme_id)
VALUES
    -- 오늘 기준 7일 전
    (1, '김민준', DATEADD('DAY', -7, CURRENT_DATE), 1, 1),
    (2, '이서연', DATEADD('DAY', -7, CURRENT_DATE), 2, 1),
    (3, '박도윤', DATEADD('DAY', -7, CURRENT_DATE), 3, 2),
    (4, '최하윤', DATEADD('DAY', -7, CURRENT_DATE), 4, 3),

    -- 오늘 기준 6일 전
    (5, '정지호', DATEADD('DAY', -6, CURRENT_DATE), 1, 1),
    (6, '강지민', DATEADD('DAY', -6, CURRENT_DATE), 2, 2),
    (7, '조서아', DATEADD('DAY', -6, CURRENT_DATE), 3, 2),
    (8, '윤하준', DATEADD('DAY', -6, CURRENT_DATE), 4, 4),
    (9, '임서준', DATEADD('DAY', -6, CURRENT_DATE), 5, 5),

    -- 오늘 기준 5일 전
    (10, '한유진', DATEADD('DAY', -5, CURRENT_DATE), 1, 1),
    (11, '오시우', DATEADD('DAY', -5, CURRENT_DATE), 2, 3),
    (12, '신채원', DATEADD('DAY', -5, CURRENT_DATE), 3, 3),
    (13, '서지안', DATEADD('DAY', -5, CURRENT_DATE), 4, 4),

    -- 오늘 기준 4일 전
    (14, '권도현', DATEADD('DAY', -4, CURRENT_DATE), 1, 1),
    (15, '황예준', DATEADD('DAY', -4, CURRENT_DATE), 2, 2),
    (16, '안수빈', DATEADD('DAY', -4, CURRENT_DATE), 3, 4),
    (17, '송민서', DATEADD('DAY', -4, CURRENT_DATE), 4, 5),
    (18, '류지우', DATEADD('DAY', -4, CURRENT_DATE), 5, 6),

    -- 오늘 기준 3일 전
    (19, '장서윤', DATEADD('DAY', -3, CURRENT_DATE), 1, 1),
    (20, '백현우', DATEADD('DAY', -3, CURRENT_DATE), 2, 2),
    (21, '남지후', DATEADD('DAY', -3, CURRENT_DATE), 3, 3),
    (22, '문하린', DATEADD('DAY', -3, CURRENT_DATE), 4, 6),

    -- 오늘 기준 2일 전
    (23, '유준서', DATEADD('DAY', -2, CURRENT_DATE), 1, 4),
    (24, '배아린', DATEADD('DAY', -2, CURRENT_DATE), 2, 5),
    (25, '홍시온', DATEADD('DAY', -2, CURRENT_DATE), 3, 6),
    (26, '진서우', DATEADD('DAY', -2, CURRENT_DATE), 4, 7),

    -- 오늘 기준 1일 전
    (27, '고은우', DATEADD('DAY', -1, CURRENT_DATE), 1, 7),
    (28, '민채린', DATEADD('DAY', -1, CURRENT_DATE), 2, 8),
    (29, '차윤재', DATEADD('DAY', -1, CURRENT_DATE), 3, 9),
    (30, '나예린', DATEADD('DAY', -1, CURRENT_DATE), 4, 10);


INSERT INTO reservation_seed (id, customer_name, reservation_date, time_id, theme_id)
VALUES (31, '테스트1', DATEADD('DAY', -13, CURRENT_DATE), 1, 12),
       (32, '테스트2', DATEADD('DAY', -13, CURRENT_DATE), 2, 12),
       (33, '테스트3', DATEADD('DAY', -12, CURRENT_DATE), 1, 12),
       (34, '테스트4', DATEADD('DAY', -12, CURRENT_DATE), 2, 12),
       (35, '테스트5', DATEADD('DAY', -11, CURRENT_DATE), 1, 12),
       (36, '테스트6', DATEADD('DAY', -11, CURRENT_DATE), 2, 12),
       (37, '테스트7', DATEADD('DAY', -10, CURRENT_DATE), 1, 12),
       (38, '테스트8', DATEADD('DAY', -10, CURRENT_DATE), 2, 12),
       (39, '테스트9', DATEADD('DAY', -9, CURRENT_DATE), 1, 12),
       (40, '테스트10', DATEADD('DAY', -9, CURRENT_DATE), 2, 12);

INSERT INTO reservation_slot (id, reservation_date, time_id, theme_id)
SELECT id, reservation_date, time_id, theme_id
FROM reservation_seed;

INSERT INTO reservation (id, customer_name, customer_email, slot_id, status)
SELECT id, customer_name, CONCAT('customer', id, '@example.com'), id, 'CONFIRMED'
FROM reservation_seed;

DROP TABLE reservation_seed;

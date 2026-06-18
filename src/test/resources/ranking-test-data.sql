INSERT INTO reservation_time (id, start_at)
VALUES (1, '10:00'),
       (2, '11:00'),
       (3, '12:00'),
       (4, '13:00'),
       (5, '14:00'),
       (6, '15:00'),
       (7, '16:00'),
       (8, '17:00'),
       (9, '18:00'),
       (10, '19:00');

INSERT INTO theme (id, name, description, thumbnail_url)
VALUES (1, '잃어버린 왕국', '사라진 고대 왕국의 비밀을 추적하는 모험 테마', 'https://example.com/images/lost-kingdom.jpg'),
       (2, '심야의 연구소', '한밤중 폐쇄된 연구소에서 탈출 단서를 찾는 스릴러 테마', 'https://example.com/images/midnight-lab.jpg'),
       (3, '해적선의 저주', '저주받은 해적선에서 보물을 찾아 탈출하는 테마', 'https://example.com/images/pirate-curse.jpg'),
       (4, '시간 여행자', '흩어진 시간의 조각을 맞춰 현재로 돌아오는 SF 테마', 'https://example.com/images/time-traveler.jpg'),
       (5, '마법사의 서재', '마법 주문과 숨겨진 장치를 풀어내는 판타지 테마', 'https://example.com/images/wizard-library.jpg'),
       (6, '사라진 탐정', '실종된 탐정이 남긴 단서를 따라 사건을 해결하는 추리 테마', 'https://example.com/images/missing-detective.jpg'),
       (7, '지하 벙커', '폐쇄된 지하 벙커의 보안 시스템을 해제하는 생존 테마', 'https://example.com/images/underground-bunker.jpg'),
       (8, '고대 신전', '신전 깊은 곳의 퍼즐을 풀고 봉인을 해제하는 모험 테마', 'https://example.com/images/ancient-temple.jpg'),
       (9, '유령 호텔', '기묘한 호텔 방마다 숨은 이야기를 밝혀내는 공포 테마', 'https://example.com/images/ghost-hotel.jpg'),
       (10, '우주 정거장', '고장 난 우주 정거장을 복구하고 귀환하는 SF 테마', 'https://example.com/images/space-station.jpg'),
       (11, '비밀 카지노', '비밀 카지노의 금고 암호를 찾아내는 잠입 테마', 'https://example.com/images/secret-casino.jpg'),
       (12, '눈보라 산장', '눈보라에 갇힌 산장에서 범인을 찾아내는 추리 테마', 'https://example.com/images/snow-cabin.jpg'),
       (13, '인형의 집', '낡은 인형의 집에 숨겨진 진실을 발견하는 미스터리 테마', 'https://example.com/images/doll-house.jpg'),
       (14, '기억의 방', '잃어버린 기억을 되찾기 위해 단서를 연결하는 감성 테마', 'https://example.com/images/memory-room.jpg'),
       (15, '네온 시티', '미래 도시의 보안망을 돌파하는 사이버펑크 테마', 'https://example.com/images/neon-city.jpg');

-- member id 1~55 (테마별 예약자)
INSERT INTO member (id, name)
VALUES (1, '왕국_01'), (2, '왕국_02'), (3, '왕국_03'), (4, '왕국_04'), (5, '왕국_05'),
       (6, '왕국_06'), (7, '왕국_07'), (8, '왕국_08'), (9, '왕국_09'), (10, '왕국_10'),
       (11, '연구소_01'), (12, '연구소_02'), (13, '연구소_03'), (14, '연구소_04'), (15, '연구소_05'),
       (16, '연구소_06'), (17, '연구소_07'), (18, '연구소_08'), (19, '연구소_09'),
       (20, '해적_01'), (21, '해적_02'), (22, '해적_03'), (23, '해적_04'), (24, '해적_05'),
       (25, '해적_06'), (26, '해적_07'), (27, '해적_08'),
       (28, '여행자_01'), (29, '여행자_02'), (30, '여행자_03'), (31, '여행자_04'), (32, '여행자_05'),
       (33, '여행자_06'), (34, '여행자_07'),
       (35, '마법사_01'), (36, '마법사_02'), (37, '마법사_03'), (38, '마법사_04'), (39, '마법사_05'),
       (40, '마법사_06'),
       (41, '탐정_01'), (42, '탐정_02'), (43, '탐정_03'), (44, '탐정_04'), (45, '탐정_05'),
       (46, '벙커_01'), (47, '벙커_02'), (48, '벙커_03'), (49, '벙커_04'),
       (50, '신전_01'), (51, '신전_02'), (52, '신전_03'),
       (53, '호텔_01'), (54, '호텔_02'),
       (55, '우주_01');

-- [Theme 1: 잃어버린 왕국] - 총 10건
INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
VALUES (1, '2026-05-01', 1, 1),
       (2, '2026-05-02', 2, 1),
       (3, '2026-05-03', 3, 1),
       (4, '2026-05-04', 4, 1),
       (5, '2026-05-05', 5, 1),
       (6, '2026-05-06', 6, 1),
       (7, '2026-05-07', 7, 1),
       (8, '2026-05-07', 8, 1),
       (9, '2026-05-07', 9, 1),
       (10, '2026-05-07', 10, 1);

-- [Theme 2: 심야의 연구소] - 총 9건
INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
VALUES (11, '2026-05-01', 1, 2),
       (12, '2026-05-02', 2, 2),
       (13, '2026-05-03', 3, 2),
       (14, '2026-05-04', 4, 2),
       (15, '2026-05-05', 5, 2),
       (16, '2026-05-06', 6, 2),
       (17, '2026-05-07', 7, 2),
       (18, '2026-05-07', 8, 2),
       (19, '2026-05-07', 9, 2);

-- [Theme 3: 해적선의 저주] - 총 8건
INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
VALUES (20, '2026-05-01', 1, 3),
       (21, '2026-05-02', 2, 3),
       (22, '2026-05-03', 3, 3),
       (23, '2026-05-04', 4, 3),
       (24, '2026-05-05', 5, 3),
       (25, '2026-05-06', 6, 3),
       (26, '2026-05-07', 7, 3),
       (27, '2026-05-07', 8, 3);

-- [Theme 4: 시간 여행자] - 총 7건
INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
VALUES (28, '2026-05-01', 1, 4),
       (29, '2026-05-02', 2, 4),
       (30, '2026-05-03', 3, 4),
       (31, '2026-05-04', 4, 4),
       (32, '2026-05-05', 5, 4),
       (33, '2026-05-06', 6, 4),
       (34, '2026-05-07', 7, 4);

-- [Theme 5: 마법사의 서재] - 총 6건
INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
VALUES (35, '2026-05-01', 1, 5),
       (36, '2026-05-02', 2, 5),
       (37, '2026-05-03', 3, 5),
       (38, '2026-05-04', 4, 5),
       (39, '2026-05-05', 5, 5),
       (40, '2026-05-06', 6, 5);

-- [Theme 6: 사라진 탐정] - 총 5건
INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
VALUES (41, '2026-05-01', 1, 6),
       (42, '2026-05-02', 2, 6),
       (43, '2026-05-03', 3, 6),
       (44, '2026-05-04', 4, 6),
       (45, '2026-05-05', 5, 6);

-- [Theme 7: 지하 벙커] - 총 4건
INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
VALUES (46, '2026-05-01', 1, 7),
       (47, '2026-05-02', 2, 7),
       (48, '2026-05-03', 3, 7),
       (49, '2026-05-04', 4, 7);

-- [Theme 8: 고대 신전] - 총 3건
INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
VALUES (50, '2026-05-01', 1, 8),
       (51, '2026-05-02', 2, 8),
       (52, '2026-05-03', 3, 8);

-- [Theme 9: 유령 호텔] - 총 2건
INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
VALUES (53, '2026-05-01', 1, 9),
       (54, '2026-05-02', 2, 9);

-- [Theme 10: 우주 정거장] - 총 1건
INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
VALUES (55, '2026-05-01', 1, 10);

-- [Theme 11 ~ 15]: 예약 없음 (LEFT JOIN 테스트용)

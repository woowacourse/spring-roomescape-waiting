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

-- [Theme 1: 잃어버린 왕국] - 총 10건
INSERT INTO reservation (name, reservation_date, time_id, theme_id)
VALUES ('왕국_01', '2026-05-01', 1, 1),
       ('왕국_02', '2026-05-02', 2, 1),
       ('왕국_03', '2026-05-03', 3, 1),
       ('왕국_04', '2026-05-04', 4, 1),
       ('왕국_05', '2026-05-05', 5, 1),
       ('왕국_06', '2026-05-06', 6, 1),
       ('왕국_07', '2026-05-07', 7, 1),
       ('왕국_08', '2026-05-07', 8, 1),
       ('왕국_09', '2026-05-07', 9, 1),
       ('왕국_10', '2026-05-07', 10, 1);

-- [Theme 2: 심야의 연구소] - 총 9건
INSERT INTO reservation (name, reservation_date, time_id, theme_id)
VALUES ('연구소_01', '2026-05-01', 1, 2),
       ('연구소_02', '2026-05-02', 2, 2),
       ('연구소_03', '2026-05-03', 3, 2),
       ('연구소_04', '2026-05-04', 4, 2),
       ('연구소_05', '2026-05-05', 5, 2),
       ('연구소_06', '2026-05-06', 6, 2),
       ('연구소_07', '2026-05-07', 7, 2),
       ('연구소_08', '2026-05-07', 8, 2),
       ('연구소_09', '2026-05-07', 9, 2);

-- [Theme 3: 해적선의 저주] - 총 8건
INSERT INTO reservation (name, reservation_date, time_id, theme_id)
VALUES ('해적_01', '2026-05-01', 1, 3),
       ('해적_02', '2026-05-02', 2, 3),
       ('해적_03', '2026-05-03', 3, 3),
       ('해적_04', '2026-05-04', 4, 3),
       ('해적_05', '2026-05-05', 5, 3),
       ('해적_06', '2026-05-06', 6, 3),
       ('해적_07', '2026-05-07', 7, 3),
       ('해적_08', '2026-05-07', 8, 3);

-- [Theme 4: 시간 여행자] - 총 7건
INSERT INTO reservation (name, reservation_date, time_id, theme_id)
VALUES ('여행자_01', '2026-05-01', 1, 4),
       ('여행자_02', '2026-05-02', 2, 4),
       ('여행자_03', '2026-05-03', 3, 4),
       ('여행자_04', '2026-05-04', 4, 4),
       ('여행자_05', '2026-05-05', 5, 4),
       ('여행자_06', '2026-05-06', 6, 4),
       ('여행자_07', '2026-05-07', 7, 4);

-- [Theme 5: 마법사의 서재] - 총 6건
INSERT INTO reservation (name, reservation_date, time_id, theme_id)
VALUES ('마법사_01', '2026-05-01', 1, 5),
       ('마법사_02', '2026-05-02', 2, 5),
       ('마법사_03', '2026-05-03', 3, 5),
       ('마법사_04', '2026-05-04', 4, 5),
       ('마법사_05', '2026-05-05', 5, 5),
       ('마법사_06', '2026-05-06', 6, 5);

-- [Theme 6: 사라진 탐정] - 총 5건
INSERT INTO reservation (name, reservation_date, time_id, theme_id)
VALUES ('탐정_01', '2026-05-01', 1, 6),
       ('탐정_02', '2026-05-02', 2, 6),
       ('탐정_03', '2026-05-03', 3, 6),
       ('탐정_04', '2026-05-04', 4, 6),
       ('탐정_05', '2026-05-05', 5, 6);

-- [Theme 7: 지하 벙커] - 총 4건
INSERT INTO reservation (name, reservation_date, time_id, theme_id)
VALUES ('벙커_01', '2026-05-01', 1, 7),
       ('벙커_02', '2026-05-02', 2, 7),
       ('벙커_03', '2026-05-03', 3, 7),
       ('벙커_04', '2026-05-04', 4, 7);

-- [Theme 8: 고대 신전] - 총 3건
INSERT INTO reservation (name, reservation_date, time_id, theme_id)
VALUES ('신전_01', '2026-05-01', 1, 8),
       ('신전_02', '2026-05-02', 2, 8),
       ('신전_03', '2026-05-03', 3, 8);

-- [Theme 9: 유령 호텔] - 총 2건
INSERT INTO reservation (name, reservation_date, time_id, theme_id)
VALUES ('호텔_01', '2026-05-01', 1, 9),
       ('호텔_02', '2026-05-02', 2, 9);

-- [Theme 10: 우주 정거장] - 총 1건
INSERT INTO reservation (name, reservation_date, time_id, theme_id)
VALUES ('우주_01', '2026-05-01', 1, 10);

-- [Theme 11 ~ 15]: 예약 없음 (LEFT JOIN 테스트용)

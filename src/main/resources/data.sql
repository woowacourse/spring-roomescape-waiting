-- member
INSERT INTO member (name)
VALUES ('예약자'),
       ('대기자1'),
       ('대기자2'),
       ('대기자3'),
       ('대기자4'),
       ('대기자5'),
       ('대기자6'),
       ('대기자7'),
       ('브라운');

-- reservation_time
INSERT INTO reservation_time (start_at)
VALUES ('10:00:00'),
       ('11:00:00'),
       ('12:00:00'),
       ('13:00:00'),
       ('14:00:00'),
       ('15:00:00'),
       ('16:00:00'),
       ('17:00:00'),
       ('18:00:00');

-- theme
INSERT INTO theme (name, description, thumbnail)
VALUES ('공포의 저택', '오래된 저택에서 탈출하세요', '/images/themes/haunted-house.jpg'),
       ('사라진 연구소', '비밀 연구소의 진실을 밝혀내세요', '/images/themes/lost-lab.jpg'),
       ('시간 여행자', '시간의 틈에서 탈출하세요', '/images/themes/time-traveler.jpg'),
       ('감옥 탈출', '제한 시간 안에 감옥을 탈출하세요', '/images/themes/prison-break.jpg'),
       ('마법사의 방', '마법사의 숨겨진 방을 탐험하세요', '/images/themes/wizard-room.jpg'),
       ('좀비 바이러스', '바이러스가 퍼진 도시에서 살아남으세요', '/images/themes/zombie-virus.jpg'),
       ('해적의 보물', '해적선에 숨겨진 보물을 찾으세요', '/images/themes/pirate-treasure.jpg'),
       ('스파이 미션', '비밀 요원이 되어 임무를 완수하세요', '/images/themes/spy-mission.jpg'),
       ('우주 정거장', '고장난 우주 정거장에서 탈출하세요', '/images/themes/space-station.jpg'),
       ('고대 유적', '고대 유적의 수수께끼를 풀어보세요', '/images/themes/ancient-ruins.jpg'),
       ('미스터리 호텔', '호텔에서 벌어진 사건을 해결하세요', '/images/themes/mystery-hotel.jpg'),
       ('지하 벙커', '폐쇄된 지하 벙커에서 탈출하세요', '/images/themes/bunker.jpg');

-- reservation (member_id=1: 예약자)
-- theme_id 1: 12건
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (1, '2026-06-10', 1, 1),
       (1, '2026-06-12', 2, 1),
       (1, '2026-06-12', 3, 1),
       (1, '2026-06-12', 4, 1),
       (1, '2026-06-12', 5, 1),
       (1, '2026-06-11', 1, 1),
       (1, '2026-06-11', 2, 1),
       (1, '2026-06-10', 3, 1),
       (1, '2026-06-10', 4, 1),
       (1, '2026-06-10', 5, 1),
       (1, '2026-06-10', 6, 1),
       (1, '2026-06-09', 7, 1);

-- theme_id 2: 9건
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (1, '2026-05-12', 1, 2),
       (1, '2026-05-12', 2, 2),
       (1, '2026-05-12', 3, 2),
       (1, '2026-05-12', 4, 2),
       (1, '2026-05-12', 5, 2),
       (1, '2026-05-12', 6, 2),
       (1, '2026-05-12', 7, 2),
       (1, '2026-05-12', 8, 2),
       (1, '2026-05-12', 9, 2);

-- theme_id 3: 9건
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (1, '2026-05-09', 1, 3),
       (1, '2026-05-09', 2, 3),
       (1, '2026-05-09', 3, 3),
       (1, '2026-05-09', 4, 3),
       (1, '2026-05-09', 5, 3),
       (1, '2026-05-09', 6, 3),
       (1, '2026-05-09', 7, 3),
       (1, '2026-04-09', 8, 3),
       (1, '2026-04-09', 9, 3);

-- theme_id 4: 8건
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (1, '2026-05-08', 1, 4),
       (1, '2026-05-08', 2, 4),
       (1, '2026-05-08', 3, 4),
       (1, '2026-05-08', 4, 4),
       (1, '2026-05-08', 5, 4),
       (1, '2026-05-08', 6, 4),
       (1, '2026-05-08', 7, 4),
       (1, '2026-05-08', 8, 4);

-- theme_id 5: 7건
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (1, '2026-05-04', 1, 5),
       (1, '2026-05-04', 2, 5),
       (1, '2026-05-04', 3, 5),
       (1, '2026-05-04', 4, 5),
       (1, '2026-05-04', 5, 5),
       (1, '2026-05-04', 6, 5),
       (1, '2026-05-04', 7, 5);

-- theme_id 6: 6건
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (1, '2026-05-03', 1, 6),
       (1, '2026-05-03', 2, 6),
       (1, '2026-05-03', 3, 6),
       (1, '2026-05-03', 4, 6),
       (1, '2026-05-03', 5, 6),
       (1, '2026-05-03', 6, 6);

-- theme_id 7: 5건
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (1, '2026-05-05', 1, 7),
       (1, '2026-05-04', 2, 7),
       (1, '2026-05-03', 3, 7),
       (1, '2026-05-02', 4, 7),
       (1, '2026-05-01', 5, 7);

-- theme_id 8: 4건
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (1, '2026-05-05', 1, 8),
       (1, '2026-05-04', 2, 8),
       (1, '2026-05-03', 3, 8),
       (1, '2026-05-02', 4, 8);

-- theme_id 9: 3건
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (1, '2026-05-05', 1, 9),
       (1, '2026-05-04', 2, 9),
       (1, '2026-05-03', 3, 9);

-- theme_id 10: 2건
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (1, '2026-05-05', 1, 10),
       (1, '2026-05-04', 2, 10);

-- theme_id 11: 1건
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (1, '2026-05-05', 1, 11);

-- 인기 테마 조회용: 2026-05-27 기준 최근 7일 이내 데이터
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (1, '2026-05-26', 1, 2),
       (1, '2026-05-26', 2, 2),
       (1, '2026-05-25', 3, 2),
       (1, '2026-05-24', 4, 2),
       (1, '2026-05-23', 5, 2),
       (1, '2026-05-26', 1, 3),
       (1, '2026-05-25', 2, 3),
       (1, '2026-05-24', 3, 3),
       (1, '2026-05-23', 4, 3),
       (1, '2026-05-26', 1, 5),
       (1, '2026-05-25', 2, 5),
       (1, '2026-05-24', 3, 5),
       (1, '2026-05-26', 1, 7),
       (1, '2026-05-25', 2, 7),
       (1, '2026-05-26', 1, 9);

-- theme_id 12: 최근 7일 밖 데이터라 인기 순위에 포함되면 안 되는 데이터
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (1, '2026-04-20', 1, 12),
       (1, '2026-04-20', 2, 12),
       (1, '2026-04-20', 3, 12),
       (1, '2026-04-20', 4, 12),
       (1, '2026-04-20', 5, 12),
       (1, '2026-04-20', 6, 12),
       (1, '2026-04-20', 7, 12),
       (1, '2026-04-20', 8, 12),
       (1, '2026-04-20', 9, 12);

-- reservation_waiting
-- theme_id 1, date 2026-06-10, time_id 1 → 예약자(id=1)가 예약한 슬롯에 대기
-- 같은 슬롯에 대기하므로 각각 다른 member_id 사용 (대기자1=2, 대기자2=3, 대기자3=4)
INSERT INTO reservation_waiting (member_id, created_at, reservation_date, time_id, theme_id)
VALUES (2, '2026-05-27 10:00:00', '2026-06-10', 1, 1),
       (3, '2026-05-27 11:00:00', '2026-06-10', 1, 1),
       (4, '2026-05-27 12:00:00', '2026-06-10', 1, 1);

-- theme_id 1, date 2026-06-11, time_id 1 → 대기자4=5, 대기자5=6
INSERT INTO reservation_waiting (member_id, created_at, reservation_date, time_id, theme_id)
VALUES (5, '2026-05-27 10:00:00', '2026-06-11', 1, 1),
       (6, '2026-05-27 11:00:00', '2026-06-11', 1, 1);

-- theme_id 2, date 2026-05-12, time_id 1 → 대기자6=7, 대기자7=8
INSERT INTO reservation_waiting (member_id, created_at, reservation_date, time_id, theme_id)
VALUES (7, '2026-05-27 10:00:00', '2026-05-12', 1, 2),
       (8, '2026-05-27 11:00:00', '2026-05-12', 1, 2);

-- 내 예약/대기 조회 테스트용 - 브라운(id=9)이 예약도 있고 대기도 있는 케이스
-- 브라운 예약 (theme_id 3, date 2026-06-15, time_id 1)
INSERT INTO reservation (member_id, `date`, time_id, theme_id)
VALUES (9, '2026-06-15', 1, 3);

-- 브라운 대기 (이미 예약자(id=1)가 예약한 슬롯 - theme_id 1, date 2026-06-10, time_id 1)
INSERT INTO reservation_waiting (member_id, created_at, reservation_date, time_id, theme_id)
VALUES (9, '2026-05-27 13:00:00', '2026-06-10', 1, 1);

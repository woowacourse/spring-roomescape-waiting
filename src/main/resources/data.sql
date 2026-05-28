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
       ('18:00:00'),
       ('19:00:00'),
       ('20:00:00'),
       ('21:00:00'),
       ('22:00:00');

-- theme (12 unique themes with high-stability thumbnails)
INSERT INTO theme (name, description, thumbnail)
VALUES ('공포의 저택', '오래된 저택에서 탈출하세요',
        'https://images.unsplash.com/photo-1508739773434-c26b3d09e071?q=80&w=400&auto=format&fit=crop'),
       ('사라진 연구소', '비밀 연구소의 진실을 밝혀내세요',
        'https://images.unsplash.com/photo-1532094349884-543bc11b234d?q=80&w=400&auto=format&fit=crop'),
       ('시간 여행자', '시간의 틈에서 탈출하세요',
        'https://images.unsplash.com/photo-1501139083538-0139583c060f?q=80&w=400&auto=format&fit=crop'),
       ('감옥 탈출', '제한 시간 안에 감옥을 탈출하세요',
        'https://images.unsplash.com/photo-1552508744-1696d4464960?q=80&w=400&auto=format&fit=crop'),
       ('마법사의 방', '마법사의 숨겨진 방을 탐험하세요', 'https://picsum.photos/seed/wizard-room/400/300'),
       ('좀비 바이러스', '바이러스가 퍼진 도시에서 살아남으세요',
        'https://images.unsplash.com/photo-1509248961158-e54f6934749c?q=80&w=400&auto=format&fit=crop'),
       ('해적의 보물', '해적선에 숨겨진 보물을 찾으세요',
        'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=400&auto=format&fit=crop'),
       ('스파이 미션', '비밀 요원이 되어 임무를 완수하세요',
        'https://images.unsplash.com/photo-1555664424-778a1e5e1b48?q=80&w=400&auto=format&fit=crop'),
       ('우주 정거장', '고장난 우주 정거장에서 탈출하세요',
        'https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=400&auto=format&fit=crop'),
       ('고대 유적', '고대 유적의 수수께끼를 풀어보세요',
        'https://images.unsplash.com/photo-1503177119275-0aa32b3a9368?q=80&w=400&auto=format&fit=crop'),
       ('미스터리 호텔', '호텔에서 벌어진 사건을 해결하세요',
        'https://images.unsplash.com/photo-1566073771259-6a8506099945?q=80&w=400&auto=format&fit=crop'),
       ('지하 벙커', '폐쇄된 지하 벙커에서 탈출하세요', 'https://picsum.photos/seed/bunker/400/300');

-- slot
-- 인기 테마 산정 기준: 2026-05-28 기준 최근 7일(2026-05-21 ~ 2026-05-27)에 해당하는 슬롯들
INSERT INTO slot (`date`, time_id, theme_id)
VALUES ('2026-05-27', 1, 1), -- id 1
       ('2026-05-27', 2, 1), -- id 2
       ('2026-05-26', 3, 1), -- id 3
       ('2026-05-26', 4, 1), -- id 4
       ('2026-05-25', 5, 1), -- id 5
       ('2026-05-25', 1, 1), -- id 6
       ('2026-05-24', 2, 1), -- id 7
       ('2026-05-23', 3, 1), -- id 8
       ('2026-05-22', 4, 1), -- id 9
       ('2026-05-21', 5, 1); -- id 10

INSERT INTO slot (`date`, time_id, theme_id)
VALUES ('2026-05-27', 1, 2), -- id 11
       ('2026-05-27', 2, 2), -- id 12
       ('2026-05-26', 3, 2), -- id 13
       ('2026-05-26', 4, 2), -- id 14
       ('2026-05-25', 5, 2), -- id 15
       ('2026-05-24', 6, 2), -- id 16
       ('2026-05-23', 7, 2), -- id 17
       ('2026-05-22', 1, 2); -- id 18

INSERT INTO slot (`date`, time_id, theme_id)
VALUES ('2026-05-27', 1, 3), -- id 19
       ('2026-05-26', 2, 3), -- id 20
       ('2026-05-25', 3, 3), -- id 21
       ('2026-05-24', 4, 3), -- id 22
       ('2026-05-23', 5, 3), -- id 23
       ('2026-05-22', 6, 3); -- id 24

INSERT INTO slot (`date`, time_id, theme_id)
VALUES ('2026-05-27', 1, 4), -- id 25
       ('2026-05-26', 2, 4), -- id 26
       ('2026-05-25', 3, 4), -- id 27
       ('2026-05-24', 4, 4); -- id 28

-- 미래 예약용 슬롯
INSERT INTO slot (`date`, time_id, theme_id)
VALUES ('2026-05-28', 10, 1), -- id 29
       ('2026-05-29', 11, 2), -- id 30
       ('2026-05-30', 12, 3); -- id 31

-- reservation
INSERT INTO reservation (name, slot_id)
VALUES ('브라운', 1),
       ('제임스', 2),
       ('코니', 3),
       ('샐리', 4),
       ('네오', 5),
       ('프로도', 6),
       ('무지', 7),
       ('어피치', 8),
       ('레오나드', 9),
       ('문', 10);

INSERT INTO reservation (name, slot_id)
VALUES ('포비', 11),
       ('크롱', 12),
       ('루피', 13),
       ('에디', 14),
       ('패티', 15),
       ('해리', 16),
       ('로디', 17),
       ('뽀로로', 18);

INSERT INTO reservation (name, slot_id)
VALUES ('타요', 19),
       ('로기', 20),
       ('라니', 21),
       ('가니', 22),
       ('시투', 23),
       ('하나', 24);

INSERT INTO reservation (name, slot_id)
VALUES ('토토로', 25),
       ('지브리', 26),
       ('카논', 27),
       ('치히로', 28);

-- 미래 예약 데이터
INSERT INTO reservation (name, slot_id)
VALUES ('브라운', 29),
       ('제임스', 30),
       ('코니', 31);

-- 대기 데이터
INSERT INTO waiting (created_at, slot_id, name)
VALUES ('2026-05-28 10:00:00', 29, '워니'),
       ('2026-05-28 11:00:00', 29, '구구'),
       ('2026-05-28 10:00:00', 30, '준');

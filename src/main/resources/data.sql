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
        'https://images.unsplash.com/photo-1581093458791-9f3c3250f8b9?q=80&w=400&auto=format&fit=crop'),
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
        'https://images.unsplash.com/photo-1524178232363-1fb28f74b0cd?q=80&w=400&auto=format&fit=crop'),
       ('우주 정거장', '고장난 우주 정거장에서 탈출하세요',
        'https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=400&auto=format&fit=crop'),
       ('고대 유적', '고대 유적의 수수께끼를 풀어보세요',
        'https://images.unsplash.com/photo-1503177119275-0aa32b3a9368?q=80&w=400&auto=format&fit=crop'),
       ('미스터리 호텔', '호텔에서 벌어진 사건을 해결하세요',
        'https://images.unsplash.com/photo-1566073771259-6a8506099945?q=80&w=400&auto=format&fit=crop'),
       ('지하 벙커', '폐쇄된 지하 벙커에서 탈출하세요', 'https://picsum.photos/seed/bunker/400/300');

-- reservation
-- 인기 테마 산정 기준: 2026-05-15 기준 최근 7일(05-08 ~ 05-14)
INSERT INTO reservation (name, `date`, time_id, theme_id)
VALUES ('브라운', '2026-05-14', 1, 1),
       ('제임스', '2026-05-14', 2, 1),
       ('코니', '2026-05-13', 3, 1),
       ('샐리', '2026-05-13', 4, 1),
       ('네오', '2026-05-12', 5, 1),
       ('프로도', '2026-05-12', 1, 1),
       ('무지', '2026-05-11', 2, 1),
       ('어피치', '2026-05-10', 3, 1),
       ('레오나드', '2026-05-09', 4, 1),
       ('문', '2026-05-08', 5, 1);

INSERT INTO reservation (name, `date`, time_id, theme_id)
VALUES ('포비', '2026-05-14', 1, 2),
       ('크롱', '2026-05-14', 2, 2),
       ('루피', '2026-05-13', 3, 2),
       ('에디', '2026-05-13', 4, 2),
       ('패티', '2026-05-12', 5, 2),
       ('해리', '2026-05-11', 6, 2),
       ('로디', '2026-05-10', 7, 2),
       ('뽀로로', '2026-05-09', 1, 2);

INSERT INTO reservation (name, `date`, time_id, theme_id)
VALUES ('타요', '2026-05-14', 1, 3),
       ('로기', '2026-05-13', 2, 3),
       ('라니', '2026-05-12', 3, 3),
       ('가니', '2026-05-11', 4, 3),
       ('시투', '2026-05-10', 5, 3),
       ('하나', '2026-05-09', 6, 3);

INSERT INTO reservation (name, `date`, time_id, theme_id)
VALUES ('토토로', '2026-05-14', 1, 4),
       ('지브리', '2026-05-13', 2, 4),
       ('카논', '2026-05-12', 3, 4),
       ('치히로', '2026-05-11', 4, 4);

-- 미래 예약 데이터
INSERT INTO reservation (name, `date`, time_id, theme_id)
VALUES ('브라운', '2026-05-15', 10, 1),
       ('제임스', '2026-05-16', 11, 2),
       ('코니', '2026-05-17', 12, 3);

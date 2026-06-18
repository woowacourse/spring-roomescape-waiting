INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('11:00'),
       ('13:00'),
       ('15:00'),
       ('16:00');

INSERT INTO reservation_date (play_day)
VALUES (CURRENT_DATE - 3),
       (CURRENT_DATE - 2),
       (CURRENT_DATE - 1),
       (CURRENT_DATE),
       (CURRENT_DATE + 1),
       (CURRENT_DATE + 2),
       (CURRENT_DATE + 3),
       (CURRENT_DATE + 4),
       (CURRENT_DATE + 5),
       (CURRENT_DATE + 6),
       (CURRENT_DATE + 7);

INSERT INTO theme (name, content, url)
VALUES ('공포', '오금이 저리는 공포입니다.', '/themes/scary'),
       ('스릴러', '액션이 가미된 스릴러입니다.', '/themes/thriller'),
       ('청춘물', '학교 배경인 테마 입니다.', '/themes/youth'),
       ('미스터리', '단서를 따라 진실을 밝히는 추리 테마입니다.', '/themes/mystery'),
       ('판타지', '마법과 전설이 살아있는 판타지 테마입니다.', '/themes/fantasy'),
       ('우주', '우주정거장을 배경으로 한 SF 테마입니다.', '/themes/space'),
       ('잠입', '금고를 털기 위한 잠입 작전 테마입니다.', '/themes/infiltration'),
       ('재난', '제한 시간 안에 탈출해야 하는 재난 테마입니다.', '/themes/disaster'),
       ('사극', '왕실의 비밀을 쫓는 사극 테마입니다.', '/themes/history'),
       ('모험', '유적을 탐험하는 어드벤처 테마입니다.', '/themes/adventure'),
       ('코미디', '유쾌한 소동이 가득한 코미디 테마입니다.', '/themes/comedy'),
       ('느와르', '어두운 도시를 배경으로 한 느와르 테마입니다.', '/themes/noir');

INSERT INTO member (name)
VALUES ('보예'),   -- 1
       ('이산'),   -- 2
       ('나무'),   -- 3
       ('피즈'),   -- 4
       ('제이콥'), -- 5
       ('보예짱'), -- 6
       ('이산짱'), -- 7
       ('나무짱'), -- 8
       ('피즈짱'), -- 9
       ('샤를'),   -- 10
       ('마이찬'), -- 11
       ('샤를짱'), -- 12
       ('마이찬짱'), -- 13
       ('브라운'), -- 14
       ('네오'),   -- 15
       ('브리'),   -- 16
       ('구구'),   -- 17
       ('리사'),   -- 18
       ('레서'),   -- 19
       ('바니'),   -- 20
       ('소낙눈'), -- 21
       ('카야'),   -- 22
       ('피노'),   -- 23
       ('우디'),   -- 24
       ('캐모'),   -- 25
       ('아이큐'), -- 26
       ('쿠다'),   -- 27
       ('고래');   -- 28

INSERT INTO reservation (member_id, date_id, time_id, theme_id)
VALUES (1,  1, 1, 1),
       (2,  1, 2, 1),
       (3,  2, 1, 2),
       (4,  2, 3, 2),
       (5,  3, 1, 1),
       (6,  3, 4, 3),
       (7,  3, 2, 1),
       (8,  4, 3, 3),
       (9,  4, 1, 2),
       (10, 4, 4, 1),
       (11, 5, 2, 1),
       (12, 5, 3, 8),
       (13, 5, 4, 5),
       (14, 6, 2, 11),
       (15, 6, 4, 4),
       (16, 6, 1, 9),
       (17, 7, 3, 6),
       (18, 7, 1, 12),
       (19, 7, 4, 7),
       (20, 8, 2, 10),
       (21, 8, 3, 3),
       (22, 8, 4, 8),
       (23, 9, 1, 5),
       (24, 9, 2, 11),
       (25, 10, 3, 4),
       (26, 10, 1, 9),
       (27, 11, 3, 6),
       (28, 11, 4, 10);

-- 고래(28) 대기 데이터
-- 슬롯1 (date+1, time2, theme1): 이산(2)이 먼저 대기 → 고래 2순위
-- 슬롯2 (date+2, time2, theme11): 고래만 대기 → 고래 1순위
-- 슬롯3 (date+3, time3, theme6): 보예(1)·나무(3)가 먼저 대기 → 고래 3순위
INSERT INTO waiting_reservation (member_id, date_id, time_id, theme_id, created_at)
VALUES (2,  5, 2, 1,  CURRENT_TIMESTAMP - INTERVAL '3' HOUR),
       (28, 5, 2, 1,  CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
       (28, 6, 2, 11, CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
       (1,  7, 3, 6,  CURRENT_TIMESTAMP - INTERVAL '4' HOUR),
       (3,  7, 3, 6,  CURRENT_TIMESTAMP - INTERVAL '3' HOUR),
       (28, 7, 3, 6,  CURRENT_TIMESTAMP - INTERVAL '2' HOUR);

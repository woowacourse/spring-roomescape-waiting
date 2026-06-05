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

INSERT INTO reservation (name, date_id, time_id, theme_id)
VALUES ('보예', 1, 1, 1),
       ('이산', 1, 2, 1),
       ('나무', 2, 1, 2),
       ('피즈', 2, 3, 2),
       ('제이콥', 3, 1, 1),
       ('보예짱', 3, 4, 3),
       ('이산짱', 3, 2, 1),
       ('나무짱', 4, 3, 3),
       ('피즈짱', 4, 1, 2),
       ('샤를', 4, 4, 1),
       ('마이찬', 5, 2, 1),
       ('샤를짱', 5, 3, 8),
       ('마이찬짱', 5, 4, 5),
       ('브라운', 6, 2, 11),
       ('네오', 6, 4, 4),
       ('브리', 6, 1, 9),
       ('구구', 7, 3, 6),
       ('리사', 7, 1, 12),
       ('레서', 7, 4, 7),
       ('바니', 8, 2, 10),
       ('소낙눈', 8, 3, 3),
       ('카야', 8, 4, 8),
       ('피노', 9, 1, 5),
       ('우디', 9, 2, 11),
       ('캐모', 10, 3, 4),
       ('아이큐', 10, 1, 9),
       ('쿠다', 11, 3, 6),
       ('고래', 11, 4, 10);

-- 고래 대기 데이터
-- 슬롯1 (date+1, time2, theme1): 이산이 먼저 대기 → 고래 2순위
-- 슬롯2 (date+2, time2, theme11): 고래만 대기 → 고래 1순위
-- 슬롯3 (date+3, time3, theme6): 보예·나무가 먼저 대기 → 고래 3순위
INSERT INTO waiting_reservation (name, date_id, time_id, theme_id, created_at)
VALUES ('이산', 5, 2, 1,  CURRENT_TIMESTAMP - INTERVAL '3' HOUR),
       ('고래', 5, 2, 1,  CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
       ('고래', 6, 2, 11, CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
       ('보예', 7, 3, 6,  CURRENT_TIMESTAMP - INTERVAL '4' HOUR),
       ('나무', 7, 3, 6,  CURRENT_TIMESTAMP - INTERVAL '3' HOUR),
       ('고래', 7, 3, 6,  CURRENT_TIMESTAMP - INTERVAL '2' HOUR);

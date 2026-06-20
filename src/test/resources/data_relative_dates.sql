-- reservation_time
INSERT INTO reservation_time (id, start_at)
VALUES (1, '10:00');

INSERT INTO reservation_time (id, start_at)
VALUES (2, '11:00');

INSERT INTO reservation_time (id, start_at)
VALUES (3, '12:00');

INSERT INTO reservation_time (id, start_at)
VALUES (4, '13:00');

INSERT INTO reservation_time (id, start_at)
VALUES (5, '14:00');

INSERT INTO reservation_time (id, start_at)
VALUES (6, '15:00');

INSERT INTO reservation_time (id, start_at)
VALUES (7, '16:00');

INSERT INTO reservation_time (id, start_at)
VALUES (8, '17:00');

INSERT INTO reservation_time (id, start_at)
VALUES (9, '18:00');

INSERT INTO reservation_time (id, start_at)
VALUES (10, '19:00');


-- theme
INSERT INTO theme (id, name, description, thumbnail_image_url)
VALUES (1, '미스터리 저택', '빅토리아 시대 영국의 의문스러운 저택을 탐험하세요', 'https://loremflickr.com/800/600/mansion,dark');

INSERT INTO theme (id, name, description, thumbnail_image_url)
VALUES (2, '우주 정거장', '고장난 우주 정거장에서 살아남으세요', 'https://loremflickr.com/800/600/spacestation,space');

INSERT INTO theme (id, name, description, thumbnail_image_url)
VALUES (3, '좀비 아포칼립스', '좀비가 점령한 도시에서 탈출하세요', 'https://loremflickr.com/800/600/zombie,apocalypse');

INSERT INTO theme (id, name, description, thumbnail_image_url)
VALUES (4, '고대 이집트', '파라오의 무덤에 숨겨진 비밀을 풀어내세요', 'https://loremflickr.com/800/600/egypt,pyramid');

INSERT INTO theme (id, name, description, thumbnail_image_url)
VALUES (5, '해적선의 보물', '카리브해 해적선에서 보물을 찾아 탈출하세요', 'https://loremflickr.com/800/600/pirate');

INSERT INTO theme (id, name, description, thumbnail_image_url)
VALUES (6, '폐쇄 병동', '버려진 병동의 어두운 비밀을 파헤치세요', 'https://loremflickr.com/800/600/abandoned,asylum');

INSERT INTO theme (id, name, description, thumbnail_image_url)
VALUES (7, '시간 여행자의 실험실', '시간 여행 실험에 갇힌 당신을 구하세요', 'https://loremflickr.com/800/600/laboratory,steampunk');

INSERT INTO theme (id, name, description, thumbnail_image_url)
VALUES (8, '마법사의 탑', '사라진 마법사의 탑에서 주문을 풀어내세요', 'https://loremflickr.com/800/600/wizard,tower');

INSERT INTO theme (id, name, description, thumbnail_image_url)
VALUES (9, '침몰하는 잠수함', '가라앉는 잠수함에서 탈출하세요', 'https://loremflickr.com/800/600/submarine,ocean');

INSERT INTO theme (id, name, description, thumbnail_image_url)
VALUES (10, '은행 금고', '삼엄한 경비를 뚫고 금고에서 탈출하세요', 'https://loremflickr.com/800/600/bank,vault');
-- reservation
-- 인기 테마 윈도우 (오늘=CURRENT_DATE 기준, 어제부터 7일 = CURRENT_DATE - 7일 ~ CURRENT_DATE - 1일)
-- 윈도우 내 카운트 목표:
--   theme 1: 5건, theme 5: 4건, theme 8: 4건, theme 3: 3건, theme 4: 3건,
--   theme 2: 2건, theme 7: 2건, theme 6: 1건, theme 9: 1건, theme 10: 1건

-- 윈도우 내 (기존)
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 3, 1);
MERGE INTO member (name)
KEY (name)
VALUES ('김민수');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '김민수'), id FROM slot WHERE date = DATEADD('DAY', -1, CURRENT_DATE) AND time_id = 3 AND theme_id = 1;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 5, 2);
MERGE INTO member (name)
KEY (name)
VALUES ('김민수');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '김민수'), id FROM slot WHERE date = DATEADD('DAY', -1, CURRENT_DATE) AND time_id = 5 AND theme_id = 2;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 7, 3);
MERGE INTO member (name)
KEY (name)
VALUES ('김민수');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '김민수'), id FROM slot WHERE date = DATEADD('DAY', -2, CURRENT_DATE) AND time_id = 7 AND theme_id = 3;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 4, 1);
MERGE INTO member (name)
KEY (name)
VALUES ('정유나');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '정유나'), id FROM slot WHERE date = DATEADD('DAY', -2, CURRENT_DATE) AND time_id = 4 AND theme_id = 1;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 6, 5);
MERGE INTO member (name)
KEY (name)
VALUES ('최도현');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '최도현'), id FROM slot WHERE date = DATEADD('DAY', -3, CURRENT_DATE) AND time_id = 6 AND theme_id = 5;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 8, 4);
MERGE INTO member (name)
KEY (name)
VALUES ('한소희');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '한소희'), id FROM slot WHERE date = DATEADD('DAY', -3, CURRENT_DATE) AND time_id = 8 AND theme_id = 4;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 2, 7);
MERGE INTO member (name)
KEY (name)
VALUES ('강민호');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '강민호'), id FROM slot WHERE date = DATEADD('DAY', -4, CURRENT_DATE) AND time_id = 2 AND theme_id = 7;

-- 윈도우 내 (theme 1: +3건 → 총 5건)
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -7, CURRENT_DATE), 3, 1);
MERGE INTO member (name)
KEY (name)
VALUES ('오현우');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '오현우'), id FROM slot WHERE date = DATEADD('DAY', -7, CURRENT_DATE) AND time_id = 3 AND theme_id = 1;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 2, 1);
MERGE INTO member (name)
KEY (name)
VALUES ('신예린');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '신예린'), id FROM slot WHERE date = DATEADD('DAY', -6, CURRENT_DATE) AND time_id = 2 AND theme_id = 1;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 1, 1);
MERGE INTO member (name)
KEY (name)
VALUES ('배수지');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '배수지'), id FROM slot WHERE date = DATEADD('DAY', -5, CURRENT_DATE) AND time_id = 1 AND theme_id = 1;

-- 윈도우 내 (theme 5: +3건 → 총 4건)
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 5);
MERGE INTO member (name)
KEY (name)
VALUES ('남궁민');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '남궁민'), id FROM slot WHERE date = DATEADD('DAY', -1, CURRENT_DATE) AND time_id = 1 AND theme_id = 5;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 2, 5);
MERGE INTO member (name)
KEY (name)
VALUES ('류준열');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '류준열'), id FROM slot WHERE date = DATEADD('DAY', -2, CURRENT_DATE) AND time_id = 2 AND theme_id = 5;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 3, 5);
MERGE INTO member (name)
KEY (name)
VALUES ('서지혜');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '서지혜'), id FROM slot WHERE date = DATEADD('DAY', -4, CURRENT_DATE) AND time_id = 3 AND theme_id = 5;

-- 윈도우 내 (theme 8: +4건 → 총 4건)
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 3, 8);
MERGE INTO member (name)
KEY (name)
VALUES ('곽도원');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '곽도원'), id FROM slot WHERE date = DATEADD('DAY', -6, CURRENT_DATE) AND time_id = 3 AND theme_id = 8;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 2, 8);
MERGE INTO member (name)
KEY (name)
VALUES ('전미도');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '전미도'), id FROM slot WHERE date = DATEADD('DAY', -5, CURRENT_DATE) AND time_id = 2 AND theme_id = 8;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 8);
MERGE INTO member (name)
KEY (name)
VALUES ('변요한');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '변요한'), id FROM slot WHERE date = DATEADD('DAY', -3, CURRENT_DATE) AND time_id = 1 AND theme_id = 8;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 9, 8);
MERGE INTO member (name)
KEY (name)
VALUES ('이주영');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '이주영'), id FROM slot WHERE date = DATEADD('DAY', -1, CURRENT_DATE) AND time_id = 9 AND theme_id = 8;

-- 윈도우 내 (theme 3: +2건 → 총 3건)
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 8, 3);
MERGE INTO member (name)
KEY (name)
VALUES ('홍은채');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '홍은채'), id FROM slot WHERE date = DATEADD('DAY', -4, CURRENT_DATE) AND time_id = 8 AND theme_id = 3;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 7, 3);
MERGE INTO member (name)
KEY (name)
VALUES ('차은우');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '차은우'), id FROM slot WHERE date = DATEADD('DAY', -1, CURRENT_DATE) AND time_id = 7 AND theme_id = 3;

-- 윈도우 내 (theme 4: +2건 → 총 3건)
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 4, 4);
MERGE INTO member (name)
KEY (name)
VALUES ('박보검');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '박보검'), id FROM slot WHERE date = DATEADD('DAY', -5, CURRENT_DATE) AND time_id = 4 AND theme_id = 4;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 8, 4);
MERGE INTO member (name)
KEY (name)
VALUES ('김다미');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '김다미'), id FROM slot WHERE date = DATEADD('DAY', -2, CURRENT_DATE) AND time_id = 8 AND theme_id = 4;

-- 윈도우 내 (theme 2: +1건 → 총 2건)
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 6, 2);
MERGE INTO member (name)
KEY (name)
VALUES ('손석구');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '손석구'), id FROM slot WHERE date = DATEADD('DAY', -2, CURRENT_DATE) AND time_id = 6 AND theme_id = 2;

-- 윈도우 내 (theme 7: +1건 → 총 2건)
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 5, 7);
MERGE INTO member (name)
KEY (name)
VALUES ('전여빈');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '전여빈'), id FROM slot WHERE date = DATEADD('DAY', -6, CURRENT_DATE) AND time_id = 5 AND theme_id = 7;

-- 윈도우 내 (theme 6: +1건 → 총 1건)
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 4, 6);
MERGE INTO member (name)
KEY (name)
VALUES ('이정은');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '이정은'), id FROM slot WHERE date = DATEADD('DAY', -3, CURRENT_DATE) AND time_id = 4 AND theme_id = 6;

-- 윈도우 내 (theme 9: +1건 → 총 1건)
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 5, 9);
MERGE INTO member (name)
KEY (name)
VALUES ('조여정');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '조여정'), id FROM slot WHERE date = DATEADD('DAY', -5, CURRENT_DATE) AND time_id = 5 AND theme_id = 9;

-- 윈도우 내 (theme 10: +1건 → 총 1건)
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 6, 10);
MERGE INTO member (name)
KEY (name)
VALUES ('이성민');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '이성민'), id FROM slot WHERE date = DATEADD('DAY', -6, CURRENT_DATE) AND time_id = 6 AND theme_id = 10;

-- 윈도우 직전 (CURRENT_DATE - 8일) — 카운트에 포함되면 안 됨
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', -8, CURRENT_DATE), 7, 1);
MERGE INTO member (name)
KEY (name)
VALUES ('윈도우직전');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '윈도우직전'), id FROM slot WHERE date = DATEADD('DAY', -8, CURRENT_DATE) AND time_id = 7 AND theme_id = 1;

-- 오늘 (CURRENT_DATE) — end가 어제(CURRENT_DATE - 1일)이므로 카운트에서 제외되어야 함
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (CURRENT_DATE, 8, 5);
MERGE INTO member (name)
KEY (name)
VALUES ('오늘예약');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '오늘예약'), id FROM slot WHERE date = CURRENT_DATE AND time_id = 8 AND theme_id = 5;

-- 미래 — 윈도우 밖
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', 8, CURRENT_DATE), 9, 8);
MERGE INTO member (name)
KEY (name)
VALUES ('윤채영');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '윤채영'), id FROM slot WHERE date = DATEADD('DAY', 8, CURRENT_DATE) AND time_id = 9 AND theme_id = 8;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', 8, CURRENT_DATE), 5, 6);
MERGE INTO member (name)
KEY (name)
VALUES ('임재현');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '임재현'), id FROM slot WHERE date = DATEADD('DAY', 8, CURRENT_DATE) AND time_id = 5 AND theme_id = 6;
MERGE INTO slot (date, time_id, theme_id)
KEY (date, time_id, theme_id)
VALUES (DATEADD('DAY', 9, CURRENT_DATE), 6, 10);
MERGE INTO member (name)
KEY (name)
VALUES ('송하은');
INSERT INTO reservation (member_id, slot_id)
SELECT (SELECT id FROM member WHERE name = '송하은'), id FROM slot WHERE date = DATEADD('DAY', 9, CURRENT_DATE) AND time_id = 6 AND theme_id = 10;

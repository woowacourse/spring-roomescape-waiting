-- reservation_time
INSERT INTO reservation_time (start_at)
VALUES ('10:00');
INSERT INTO reservation_time (start_at)
VALUES ('11:00');
INSERT INTO reservation_time (start_at)
VALUES ('12:00');
INSERT INTO reservation_time (start_at)
VALUES ('13:00');
INSERT INTO reservation_time (start_at)
VALUES ('14:00');
INSERT INTO reservation_time (start_at)
VALUES ('15:00');
INSERT INTO reservation_time (start_at)
VALUES ('16:00');
INSERT INTO reservation_time (start_at)
VALUES ('17:00');
INSERT INTO reservation_time (start_at)
VALUES ('18:00');
INSERT INTO reservation_time (start_at)
VALUES ('19:00');

-- theme
INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('미스터리 저택', '빅토리아 시대 영국의 의문스러운 저택을 탐험하세요', 'https://loremflickr.com/800/600/mansion,dark');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('우주 정거장', '고장난 우주 정거장에서 살아남으세요', 'https://loremflickr.com/800/600/spacestation,space');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('좀비 아포칼립스', '좀비가 점령한 도시에서 탈출하세요', 'https://loremflickr.com/800/600/zombie,apocalypse');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('고대 이집트', '파라오의 무덤에 숨겨진 비밀을 풀어내세요', 'https://loremflickr.com/800/600/egypt,pyramid');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('해적선의 보물', '카리브해 해적선에서 보물을 찾아 탈출하세요', 'https://loremflickr.com/800/600/pirate');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('폐쇄 병동', '버려진 병동의 어두운 비밀을 파헤치세요', 'https://loremflickr.com/800/600/abandoned,asylum');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('시간 여행자의 실험실', '시간 여행 실험에 갇힌 당신을 구하세요', 'https://loremflickr.com/800/600/laboratory,steampunk');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('마법사의 탑', '사라진 마법사의 탑에서 주문을 풀어내세요', 'https://loremflickr.com/800/600/wizard,tower');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('침몰하는 잠수함', '가라앉는 잠수함에서 탈출하세요', 'https://loremflickr.com/800/600/submarine,ocean');

INSERT INTO theme (name, description, thumbnail_image_url)
VALUES ('은행 금고', '삼엄한 경비를 뚫고 금고에서 탈출하세요', 'https://loremflickr.com/800/600/bank,vault');

-- reservation
-- 인기 테마 윈도우 (오늘=CURRENT_DATE 기준, 어제부터 7일 = CURRENT_DATE - 7일 ~ CURRENT_DATE - 1일)
-- 윈도우 내 카운트 목표:
--   theme 1: 5건, theme 5: 4건, theme 8: 4건, theme 3: 3건, theme 4: 3건,
--   theme 2: 2건, theme 7: 2건, theme 6: 1건, theme 9: 1건, theme 10: 1건

-- 윈도우 내 (기존)
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 3, 1);
MERGE INTO member (name)
KEY (name)
VALUES ('네오');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '네오'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -1, CURRENT_DATE) AND time_id = 3 AND theme_id = 1));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 5, 2);
MERGE INTO member (name)
KEY (name)
VALUES ('브라운');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '브라운'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -1, CURRENT_DATE) AND time_id = 5 AND theme_id = 2));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 7, 3);
MERGE INTO member (name)
KEY (name)
VALUES ('브리');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '브리'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -2, CURRENT_DATE) AND time_id = 7 AND theme_id = 3));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 4, 1);
MERGE INTO member (name)
KEY (name)
VALUES ('준');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '준'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -2, CURRENT_DATE) AND time_id = 4 AND theme_id = 1));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 6, 5);
MERGE INTO member (name)
KEY (name)
VALUES ('포비');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '포비'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -3, CURRENT_DATE) AND time_id = 6 AND theme_id = 5));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 8, 4);
MERGE INTO member (name)
KEY (name)
VALUES ('워니');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '워니'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -3, CURRENT_DATE) AND time_id = 8 AND theme_id = 4));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 2, 7);
MERGE INTO member (name)
KEY (name)
VALUES ('네오');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '네오'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -4, CURRENT_DATE) AND time_id = 2 AND theme_id = 7));

-- 윈도우 내 (theme 1: +3건 → 총 5건)
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -7, CURRENT_DATE), 3, 1);
MERGE INTO member (name)
KEY (name)
VALUES ('브라운');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '브라운'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -7, CURRENT_DATE) AND time_id = 3 AND theme_id = 1));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 2, 1);
MERGE INTO member (name)
KEY (name)
VALUES ('브리');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '브리'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -6, CURRENT_DATE) AND time_id = 2 AND theme_id = 1));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 1, 1);
MERGE INTO member (name)
KEY (name)
VALUES ('준');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '준'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -5, CURRENT_DATE) AND time_id = 1 AND theme_id = 1));

-- 윈도우 내 (theme 5: +3건 → 총 4건)
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 5);
MERGE INTO member (name)
KEY (name)
VALUES ('포비');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '포비'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -1, CURRENT_DATE) AND time_id = 1 AND theme_id = 5));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 2, 5);
MERGE INTO member (name)
KEY (name)
VALUES ('워니');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '워니'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -2, CURRENT_DATE) AND time_id = 2 AND theme_id = 5));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 3, 5);
MERGE INTO member (name)
KEY (name)
VALUES ('네오');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '네오'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -4, CURRENT_DATE) AND time_id = 3 AND theme_id = 5));

-- 윈도우 내 (theme 8: +4건 → 총 4건)
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 3, 8);
MERGE INTO member (name)
KEY (name)
VALUES ('브라운');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '브라운'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -6, CURRENT_DATE) AND time_id = 3 AND theme_id = 8));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 2, 8);
MERGE INTO member (name)
KEY (name)
VALUES ('브리');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '브리'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -5, CURRENT_DATE) AND time_id = 2 AND theme_id = 8));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 1, 8);
MERGE INTO member (name)
KEY (name)
VALUES ('준');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '준'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -3, CURRENT_DATE) AND time_id = 1 AND theme_id = 8));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 9, 8);
MERGE INTO member (name)
KEY (name)
VALUES ('포비');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '포비'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -1, CURRENT_DATE) AND time_id = 9 AND theme_id = 8));

-- 윈도우 내 (theme 3: +2건 → 총 3건)
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE), 8, 3);
MERGE INTO member (name)
KEY (name)
VALUES ('워니');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '워니'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -4, CURRENT_DATE) AND time_id = 8 AND theme_id = 3));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -1, CURRENT_DATE), 7, 3);
MERGE INTO member (name)
KEY (name)
VALUES ('네오');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '네오'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -1, CURRENT_DATE) AND time_id = 7 AND theme_id = 3));

-- 윈도우 내 (theme 4: +2건 → 총 3건)
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 4, 4);
MERGE INTO member (name)
KEY (name)
VALUES ('브라운');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '브라운'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -5, CURRENT_DATE) AND time_id = 4 AND theme_id = 4));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 8, 4);
MERGE INTO member (name)
KEY (name)
VALUES ('브리');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '브리'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -2, CURRENT_DATE) AND time_id = 8 AND theme_id = 4));

-- 윈도우 내 (theme 2: +1건 → 총 2건)
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE), 6, 2);
MERGE INTO member (name)
KEY (name)
VALUES ('준');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '준'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -2, CURRENT_DATE) AND time_id = 6 AND theme_id = 2));

-- 윈도우 내 (theme 7: +1건 → 총 2건)
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 5, 7);
MERGE INTO member (name)
KEY (name)
VALUES ('포비');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '포비'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -6, CURRENT_DATE) AND time_id = 5 AND theme_id = 7));

-- 윈도우 내 (theme 6: +1건 → 총 1건)
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -3, CURRENT_DATE), 4, 6);
MERGE INTO member (name)
KEY (name)
VALUES ('워니');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '워니'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -3, CURRENT_DATE) AND time_id = 4 AND theme_id = 6));

-- 윈도우 내 (theme 9: +1건 → 총 1건)
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -5, CURRENT_DATE), 5, 9);
MERGE INTO member (name)
KEY (name)
VALUES ('네오');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '네오'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -5, CURRENT_DATE) AND time_id = 5 AND theme_id = 9));

-- 윈도우 내 (theme 10: +1건 → 총 1건)
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -6, CURRENT_DATE), 6, 10);
MERGE INTO member (name)
KEY (name)
VALUES ('브라운');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '브라운'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -6, CURRENT_DATE) AND time_id = 6 AND theme_id = 10));

-- 윈도우 직전 (CURRENT_DATE - 8일) — 카운트에 포함되면 안 됨
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', -8, CURRENT_DATE), 7, 1);
MERGE INTO member (name)
KEY (name)
VALUES ('브리');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '브리'), (SELECT id FROM slot WHERE date = DATEADD('DAY', -8, CURRENT_DATE) AND time_id = 7 AND theme_id = 1));

-- 오늘 (CURRENT_DATE) — end가 어제(CURRENT_DATE - 1일)이므로 카운트에서 제외되어야 함
INSERT INTO slot (date, time_id, theme_id)
VALUES (CURRENT_DATE, 8, 5);
MERGE INTO member (name)
KEY (name)
VALUES ('준');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '준'), (SELECT id FROM slot WHERE date = CURRENT_DATE AND time_id = 8 AND theme_id = 5));

-- 미래 — 윈도우 밖
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', 8, CURRENT_DATE), 9, 8);
MERGE INTO member (name)
KEY (name)
VALUES ('포비');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '포비'), (SELECT id FROM slot WHERE date = DATEADD('DAY', 8, CURRENT_DATE) AND time_id = 9 AND theme_id = 8));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', 8, CURRENT_DATE), 5, 6);
MERGE INTO member (name)
KEY (name)
VALUES ('워니');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '워니'), (SELECT id FROM slot WHERE date = DATEADD('DAY', 8, CURRENT_DATE) AND time_id = 5 AND theme_id = 6));
INSERT INTO slot (date, time_id, theme_id)
VALUES (DATEADD('DAY', 9, CURRENT_DATE), 6, 10);
MERGE INTO member (name)
KEY (name)
VALUES ('네오');
INSERT INTO reservation (member_id, slot_id)
VALUES ((SELECT id FROM member WHERE name = '네오'), (SELECT id FROM slot WHERE date = DATEADD('DAY', 9, CURRENT_DATE) AND time_id = 6 AND theme_id = 10));

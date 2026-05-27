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
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('김민수', DATEADD('DAY', -1, CURRENT_DATE), 3, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('김민수', DATEADD('DAY', -1, CURRENT_DATE), 5, 2);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('김민수', DATEADD('DAY', -2, CURRENT_DATE), 7, 3);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('정유나', DATEADD('DAY', -2, CURRENT_DATE), 4, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('최도현', DATEADD('DAY', -3, CURRENT_DATE), 6, 5);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('한소희', DATEADD('DAY', -3, CURRENT_DATE), 8, 4);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('강민호', DATEADD('DAY', -4, CURRENT_DATE), 2, 7);

-- 윈도우 내 (theme 1: +3건 → 총 5건)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('오현우', DATEADD('DAY', -7, CURRENT_DATE), 3, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('신예린', DATEADD('DAY', -6, CURRENT_DATE), 2, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('배수지', DATEADD('DAY', -5, CURRENT_DATE), 1, 1);

-- 윈도우 내 (theme 5: +3건 → 총 4건)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('남궁민', DATEADD('DAY', -1, CURRENT_DATE), 1, 5);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('류준열', DATEADD('DAY', -2, CURRENT_DATE), 2, 5);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('서지혜', DATEADD('DAY', -4, CURRENT_DATE), 3, 5);

-- 윈도우 내 (theme 8: +4건 → 총 4건)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('곽도원', DATEADD('DAY', -6, CURRENT_DATE), 3, 8);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('전미도', DATEADD('DAY', -5, CURRENT_DATE), 2, 8);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('변요한', DATEADD('DAY', -3, CURRENT_DATE), 1, 8);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('이주영', DATEADD('DAY', -1, CURRENT_DATE), 9, 8);

-- 윈도우 내 (theme 3: +2건 → 총 3건)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('홍은채', DATEADD('DAY', -4, CURRENT_DATE), 8, 3);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('차은우', DATEADD('DAY', -1, CURRENT_DATE), 7, 3);

-- 윈도우 내 (theme 4: +2건 → 총 3건)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('박보검', DATEADD('DAY', -5, CURRENT_DATE), 4, 4);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('김다미', DATEADD('DAY', -2, CURRENT_DATE), 8, 4);

-- 윈도우 내 (theme 2: +1건 → 총 2건)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('손석구', DATEADD('DAY', -2, CURRENT_DATE), 6, 2);

-- 윈도우 내 (theme 7: +1건 → 총 2건)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('전여빈', DATEADD('DAY', -6, CURRENT_DATE), 5, 7);

-- 윈도우 내 (theme 6: +1건 → 총 1건)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('이정은', DATEADD('DAY', -3, CURRENT_DATE), 4, 6);

-- 윈도우 내 (theme 9: +1건 → 총 1건)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('조여정', DATEADD('DAY', -5, CURRENT_DATE), 5, 9);

-- 윈도우 내 (theme 10: +1건 → 총 1건)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('이성민', DATEADD('DAY', -6, CURRENT_DATE), 6, 10);

-- 윈도우 직전 (CURRENT_DATE - 8일) — 카운트에 포함되면 안 됨
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('윈도우직전', DATEADD('DAY', -8, CURRENT_DATE), 7, 1);

-- 오늘 (CURRENT_DATE) — end가 어제(CURRENT_DATE - 1일)이므로 카운트에서 제외되어야 함
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('오늘예약', CURRENT_DATE, 8, 5);

-- 미래 — 윈도우 밖
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('윤채영', DATEADD('DAY', 8, CURRENT_DATE), 9, 8);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('임재현', DATEADD('DAY', 8, CURRENT_DATE), 5, 6);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('송하은', DATEADD('DAY', 9, CURRENT_DATE), 6, 10);

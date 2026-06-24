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
-- 인기 테마 윈도우 (오늘=2026-05-27 기준, 어제부터 7일 = 2026-05-20 ~ 2026-05-26)
-- 윈도우 내 카운트 목표:
--   theme 1: 5건, theme 5: 4건, theme 8: 4건, theme 3: 3건, theme 4: 3건,
--   theme 2: 2건, theme 7: 2건, theme 6: 1건, theme 9: 1건, theme 10: 1건

-- 윈도우 내 (기존)
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('김민수', '2026-05-26', 3, 1, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('이지영', '2026-05-26', 5, 2, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('박서준', '2026-05-25', 7, 3, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('정유나', '2026-05-25', 4, 1, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('최도현', '2026-05-24', 6, 5, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('한소희', '2026-05-24', 8, 4, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('강민호', '2026-05-23', 2, 7, 'CONFIRM');

-- 윈도우 내 (theme 1: +3건 → 총 5건)
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('오현우', '2026-05-20', 3, 1, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('신예린', '2026-05-21', 2, 1, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('배수지', '2026-05-22', 1, 1, 'CONFIRM');

-- 윈도우 내 (theme 5: +3건 → 총 4건)
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('남궁민', '2026-05-26', 1, 5, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('류준열', '2026-05-25', 2, 5, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('서지혜', '2026-05-23', 3, 5, 'CONFIRM');

-- 윈도우 내 (theme 8: +4건 → 총 4건)
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('곽도원', '2026-05-21', 3, 8, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('전미도', '2026-05-22', 2, 8, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('변요한', '2026-05-24', 1, 8, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('이주영', '2026-05-26', 9, 8, 'CONFIRM');

-- 윈도우 내 (theme 3: +2건 → 총 3건)
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('홍은채', '2026-05-23', 8, 3, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('차은우', '2026-05-26', 7, 3, 'CONFIRM');

-- 윈도우 내 (theme 4: +2건 → 총 3건)
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('박보검', '2026-05-22', 4, 4, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('김다미', '2026-05-25', 8, 4, 'CONFIRM');

-- 윈도우 내 (theme 2: +1건 → 총 2건)
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('손석구', '2026-05-25', 6, 2, 'CONFIRM');

-- 윈도우 내 (theme 7: +1건 → 총 2건)
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('전여빈', '2026-05-21', 5, 7, 'CONFIRM');

-- 윈도우 내 (theme 6: +1건 → 총 1건)
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('이정은', '2026-05-24', 4, 6, 'CONFIRM');

-- 윈도우 내 (theme 9: +1건 → 총 1건)
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('조여정', '2026-05-22', 5, 9, 'CONFIRM');

-- 윈도우 내 (theme 10: +1건 → 총 1건)
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('이성민', '2026-05-21', 6, 10, 'CONFIRM');

-- 윈도우 직전 (2026-05-19) — 카운트에 포함되면 안 됨
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('윈도우직전', '2026-05-19', 7, 1, 'CONFIRM');

-- 오늘 (2026-05-27) — end가 어제(05-26)이므로 카운트에서 제외되어야 함
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('오늘예약', '2026-05-27', 10, 5, 'CONFIRM');

-- 미래 — 윈도우 밖
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('윤채영', '2026-06-03', 9, 8, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('임재현', '2026-06-03', 5, 6, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('송하은', '2026-06-04', 6, 10, 'CONFIRM');
INSERT INTO reservation (name, date, time_id, theme_id, reservation_status)
VALUES ('민욱', '2026-06-04', 4, 3, 'CONFIRM');

-- reservation_waiting
-- 미래 예약 슬롯에 대한 대기 데이터
INSERT INTO reservation_waiting (name, created_at, reservation_id)
SELECT '민욱', '2026-05-27 09:00:00', id
FROM reservation
WHERE name = '윤채영' AND date = '2026-06-03' AND time_id = 9 AND theme_id = 8;

INSERT INTO reservation_waiting (name, created_at, reservation_id)
SELECT '브라운', '2026-05-27 09:05:00', id
FROM reservation
WHERE name = '윤채영' AND date = '2026-06-03' AND time_id = 9 AND theme_id = 8;

INSERT INTO reservation_waiting (name, created_at, reservation_id)
SELECT '티뉴', '2026-05-27 09:10:00', id
FROM reservation
WHERE name = '임재현' AND date = '2026-06-03' AND time_id = 5 AND theme_id = 6;

INSERT INTO reservation_waiting (name, created_at, reservation_id)
SELECT '민욱', '2026-05-27 09:15:00', id
FROM reservation
WHERE name = '송하은' AND date = '2026-06-04' AND time_id = 6 AND theme_id = 10;

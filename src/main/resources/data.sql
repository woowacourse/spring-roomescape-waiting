DELETE FROM reservation_waiting;
DELETE FROM reservation;
DELETE FROM reservation_slot;
DELETE FROM reservation_time;
DELETE FROM theme;

ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1;
ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;
ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 1;
ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1;
ALTER TABLE theme ALTER COLUMN id RESTART WITH 1;

INSERT INTO reservation_time (start_at) VALUES
('10:00:00'),
('11:00:00'),
('12:00:00'),
('13:00:00'),
('14:00:00'),
('15:00:00'),
('16:00:00'),
('17:00:00'),
('18:00:00');

INSERT INTO theme (name, description, thumbnailUrl) VALUES
('우주선 탈출', '고장 난 우주선에서 제한 시간 안에 탈출하세요.', 'https://example.com/themes/space-escape.jpg'),
('좀비 아포칼립스', '봉쇄된 도시에서 생존 키트를 찾아 탈출해야 합니다.', 'https://example.com/themes/zombie-apocalypse.jpg'),
('고대 피라미드', '피라미드 깊숙한 곳의 비밀 방을 열어 보물을 찾으세요.', 'https://example.com/themes/pyramid.jpg'),
('마법학교의 비밀', '사라진 마법서를 찾아 학교의 저주를 풀어야 합니다.', 'https://example.com/themes/magic-school.jpg'),
('해적선의 보물', '해적선 선장의 단서를 모아 숨겨진 보물창고를 여세요.', 'https://example.com/themes/pirate-treasure.jpg'),
('미스터리 연구소', '폐쇄된 연구소에서 실험 기록을 복구하고 탈출하세요.', 'https://example.com/themes/lab-mystery.jpg'),
('시간여행자', '뒤틀린 시간 장치를 복구해 현재로 돌아오세요.', 'https://example.com/themes/time-traveler.jpg'),
('유령의 저택', '밤이 끝나기 전 저택의 원혼을 달래는 의식을 완성하세요.', 'https://example.com/themes/haunted-mansion.jpg'),
('사라진 화가의 작품', '실종된 화가가 남긴 암호를 풀어 진짜 작품을 찾으세요.', 'https://example.com/themes/missing-painting.jpg'),
('심해 탐험', '산소가 떨어지기 전에 심해 기지의 전원을 복구해야 합니다.', 'https://example.com/themes/deep-sea.jpg'),
('왕실 음모', '왕궁에서 벌어진 음모의 증거를 찾아 누명을 벗기세요.', 'https://example.com/themes/royal-conspiracy.jpg'),
('폐병원 탈출', '버려진 병원에서 수상한 흔적을 추적해 출구를 찾으세요.', 'https://example.com/themes/abandoned-hospital.jpg'),
('한밤중의 서커스', '멈춰버린 서커스 공연의 비밀을 밝히고 무대를 탈출하세요.', 'https://example.com/themes/midnight-circus.jpg'),
('비밀 요원 작전', '이중 잠금 장치를 해제하고 기밀 문서를 회수하세요.', 'https://example.com/themes/secret-agent.jpg'),
('드래곤의 동굴', '드래곤이 잠든 사이 고대 룬을 해독해 동굴을 빠져나오세요.', 'https://example.com/themes/dragon-cave.jpg');

INSERT INTO reservation_slot (reservation_date, time_id, theme_id) VALUES
('2026-05-27', 1, 1),
('2026-05-27', 2, 2),
('2026-05-27', 3, 3),
('2026-05-27', 4, 4),
('2026-05-27', 5, 5),
('2026-05-26', 1, 6),
('2026-05-26', 2, 7),
('2026-05-26', 3, 8),
('2026-05-26', 4, 9),
('2026-05-26', 5, 10),
('2026-05-25', 1, 11),
('2026-05-25', 2, 12),
('2026-05-25', 3, 13),
('2026-05-25', 4, 14),
('2026-05-25', 5, 15),
('2026-05-24', 1, 2),
('2026-05-23', 2, 4),
('2026-05-22', 3, 6),
('2026-05-21', 4, 8),
('2026-05-21', 5, 10),
('2026-05-20', 1, 3),
('2026-05-18', 2, 5),
('2026-05-16', 3, 7),
('2026-05-13', 4, 9),
('2026-05-08', 5, 11),
('2026-04-28', 1, 13),
('2026-04-15', 2, 15);

INSERT INTO reservation (name, slot_id) VALUES
('Minsu Kim', 1),
('Soyeon Lee', 2),
('Jihoon Park', 3),
('Yujin Choi', 4),
('Haneul Jung', 5),
('Jimin Han', 6),
('Sehun Oh', 7),
('Areum Yoon', 8),
('Doyoon Kang', 9),
('Yerin Shin', 10),
('Jaehyun Lim', 11),
('Nayeon Song', 12),
('Hyunwoo Jo', 13),
('Sujin Baek', 14),
('Jiho Moon', 15),
('Daeun Seo', 16),
('Minjae Kwon', 17),
('Jisu Nam', 18),
('Yejun Hong', 19),
('Dain Yoo', 20),
('Taeyoon Jang', 21),
('Seojin Noh', 22),
('Siwoo Ryu', 23),
('Gaeun Bae', 24),
('Hyunseo Ahn', 25),
('Mina Koo', 26),
('Dohyun Cha', 27);

INSERT INTO reservation_waiting (name, slot_id) VALUES
('Waiting Man', 1),
('Waiting Woman', 2),
('Waiting Boy', 3);


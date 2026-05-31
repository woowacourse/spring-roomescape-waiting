-- 실제 환경에 위치한 실 데이터
INSERT INTO reservation_time (start_at)
VALUES ('10:00'); -- id 1
INSERT INTO reservation_time (start_at)
VALUES ('11:00'); -- id 2
INSERT INTO reservation_time (start_at)
VALUES ('12:00'); -- id 3
INSERT INTO reservation_time (start_at)
VALUES ('13:00'); -- id 4

INSERT INTO theme (name, description, thumbnail_url)
VALUES ('세기의 도둑', '보안을 뚫고 보석을 훔쳐라', './images/thief.jpeg'),
       ('심해 연구소', '심해 기지를 탈출하라', './images/deepsea.jpeg'),
       ('시간 여행자', '과거와 미래를 오가며 단서를 찾아라', './images/time.jpeg'),
       ('유령 호텔', '폐허가 된 호텔에서 사라진 손님을 찾아라', './images/ghosthotel.jpeg'),
       ('비밀 실험동', '통제 구역 깊숙한 곳의 실험 기록을 회수하라', './images/lab-wing.png'),
       ('왕실 감옥', '감시를 피해 감옥의 비밀 통로를 열어라', './images/royal-prison.jpeg'),
       ('마녀의 숲', '저주를 풀 단서를 모아 숲을 빠져나와라', './images/witch-forest.jpeg'),
       ('우주 정거장', '고장 난 정거장을 복구하고 귀환 신호를 보내라', './images/space-station.jpeg'),
       ('잠든 박물관', '야간 경비를 피해 전시실의 비밀을 밝혀라', './images/museum.jpeg'),
       ('붉은 파도', '폭풍우 치는 선박 위에서 사건의 진실을 추적하라', './images/red-wave.jpeg');

INSERT INTO schedule (date, time_id, theme_id)
VALUES (DATEADD('DAY', 1, CURRENT_DATE), 1, 1),  -- 기본일 10시 세기의 도둑 / 1
       (DATEADD('DAY', 1, CURRENT_DATE), 2, 1),  -- 기본일 11시 세기의 도둑 / 2
       (DATEADD('DAY', 1, CURRENT_DATE), 3, 1),  -- 기본일 12시 세기의 도둑 / 3
       (DATEADD('DAY', 1, CURRENT_DATE), 4, 1),  -- 기본일 13시 세기의 도둑 / 4
       (DATEADD('DAY', 2, CURRENT_DATE), 1, 1),  -- 다음날 10시 세기의 도둑 / 5
       (DATEADD('DAY', 2, CURRENT_DATE), 2, 1),  -- 다음날 11시 세기의 도둑 / 6
       (DATEADD('DAY', 2, CURRENT_DATE), 3, 1),  -- 다음날 12시 세기의 도둑 / 7
       (DATEADD('DAY', 2, CURRENT_DATE), 4, 1),  -- 다음날 13시 세기의 도둑 / 8

       (DATEADD('DAY', 1, CURRENT_DATE), 1, 2),  -- 기본일 10시 심해 연구소 / 9
       (DATEADD('DAY', 1, CURRENT_DATE), 2, 2),  -- 기본일 11시 심해 연구소 / 10
       (DATEADD('DAY', 1, CURRENT_DATE), 3, 2),  -- 기본일 12시 심해 연구소 / 11
       (DATEADD('DAY', 1, CURRENT_DATE), 4, 2),  -- 기본일 13시 심해 연구소 / 12
       (DATEADD('DAY', 2, CURRENT_DATE), 1, 2),  -- 다음날 10시 심해 연구소 / 13
       (DATEADD('DAY', 2, CURRENT_DATE), 2, 2),  -- 다음날 11시 심해 연구소 / 14
       (DATEADD('DAY', 2, CURRENT_DATE), 3, 2),  -- 다음날 12시 심해 연구소 / 15
       (DATEADD('DAY', 2, CURRENT_DATE), 4, 2),  -- 다음날 13시 심해 연구소 / 16

       (DATEADD('DAY', 1, CURRENT_DATE), 1, 3),  -- 기본일 10시 시간 여행자 / 17
       (DATEADD('DAY', 1, CURRENT_DATE), 2, 3),  -- 기본일 11시 시간 여행자 / 18
       (DATEADD('DAY', 1, CURRENT_DATE), 3, 3),  -- 기본일 12시 시간 여행자 / 19
       (DATEADD('DAY', 1, CURRENT_DATE), 4, 3),  -- 기본일 13시 시간 여행자 / 20
       (DATEADD('DAY', 2, CURRENT_DATE), 1, 3),  -- 다음날 10시 시간 여행자 / 21
       (DATEADD('DAY', 2, CURRENT_DATE), 2, 3),  -- 다음날 11시 시간 여행자 / 22
       (DATEADD('DAY', 2, CURRENT_DATE), 3, 3),  -- 다음날 12시 시간 여행자 / 23
       (DATEADD('DAY', 2, CURRENT_DATE), 4, 3),  -- 다음날 13시 시간 여행자 / 24

       (DATEADD('DAY', 1, CURRENT_DATE), 1, 4),  -- 기본일 10시 유령 호텔 / 25
       (DATEADD('DAY', 1, CURRENT_DATE), 2, 4),  -- 기본일 11시 유령 호텔 / 26
       (DATEADD('DAY', 1, CURRENT_DATE), 3, 4),  -- 기본일 12시 유령 호텔 / 27
       (DATEADD('DAY', 1, CURRENT_DATE), 4, 4),  -- 기본일 13시 유령 호텔 / 28

       (DATEADD('DAY', 1, CURRENT_DATE), 1, 5),  -- 기본일 10시 비밀 실험동 / 29
       (DATEADD('DAY', 1, CURRENT_DATE), 2, 5),  -- 기본일 11시 비밀 실험동 / 30
       (DATEADD('DAY', 1, CURRENT_DATE), 3, 5),  -- 기본일 12시 비밀 실험동 / 31
       (DATEADD('DAY', 1, CURRENT_DATE), 4, 5),  -- 기본일 13시 비밀 실험동 / 32

       (DATEADD('DAY', 1, CURRENT_DATE), 1, 6),  -- 기본일 10시 왕실 감옥 / 33
       (DATEADD('DAY', 1, CURRENT_DATE), 2, 6),  -- 기본일 11시 왕실 감옥 / 34
       (DATEADD('DAY', 1, CURRENT_DATE), 3, 6),  -- 기본일 12시 왕실 감옥 / 35
       (DATEADD('DAY', 1, CURRENT_DATE), 4, 6),  -- 기본일 13시 왕실 감옥 / 36

       (DATEADD('DAY', 1, CURRENT_DATE), 1, 7),  -- 기본일 10시 마녀의 숲 / 37
       (DATEADD('DAY', 1, CURRENT_DATE), 2, 7),  -- 기본일 11시 마녀의 숲 / 38
       (DATEADD('DAY', 1, CURRENT_DATE), 3, 7),  -- 기본일 12시 마녀의 숲 / 39
       (DATEADD('DAY', 1, CURRENT_DATE), 4, 7),  -- 기본일 13시 마녀의 숲 / 40

       (DATEADD('DAY', 1, CURRENT_DATE), 1, 8),  -- 기본일 10시 우주 정거장 / 41
       (DATEADD('DAY', 1, CURRENT_DATE), 2, 8),  -- 기본일 11시 우주 정거장 / 42
       (DATEADD('DAY', 1, CURRENT_DATE), 3, 8),  -- 기본일 12시 우주 정거장 / 43
       (DATEADD('DAY', 1, CURRENT_DATE), 4, 8),  -- 기본일 13시 우주 정거장 / 44

       (DATEADD('DAY', 1, CURRENT_DATE), 1, 9),  -- 기본일 10시 잠든 박물관 / 45
       (DATEADD('DAY', 1, CURRENT_DATE), 2, 9),  -- 기본일 11시 잠든 박물관 / 46
       (DATEADD('DAY', 1, CURRENT_DATE), 3, 9),  -- 기본일 12시 잠든 박물관 / 47
       (DATEADD('DAY', 1, CURRENT_DATE), 4, 9),  -- 기본일 13시 잠든 박물관 / 48

       (DATEADD('DAY', 1, CURRENT_DATE), 1, 10), -- 기본일 10시 붉은 파도 / 49
       (DATEADD('DAY', 1, CURRENT_DATE), 2, 10), -- 기본일 11시 붉은 파도 / 50
       (DATEADD('DAY', 1, CURRENT_DATE), 3, 10), -- 기본일 12시 붉은 파도 / 51
       (DATEADD('DAY', 1, CURRENT_DATE), 4, 10), -- 기본일 13시 붉은 파도 / 52

       (DATEADD('DAY', 3, CURRENT_DATE), 1, 10), -- 사흘 뒤 10시 붉은 파도 / 53
       (DATEADD('DAY', 3, CURRENT_DATE), 2, 10), -- 사흘 뒤 11시 붉은 파도 / 54
       (DATEADD('DAY', 3, CURRENT_DATE), 3, 10), -- 사흘 뒤 12시 붉은 파도 / 55
       (DATEADD('DAY', 3, CURRENT_DATE), 4, 10); -- 사흘 뒤 13시 붉은 파도 / 56

INSERT INTO member (name, password, role)
VALUES ('kim', 'kimpass', 'USER'),
       ('lee', 'leepass', 'USER'),
       ('park', 'parkpass', 'USER'),
       ('oh', 'ohpass', 'USER'),
       ('manager', 'mpass', 'MANAGER');

INSERT INTO reservation (member_id, schedule_id)
VALUES (1, 1),  -- 기본일 10시 세기의 도둑 예약
       (1, 2),  -- 기본일 11시 세기의 도둑 예약
       (2, 9),  -- 기본일 10시 심해 연구소 예약
       (2, 10), -- 기본일 11시 심해 연구소 예약
       (2, 11), -- 기본일 12시 심해 연구소 예약
       (3, 21), -- 다음날 10시 시간 여행자 예약
       (3, 22), -- 다음날 11시 시간 여행자 예약
       (3, 23), -- 다음날 12시 시간 여행자 예약
       (3, 24), -- 다음날 13시 시간 여행자 예약
       (1, 53), -- 사흘 뒤 10시 붉은 파도 예약
       (1, 54); -- 사흘 뒤 11시 붉은 파도 예약

-- 클라이언트 확인용 예약 대기 데이터
INSERT INTO waiting (member_id, schedule_id)
VALUES (3, 1),  -- park: 기본일 10시 세기의 도둑 대기 1번
       (2, 1),  -- lee : 기본일 10시 세기의 도둑 대기 2번
       (4, 1),  -- oh  : 기본일 10시 세기의 도둑 대기 3번
       (2, 2),  -- lee : 기본일 11시 세기의 도둑 대기 1번
       (2, 21), -- lee : 다음날 10시 시간 여행자 대기 1번
       (4, 22), -- oh  : 다음날 11시 시간 여행자 대기 1번
       (2, 22), -- lee : 다음날 11시 시간 여행자 대기 2번
       (3, 9),  -- park: 기본일 10시 심해 연구소 대기 1번
       (4, 9);  -- oh  : 기본일 10시 심해 연구소 대기 2번

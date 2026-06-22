-- 시간 (08:00 ~ 20:00, 3시간 간격, 5개)
INSERT INTO reservation_time (start_at) VALUES ('08:00');
INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00');
INSERT INTO reservation_time (start_at) VALUES ('14:00');
INSERT INTO reservation_time (start_at) VALUES ('16:00');
INSERT INTO reservation_time (start_at) VALUES ('18:00');
INSERT INTO reservation_time (start_at) VALUES ('20:00');
INSERT INTO reservation_time (start_at) VALUES ('22:00');

-- 테마 (15개)
INSERT INTO theme (name, description, thumbnail) VALUES ('공포의 방', '심장이 멎을 듯한 공포 체험', '/images/공포.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('추리의 방', '명탐정이 되어 사건을 해결하라', '/images/추리.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('SF 우주선', '미래 우주에서 살아남기', '/images/SF 우주선.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('판타지 왕국', '마법과 모험의 세계', '/images/판타지 왕국.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('좀비 아포칼립스', '좀비로부터 도시를 탈출하라', '/images/좀비 아포칼립스.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('이집트 무덤', '파라오의 저주를 풀어라', '/images/이집트 무덤.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('비밀 연구소', '의문의 실험실에서 탈출하기', '/images/비밀 연구소.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('해적선', '보물을 찾아 떠나는 항해', '/images/해적선.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('우주정거장', '고장 난 우주정거장 탈출', '/images/우주정거장.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('마법학교', '마법사들의 비밀을 풀어라', '/images/마법학교.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('시간여행', '과거와 미래를 넘나드는 모험', '/images/시간 여행.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('고대 신전', '잃어버린 유적의 비밀', '/images/고대 신전.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('닌자 마을', '그림자 속에서 임무를 완수하라', '/images/닌자 마을.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('외계 행성', '미지의 행성에서 살아남기', '/images/외계 행성.png');
INSERT INTO theme (name, description, thumbnail) VALUES ('초자연 현상', '설명할 수 없는 현상을 조사하라', '/images/초자연 현상.png');

-- 예약 (지난 1주일 12건 + 향후 12건)
-- 지난 1주일 (popular 테마 분포: 공포 4건, 추리 3건, SF 2건, 판타지 2건, 좀비 1건)
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('고래', DATEADD('DAY', -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('구구', DATEADD('DAY', -1, CURRENT_DATE), 2, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('그해', DATEADD('DAY', -2, CURRENT_DATE), 3, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('글렌', DATEADD('DAY', -3, CURRENT_DATE), 4, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('나무', DATEADD('DAY', -2, CURRENT_DATE), 1, 2);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('라이', DATEADD('DAY', -4, CURRENT_DATE), 2, 2);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('레서', DATEADD('DAY', -5, CURRENT_DATE), 3, 2);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('루디', DATEADD('DAY', -3, CURRENT_DATE), 1, 3);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('리사', DATEADD('DAY', -6, CURRENT_DATE), 2, 3);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('보예', DATEADD('DAY', -7, CURRENT_DATE), 1, 4);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('봉구스', DATEADD('DAY', -4, CURRENT_DATE), 5, 4);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('서여', DATEADD('DAY', -7, CURRENT_DATE), 3, 5);

-- 향후 예약 (예약 변경/취소 흐름 테스트용)
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('시오', DATEADD('DAY', 1, CURRENT_DATE), 1, 6);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('아이큐', DATEADD('DAY', 1, CURRENT_DATE), 2, 7);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('에버', DATEADD('DAY', 1, CURRENT_DATE), 3, 8);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('영기', DATEADD('DAY', 2, CURRENT_DATE), 1, 9);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('우디', DATEADD('DAY', 2, CURRENT_DATE), 2, 10);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('제이콥', DATEADD('DAY', 2, CURRENT_DATE), 3, 11);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('제이크', DATEADD('DAY', 3, CURRENT_DATE), 1, 12);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('캐리', DATEADD('DAY', 3, CURRENT_DATE), 2, 13);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('캐모', DATEADD('DAY', 3, CURRENT_DATE), 3, 14);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('티뉴', DATEADD('DAY', 4, CURRENT_DATE), 1, 15);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('티모', DATEADD('DAY', 4, CURRENT_DATE), 2, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('티온', DATEADD('DAY', 5, CURRENT_DATE), 1, 2);

-- 시드 예약은 결제까지 끝난 확정 예약으로 취급한다(기본값 PAYMENT_PENDING으로 두면 내역에서 '결제 대기'로 보인다).
UPDATE reservation SET status = 'CONFIRMED';

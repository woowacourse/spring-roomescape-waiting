-- 예약 시간
INSERT INTO reservation_time (start_at, end_at) VALUES ('10:00:00', '11:30:00');
INSERT INTO reservation_time (start_at, end_at) VALUES ('12:00:00', '13:30:00');
INSERT INTO reservation_time (start_at, end_at) VALUES ('14:00:00', '15:30:00');
INSERT INTO reservation_time (start_at, end_at) VALUES ('16:00:00', '17:30:00');
INSERT INTO reservation_time (start_at, end_at) VALUES ('18:00:00', '19:30:00');
INSERT INTO reservation_time (start_at, end_at) VALUES ('20:00:00', '21:30:00');

-- 테마
INSERT INTO theme (name, description, thumbnail_url) VALUES (
    '망각의 정신병원',
    '오래된 폐정신병원에 갇혔다. 깜빡이는 형광등, 복도 끝에서 들려오는 발소리... 여기서 탈출하려면 이 병원의 어두운 비밀을 파헤쳐야 한다.',
    '/images/theme_asylum.png'
);
INSERT INTO theme (name, description, thumbnail_url) VALUES (
    '스테이션 제로',
    '심우주 정거장에서 혼자 잠에서 깨어났다. 승무원들은 어디로 갔을까? 산소가 고갈되기 전에 통신을 복구하고 탈출 포드를 가동해야 한다.',
    '/images/theme_space.png'
);
INSERT INTO theme (name, description, thumbnail_url) VALUES (
    '파라오의 금고',
    '고대 이집트의 비밀 지하 묘실에 발을 들였다. 함정과 암호로 가득한 파라오의 저주를 풀지 못하면 이 돌무덤이 영원한 안식처가 된다.',
    '/images/theme_pharaoh.png'
);
INSERT INTO theme (name, description, thumbnail_url) VALUES (
    '크림슨 레터',
    '1940년대 뒷골목의 사설탐정 사무소. 테이블 위의 붉은 봉투에는 연쇄 살인마의 단서가 담겨 있다. 다음 피해자가 되기 전에 사건을 해결하라.',
    '/images/theme_detective.png'
);
INSERT INTO theme (name, description, thumbnail_url) VALUES (
    '저주받은 저택',
    '빅토리아 시대의 대저택, 눈을 따라오는 초상화들, 삐걱거리는 계단. 저택의 주인이 남긴 마지막 수수께끼를 풀어야만 이 저주에서 벗어날 수 있다.',
    '/images/theme_haunted_mansion.png'
);
INSERT INTO theme (name, description, thumbnail_url) VALUES (
    '심해 잠수함',
    '수심 3,000m에서 엔진이 멈췄다. 산소 잔량 90분. 차가운 심해에서 잠수함 시스템을 복구하고, 수면 위로 부상하기 위한 마지막 사투를 벌여라.',
    '/images/theme_submarine.png'
);
INSERT INTO theme (name, description, thumbnail_url) VALUES (
    '금서의 도서관',
    '비밀결사의 지하 도서관. 책장 뒤에 숨겨진 통로, 암호화된 고문서, 촛불 하나만 켜진 채 떠나야 한다. 금서에 담긴 진실을 밝혀라.',
    '/images/theme_library.png'
);
INSERT INTO theme (name, description, thumbnail_url) VALUES (
    '괴박사의 실험실',
    '광기에 빠진 과학자의 지하 실험실. 형광색 액체가 끓어오르고 테슬라 코일이 번쩍인다. 다음 실험 재료가 되기 전에 탈출구를 찾아라.',
    '/images/theme_laboratory.png'
);
INSERT INTO theme (name, description, thumbnail_url) VALUES (
    '해적선의 보물',
    '카리브해의 해적선 선장 선실. 보물지도, 나침반, 잠긴 보물 상자. 바다 한가운데 떠 있는 이 배에서 진짜 보물을 찾아 항구로 돌아와라.',
    '/images/theme_pirate.png'
);

INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('홍길동', DATEADD('DAY', -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('김철수', DATEADD('DAY', 1, CURRENT_DATE), 3, 2);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('이영희', DATEADD('DAY', 2, CURRENT_DATE), 2, 3);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('박민준', DATEADD('DAY', 3, CURRENT_DATE), 5, 4);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('최지우', DATEADD('DAY', 1, CURRENT_DATE), 4, 1);

INSERT INTO orders (order_id, idempotency_key, amount, payment_key, reservation_id, status) VALUES ('order-sample-001', 'idem-key-001', 50000, null, 1, 'PENDING');
INSERT INTO orders (order_id, idempotency_key, amount, payment_key, reservation_id, status) VALUES ('order-sample-002', 'idem-key-002', 50000, null, 2, 'PENDING');
INSERT INTO orders (order_id, idempotency_key, amount, payment_key, reservation_id, status) VALUES ('order-sample-003', 'idem-key-003', 50000, null, 3, 'PENDING');
INSERT INTO orders (order_id, idempotency_key, amount, payment_key, reservation_id, status) VALUES ('order-sample-004', 'idem-key-004', 50000, null, 4, 'PENDING');
INSERT INTO orders (order_id, idempotency_key, amount, payment_key, reservation_id, status) VALUES ('order-sample-005', 'idem-key-005', 50000, null, 5, 'PENDING');

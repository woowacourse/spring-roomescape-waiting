INSERT INTO theme (name, description, thumbnail_image_url, price, is_active)
VALUES ('화이트노이즈', 'SF 추리 테마', 'https://picsum.photos/seed/1/800/600', 30000, 1),
       ('클락워크 호텔', '미스터리 테마', 'https://picsum.photos/seed/2/800/600', 30000, 1),
       ('선셋 다이브', '어드벤처 테마', 'https://picsum.photos/seed/3/800/600', 25000, 1),
       ('스모크 스테이지', '퍼포먼스 테마', 'https://picsum.photos/seed/4/800/600', 25000, 1),
       ('문라이트 아카이브', '감성 판타지 테마', 'https://picsum.photos/seed/5/800/600', 28000, 1);

INSERT INTO reservation_time (start_at, status)
VALUES ('10:00:00', 'ACTIVE'),
       ('11:30:00', 'ACTIVE'),
       ('13:00:00', 'ACTIVE'),
       ('14:30:00', 'ACTIVE'),
       ('16:00:00', 'ACTIVE'),
       ('18:00:00', 'ACTIVE'),
       ('19:30:00', 'ACTIVE');

INSERT INTO reservation (date, theme_id, time_id)
VALUES
    -- 동일한 날짜(어제)에 시간이 다른 데이터 배치
    -- 예상 정렬 순서: 이프(19:30) -> 두둠(10:00)
    (DATEADD('DAY', -1, CURRENT_DATE), 1, 1), -- 어제 10:00
    (DATEADD('DAY', -1, CURRENT_DATE), 2, 7), -- 어제 19:30

    -- [동적 쿼리 검색용] '이프'라는 이름을 다른 날짜에 배치
    (DATEADD('DAY', -3, CURRENT_DATE), 1, 3), -- 3일 전 13:00
    (DATEADD('DAY', -5, CURRENT_DATE), 3, 5), -- 5일 전 16:00

    -- 페이징 및 카운트용 대량 데이터 (기존 데이터 샘플 활용)
    (DATEADD('DAY', -2, CURRENT_DATE), 2, 2),
    (DATEADD('DAY', -2, CURRENT_DATE), 3, 3),
    (DATEADD('DAY', -2, CURRENT_DATE), 1, 4),
    (DATEADD('DAY', -3, CURRENT_DATE), 2, 5),
    (DATEADD('DAY', -4, CURRENT_DATE), 4, 1),
    (DATEADD('DAY', -4, CURRENT_DATE), 1, 2),
    (DATEADD('DAY', -5, CURRENT_DATE), 3, 3),
    (DATEADD('DAY', -6, CURRENT_DATE), 5, 4),
    (DATEADD('DAY', -6, CURRENT_DATE), 1, 5),
    (DATEADD('DAY', -7, CURRENT_DATE), 2, 6),
    (DATEADD('DAY', -7, CURRENT_DATE), 4, 7),
    (DATEADD('DAY', -8, CURRENT_DATE), 1, 1),
    (DATEADD('DAY', -9, CURRENT_DATE), 5, 3),
    (DATEADD('DAY', -9, CURRENT_DATE), 1, 4),
    (DATEADD('DAY', -10, CURRENT_DATE), 2, 5),
    (DATEADD('DAY', -11, CURRENT_DATE), 3, 6),
    (DATEADD('DAY', -11, CURRENT_DATE), 1, 7);

INSERT INTO reservation_entry (name, reservation_id, status, created_at)
VALUES ('두둠', 1, 'RESERVED', CURRENT_TIMESTAMP),
       ('이프', 2, 'RESERVED', CURRENT_TIMESTAMP),
       ('이프', 3, 'RESERVED', CURRENT_TIMESTAMP),
       ('이프', 4, 'RESERVED', CURRENT_TIMESTAMP),
       ('도윤', 5, 'RESERVED', CURRENT_TIMESTAMP),
       ('서윤', 6, 'RESERVED', CURRENT_TIMESTAMP),
       ('하준', 7, 'RESERVED', CURRENT_TIMESTAMP),
       ('지우', 8, 'RESERVED', CURRENT_TIMESTAMP),
       ('시우', 9, 'RESERVED', CURRENT_TIMESTAMP),
       ('하윤', 10, 'RESERVED', CURRENT_TIMESTAMP),
       ('예준', 11, 'RESERVED', CURRENT_TIMESTAMP),
       ('서준', 12, 'RESERVED', CURRENT_TIMESTAMP),
       ('지안', 13, 'RESERVED', CURRENT_TIMESTAMP),
       ('수아', 14, 'RESERVED', CURRENT_TIMESTAMP),
       ('이준', 15, 'RESERVED', CURRENT_TIMESTAMP),
       ('서아', 16, 'RESERVED', CURRENT_TIMESTAMP),
       ('하린', 17, 'RESERVED', CURRENT_TIMESTAMP),
       ('지민', 18, 'RESERVED', CURRENT_TIMESTAMP),
       ('윤우', 19, 'RESERVED', CURRENT_TIMESTAMP),
       ('채원', 20, 'RESERVED', CURRENT_TIMESTAMP),
       ('은우', 21, 'RESERVED', CURRENT_TIMESTAMP);

-- 예약시간 데이터
INSERT INTO reservation_time (start_at, finish_at)
VALUES ('10:00', '11:00'),
       ('11:00', '12:00'),
       ('12:00', '13:00'),
       ('13:00', '14:00'),
       ('14:00', '15:00'),
       ('15:00', '16:00'),
       ('16:00', '17:00'),
       ('17:00', '18:00');

-- 테마 데이터
INSERT INTO theme (name, description, image_url)
VALUES ('월야애담', '강남점의 대표 한국 전통 호러 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%EC%9B%94%EC%95%BC__sizedown.png'),
       ('살랑살랑 연구소', '강남점의 산뜻한 추리/장치형 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%EC%82%B4%EB%9E%91__sizedown.png'),
       ('그카지말라캤자나', '강남점의 코믹한 사투리 감성 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%EA%B7%B8%EC%B9%B4%EC%A7%80__sizedown.png'),
       ('홀리데이', '홍대점의 대표 감성 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%ED%99%80%EB%A6%AC%EB%8D%B0%EC%9D%B4_sizedown.png'),
       ('삐릿뽀', '홍대점의 명랑한 추억 감성 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%EC%82%90%EB%A6%BF%EB%BD%80__sizedown.png'),
       ('고백', '홍대점의 인생테마로 평가받는 감성 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%EA%B3%A0%EB%B0%B1__sizedown.png'),
       ('머니머니패키지', '돈을 둘러싼 코믹 활극 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%EB%A8%B8%EB%8B%88%EB%A8%B8%EB%8B%88%ED%8C%A8%ED%82%A4%EC%A7%80_sizedown.png'),
       ('셜록전화', '셜록 홈즈를 모티브로 한 추리 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%EC%85%9C%EB%A1%9D%EC%A0%84%ED%99%94__size%20down.png'),
       ('파파라치', '특종을 좇는 파파라치가 되어 보는 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%ED%8C%8C%ED%8C%8C%EB%9D%BC%EC%B9%98_size%20down.png'),
       ('정신병동', '오싹한 분위기의 호러 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%EC%A0%95%EC%8B%A0%EB%B3%91%EB%8F%99_size%20down.png'),
       ('신비의숲', '환상적인 숲 속을 모험하는 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%EC%8B%A0%EB%B9%84%EC%9D%98%EC%88%B2__size%20down.png'),
       ('난쟁이', '동화 속 세계관의 추리 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%EB%82%9C%EC%9F%81%EC%9D%B4_sizedown.png'),
       ('산장', '외딴 산장에서 펼쳐지는 미스터리 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%EC%82%B0%EC%9E%A5_size%20down.png'),
       ('혜화잡화점', '소소한 잡화점에서 시작되는 이야기 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%ED%98%9C%ED%99%94%EC%9E%A1%ED%99%94%EC%A0%90__sizedown.png'),
       ('HAVE A GOOD TAPE', '카세트테이프를 둘러싼 감성 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/haveagoodtape_size%20down.png'),
       ('AFTERLIFE', '사후 세계를 다룬 분위기 있는 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/afterlife_size%20down.png'),
       ('아야코', '일본풍 호러 감성의 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%EC%95%84%EC%95%BC%EC%BD%94_size%20down.png'),
       ('괴록', '기괴한 기록을 추적하는 호러 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%EA%B4%B4%EB%A1%9D_size%20down.png'),
       ('투투 어드벤쳐', '활기찬 어드벤처 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/%ED%88%AC%ED%88%AC%20%EC%96%B4%EB%93%9C%EB%B2%A4%EC%B3%90_size%20down.png'),
       ('NOSTALGIA', '추억을 자극하는 감성 테마.',
        'https://d1kqa23lh2nxjx.cloudfront.net/file/theme_info/nostalgia_sizedown.png');

-- 예약 데이터
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES
    -- 이번 주: 2026-05-25 ~ 2026-05-31
    ('user1', '2026-05-25', 1, 1),
    ('user2', '2026-05-25', 2, 1),
    ('user6', '2026-05-26', 1, 1),
    ('user1', '2026-05-26', 3, 6),
    ('user2', '2026-05-27', 3, 6),
    ('user1', '2026-05-27', 4, 4),
    ('user2', '2026-05-28', 4, 4),
    ('user1', '2026-05-28', 5, 5),
    ('user2', '2026-05-29', 5, 5),
    ('user1', '2026-05-29', 6, 2),
    ('user1', '2026-05-30', 7, 8),
    ('user1', '2026-05-30', 8, 3),
    ('user1', '2026-05-31', 1, 10),
    ('user1', '2026-05-31', 2, 17),
    -- 다음 주: 2026-06-01 ~ 2026-06-07
    ('user3', '2026-06-01', 1, 1),
    ('user4', '2026-06-01', 2, 1),
    ('user5', '2026-06-02', 1, 1),
    ('user3', '2026-06-02', 3, 6),
    ('user4', '2026-06-03', 3, 6),
    ('user5', '2026-06-03', 4, 6),
    ('user3', '2026-06-04', 4, 4),
    ('user4', '2026-06-04', 5, 4),
    ('user3', '2026-06-05', 5, 5),
    ('user4', '2026-06-05', 6, 5),
    ('user2', '2026-06-06', 6, 2),
    ('user3', '2026-06-06', 7, 2),
    ('user2', '2026-06-07', 7, 8),
    ('user3', '2026-06-07', 8, 8),
    ('user2', '2026-06-01', 8, 3),
    ('user1', '2026-06-02', 3, 7),
    ('user2', '2026-06-03', 1, 10),
    ('user2', '2026-06-04', 2, 17),
    ('user1', '2026-06-05', 4, 11),
    ('user1', '2026-06-06', 5, 20),
    ('user1', '2026-06-07', 8, 15);

INSERT INTO reservation_payment
    (reservation_id, order_id, payment_key, amount, idempotency_key, status)
SELECT id,
       CAST(RANDOM_UUID() AS VARCHAR),
       NULL,
       10000,
       CAST(RANDOM_UUID() AS VARCHAR),
       'PENDING'
FROM reservation;

-- 예약 대기 데이터
INSERT INTO waiting (name, date, time_id, theme_id)
VALUES ('waiting1', '2026-05-25', 1, 1),
       ('waiting2', '2026-05-25', 1, 1),
       ('waiting3', '2026-05-26', 3, 6),
       ('waiting4', '2026-05-26', 3, 6),
       ('waiting5', '2026-05-28', 5, 5),
       ('waiting6', '2026-05-31', 2, 17),
       ('waiting7', '2026-06-01', 1, 1),
       ('waiting8', '2026-06-01', 1, 1),
       ('waiting9', '2026-06-03', 4, 6),
       ('waiting10', '2026-06-05', 4, 11),
       ('waiting11', '2026-06-07', 8, 15);

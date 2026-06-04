INSERT INTO store (id, name)
VALUES (1, '강남점');

INSERT INTO reservation_time (id, start_at)
VALUES (1, '10:00');

INSERT INTO theme (id, name, description, img_url)
VALUES (1, '이든의 공포 하우스', '이든이 귀신으로 나오는 공포 테마',
        'https://images.example.com/themes/horror-house.jpg');

INSERT INTO member (id, email, password, name)
VALUES (1, 'brown@email.com', 'password', '브라운'),
       (2, 'jungkong@email.com', 'password', '정콩이'),
       (3, 'honggu@email.com', 'password', '홍구'),
       (4, 'eden@email.com', 'password', '이든');

INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
VALUES (1, 4, '2026-12-01', 1, 1, 1),
       (2, 4, '2026-12-02', 1, 1, 1),
       (3, 4, '2026-12-03', 1, 1, 1),
       (4, 4, '2026-12-04', 1, 1, 1);

INSERT INTO reservation_wait (id, reservation_id, member_id, created_at)
VALUES (1, 1, 2, '2026-05-27 12:00:01'),
       (2, 1, 1, '2026-05-27 12:00:05'),
       (3, 1, 3, '2026-05-27 12:00:10'),
       (4, 2, 1, '2026-05-27 13:00:00'),
       (5, 2, 2, '2026-05-27 13:00:05'),
       (6, 3, 1, '2026-05-27 14:00:00'),
       (7, 4, 2, '2026-05-27 15:00:00'),
       (8, 4, 3, '2026-05-27 15:00:05');

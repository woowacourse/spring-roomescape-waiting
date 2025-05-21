INSERT INTO member(member_id, name, email, password, role)
VALUES (1, '훌라', 'test@test.com', 'test', 'USER'),
       (2, '어드민', 'admin@admin.com', 'admin', 'ADMIN');

INSERT INTO theme (theme_id, name, description, thumbnail)
VALUES (1, '공포의 방', '소름 끼치는 공포 테마', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       (2, '추리의 방', '논리력으로 푸는 추리 테마', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       (3, '시간 여행자', '과거와 미래를 넘나드는 방 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time (time_id, start_at)
VALUES (1, '10:00'),
       (2, '12:00'),
       (3, '14:00'),
       (4, '16:00');

INSERT INTO reservation(reservation_id, member_id, date, time_id, theme_id, status)
VALUES (1, 1, '2025-07-01', 1, 1, 'RESERVED'),
    (2, 2, '2025-07-01', 1, 1, 'WAIT'),
    (3, 1, '2025-07-01', 2, 1, 'RESERVED'),
    (4, 2, '2025-07-01', 2, 1, 'WAIT'),
    (5, 1, '2025-07-01', 1, 2, 'RESERVED'),
    (6, 2, '2025-07-01', 1, 2, 'WAIT'),
    (7, 1, '2025-07-01', 3, 3, 'RESERVED'),
    (8, 2, '2025-07-01', 3, 3, 'WAIT');


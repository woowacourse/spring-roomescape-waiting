-- 테마 데이터
INSERT INTO themes(name, description, thumbnail)
VALUES ('추리', '셜록 with Danny', 'image/thumbnail.png'),
       ('공포', '어둠 속의 비명', 'image/thumbnail.png'),
       ('모험', '잃어버린 도시', 'image/thumbnail.png'),
       ('SF', '우주 탈출 미션', 'image/thumbnail.png'),
       ('감성', '시간을 걷는 집', 'image/thumbnail.png'),
       ('판타지', '마법사의 유산', 'image/thumbnail.png'),
       ('역사', '고려 왕실의 비밀', 'image/thumbnail.png'),
       ('범죄', '은행 강도 사건', 'image/thumbnail.png'),
       ('스릴러', '잠입 작전', 'image/thumbnail.png'),
       ('코미디', '웃음 연구소', 'image/thumbnail.png'),
       ('로맨스', '잃어버린 편지', 'image/thumbnail.png'),
       ('논리', '퍼즐 마스터', 'image/thumbnail.png');

-- 예약 시간 데이터
INSERT INTO reservation_times(start_at)
VALUES ('08:00'),
       ('12:00'),
       ('14:00'),
       ('16:00'),
       ('18:00');

-- 회원 데이터
INSERT INTO members (name, email, password, member_role)
VALUES ('Admin', 'admin@gmail.com', '$2a$10$lsczSamG1eaxq1KE2ivIpek7hOx.uNkDILI5nQPqaWyiUQtay6Msa', 'ADMIN'),
       ('Regular', 'user@gmail.com', '$2a$10$lsczSamG1eaxq1KE2ivIpek7hOx.uNkDILI5nQPqaWyiUQtay6Msa', 'REGULAR'),
       ('Alice', 'alice@gmail.com', '$2a$10$lsczSamG1eaxq1KE2ivIpek7hOx.uNkDILI5nQPqaWyiUQtay6Msa', 'REGULAR'),
       ('Bob', 'bob@gmail.com', '$2a$10$lsczSamG1eaxq1KE2ivIpek7hOx.uNkDILI5nQPqaWyiUQtay6Msa', 'REGULAR'),
       ('Carol', 'carol@gmail.com', '$2a$10$lsczSamG1eaxq1KE2ivIpek7hOx.uNkDILI5nQPqaWyiUQtay6Msa', 'REGULAR'),
       ('Dave', 'dave@example.com', '$2a$10$lsczSamG1eaxq1KE2ivIpek7hOx.uNkDILI5nQPqaWyiUQtay6Msa', 'REGULAR'),
       ('Eve', 'eve@example.com', '$2a$10$lsczSamG1eaxq1KE2ivIpek7hOx.uNkDILI5nQPqaWyiUQtay6Msa', 'REGULAR'),
       ('Frank', 'frank@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Grace', 'grace@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Heidi', 'heidi@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Ivan', 'ivan@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Judy', 'judy@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Mallory', 'mallory@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Niaj', 'niaj@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Olivia', 'olivia@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Peggy', 'peggy@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Rupert', 'rupert@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Sybil', 'sybil@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Trent', 'trent@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Uma', 'uma@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Victor', 'victor@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Wendy', 'wendy@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Xander', 'xander@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Yvonne', 'yvonne@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Zack', 'zack@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Amy', 'amy@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Brian', 'brian@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Chloe', 'chloe@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Daniel', 'daniel@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR');

-- 예약 슬롯 데이터 (ReservationSlot) - 각 테마별 고유한 날짜/시간 조합
INSERT INTO reservation_slots(date, time_id, theme_id)
VALUES
    -- theme_id = 12 (논리) - 2025-05-10 (5개 슬롯)
    ('2025-05-10', 1, 12), -- ID: 1 (08:00)
    ('2025-05-10', 2, 12), -- ID: 2 (12:00)
    ('2025-05-10', 3, 12), -- ID: 3 (14:00)
    ('2025-05-10', 4, 12), -- ID: 4 (16:00)
    ('2025-05-10', 5, 12), -- ID: 5 (18:00)

    -- theme_id = 11 (로맨스) - 2025-05-10 (5개 슬롯)
    ('2025-05-10', 1, 11), -- ID: 6 (08:00)
    ('2025-05-10', 2, 11), -- ID: 7 (12:00)
    ('2025-05-10', 3, 11), -- ID: 8 (14:00)
    ('2025-05-10', 4, 11), -- ID: 9 (16:00)
    ('2025-05-10', 5, 11), -- ID: 10 (18:00)

    -- theme_id = 3 (모험) - 2025-05-10 (4개 슬롯)
    ('2025-05-10', 1, 3),  -- ID: 11 (08:00)
    ('2025-05-10', 2, 3),  -- ID: 12 (12:00)
    ('2025-05-10', 3, 3),  -- ID: 13 (14:00)
    ('2025-05-10', 4, 3),  -- ID: 14 (16:00)

    -- theme_id = 4 (SF) - 2025-05-11 (3개 슬롯)
    ('2025-05-11', 1, 4),  -- ID: 15 (08:00)
    ('2025-05-11', 2, 4),  -- ID: 16 (12:00)
    ('2025-05-11', 3, 4),  -- ID: 17 (14:00)

    -- theme_id = 5 (감성) - 2025-05-11 (2개 슬롯)
    ('2025-05-11', 1, 5),  -- ID: 18 (08:00)
    ('2025-05-11', 2, 5);  -- ID: 19 (12:00)

-- 예약 데이터 (Reservation) - 수정된 ReservationSlot ID에 맞게 조정
INSERT INTO reservations(reservation_slot_id, member_id, created_at)
VALUES
    -- ReservationSlot ID 1 (논리, 5/10, 08:00) - Alice(현재), Bob(대기), Carol(대기)
    (1, 3, '2025-05-09 10:00:00'),   -- Alice
    (1, 4, '2025-05-09 10:30:00'),   -- Bob
    (1, 5, '2025-05-09 11:00:00'),   -- Carol

    -- ReservationSlot ID 2 (논리, 5/10, 12:00) - Dave(현재), Eve(대기)
    (2, 6, '2025-05-09 11:30:00'),   -- Dave
    (2, 7, '2025-05-09 12:00:00'),   -- Eve

    -- ReservationSlot ID 3 (논리, 5/10, 14:00) - Frank(현재)
    (3, 8, '2025-05-09 12:30:00'),   -- Frank

    -- ReservationSlot ID 4 (논리, 5/10, 16:00) - Grace(현재), Heidi(대기)
    (4, 9, '2025-05-09 13:00:00'),   -- Grace
    (4, 10, '2025-05-09 13:30:00'),  -- Heidi

    -- ReservationSlot ID 5 (논리, 5/10, 18:00) - Ivan(현재), Judy(대기), Mallory(대기)
    (5, 11, '2025-05-09 14:00:00'),  -- Ivan
    (5, 12, '2025-05-09 14:30:00'),  -- Judy
    (5, 13, '2025-05-09 15:00:00'),  -- Mallory

    -- ReservationSlot ID 6 (로맨스, 5/10, 08:00) - Niaj(현재), Olivia(대기)
    (6, 14, '2025-05-09 15:30:00'),  -- Niaj
    (6, 15, '2025-05-09 16:00:00'),  -- Olivia

    -- ReservationSlot ID 7 (로맨스, 5/10, 12:00) - Peggy(현재)
    (7, 16, '2025-05-09 16:30:00'),  -- Peggy

    -- ReservationSlot ID 8 (로맨스, 5/10, 14:00) - Rupert(현재), Sybil(대기)
    (8, 17, '2025-05-09 17:00:00'),  -- Rupert
    (8, 18, '2025-05-09 17:30:00'),  -- Sybil

    -- ReservationSlot ID 9 (로맨스, 5/10, 16:00) - Trent(현재), Uma(대기)
    (9, 19, '2025-05-09 18:00:00'),  -- Trent
    (9, 20, '2025-05-09 18:30:00'),  -- Uma

    -- ReservationSlot ID 10 (로맨스, 5/10, 18:00) - Victor(현재)
    (10, 21, '2025-05-09 19:00:00'), -- Victor

    -- ReservationSlot ID 11 (모험, 5/10, 08:00) - Wendy(현재), Xander(대기)
    (11, 22, '2025-05-09 19:30:00'), -- Wendy
    (11, 23, '2025-05-09 20:00:00'), -- Xander

    -- ReservationSlot ID 12 (모험, 5/10, 12:00) - Yvonne(현재), Zack(대기)
    (12, 24, '2025-05-09 20:30:00'), -- Yvonne
    (12, 25, '2025-05-09 21:00:00'), -- Zack

    -- ReservationSlot ID 13 (모험, 5/10, 14:00) - Amy(현재)
    (13, 26, '2025-05-09 21:30:00'), -- Amy

    -- ReservationSlot ID 14 (모험, 5/10, 16:00) - Brian(현재), Chloe(대기)
    (14, 27, '2025-05-09 22:00:00'), -- Brian
    (14, 28, '2025-05-09 22:30:00'), -- Chloe

    -- 5월 11일 예약들
    -- ReservationSlot ID 15 (SF, 5/11, 08:00) - Daniel(현재), Alice(대기)
    (15, 29, '2025-05-10 10:00:00'), -- Daniel
    (15, 3, '2025-05-10 10:30:00'),  -- Alice (다른 예약)

    -- ReservationSlot ID 16 (SF, 5/11, 12:00) - Bob(현재), Carol(대기)
    (16, 4, '2025-05-10 11:00:00'),  -- Bob
    (16, 5, '2025-05-10 11:30:00'),  -- Carol

    -- ReservationSlot ID 17 (SF, 5/11, 14:00) - Dave(현재), Eve(대기), Frank(대기)
    (17, 6, '2025-05-10 12:00:00'),  -- Dave
    (17, 7, '2025-05-10 12:30:00'),  -- Eve
    (17, 8, '2025-05-10 13:00:00'),  -- Frank

    -- ReservationSlot ID 18 (감성, 5/11, 08:00) - Grace(현재)
    (18, 9, '2025-05-10 13:30:00'),  -- Grace

    -- ReservationSlot ID 19 (감성, 5/11, 12:00) - Heidi(현재), Ivan(대기)
    (19, 10, '2025-05-10 14:00:00'), -- Heidi
    (19, 11, '2025-05-10 14:30:00'); -- Ivan

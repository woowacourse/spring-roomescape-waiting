INSERT INTO theme(name, description, thumbnail)
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

INSERT INTO reservation_time(start_at)
VALUES ('08:00'),
       ('12:00'),
       ('14:00'),
       ('16:00'),
       ('18:00');

INSERT INTO member (name, email, password, member_role)
VALUES ('Admin', 'admin@gmail.com', '$2a$10$lsczSamG1eaxq1KE2ivIpek7hOx.uNkDILI5nQPqaWyiUQtay6Msa', 'ADMIN'),
       ('Regular', 'user@gmail.com', '$2a$10$lsczSamG1eaxq1KE2ivIpek7hOx.uNkDILI5nQPqaWyiUQtay6Msa', 'REGULAR'),
       ('Alice', 'alice@gmail.com', '$2a$10$lsczSamG1eaxq1KE2ivIpek7hOx.uNkDILI5nQPqaWyiUQtay6Msa', 'REGULAR'),
       ('Bob', 'bob@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Carol', 'carol@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Dave', 'dave@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
       ('Eve', 'eve@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'REGULAR'),
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

-- Reservation 테이블 데이터 (ReservationStatus 제거)
INSERT INTO reservation(date, time_id, theme_id)
VALUES
    -- theme_id = 12 (6회) - 2025-05-10
    ('2025-05-10', 1, 12),  -- ID: 1
    ('2025-05-10', 2, 12),  -- ID: 2
    ('2025-05-10', 3, 12),  -- ID: 3
    ('2025-05-10', 4, 12),  -- ID: 4
    ('2025-05-10', 5, 12),  -- ID: 5
    ('2025-05-10', 1, 12),  -- ID: 6

    -- theme_id = 11 (5회) - 2025-05-10
    ('2025-05-10', 2, 11),  -- ID: 7
    ('2025-05-10', 3, 11),  -- ID: 8
    ('2025-05-10', 4, 11),  -- ID: 9
    ('2025-05-10', 5, 11),  -- ID: 10
    ('2025-05-10', 1, 11),  -- ID: 11

    -- theme_id = 3 (4회) - 2025-05-10
    ('2025-05-10', 2, 3),   -- ID: 12
    ('2025-05-10', 3, 3),   -- ID: 13
    ('2025-05-10', 4, 3),   -- ID: 14
    ('2025-05-10', 5, 3),   -- ID: 15

    -- theme_id = 4 (3회) - 2025-05-11
    ('2025-05-11', 1, 4),   -- ID: 16
    ('2025-05-11', 2, 4),   -- ID: 17
    ('2025-05-11', 3, 4),   -- ID: 18

    -- theme_id = 5 (2회) - 2025-05-11
    ('2025-05-11', 4, 5),   -- ID: 19
    ('2025-05-11', 5, 5),   -- ID: 20

    -- theme_id = 6 (1회) - 2025-05-11
    ('2025-05-11', 1, 6),   -- ID: 21

    -- theme_id = 7 (1회) - 2025-05-11
    ('2025-05-11', 2, 7),   -- ID: 22

    -- theme_id = 8 (1회) - 2025-05-11
    ('2025-05-11', 3, 8),   -- ID: 23

    -- theme_id = 9 (1회) - 2025-05-11
    ('2025-05-11', 4, 9),   -- ID: 24

    -- theme_id = 10 (1회) - 2025-05-11
    ('2025-05-11', 5, 10),  -- ID: 25

    -- theme_id = 11 (1회) - 2025-05-11
    ('2025-05-11', 1, 11),  -- ID: 26

    -- theme_id = 12 (1회) - 2025-05-11
    ('2025-05-11', 2, 12);  -- ID: 27

-- Waiting 테이블 데이터 (WaitingStatus enum 값 사용)
INSERT INTO waiting(reservation_id, member_id, waiting_status, created_at)
VALUES
    -- Reservation ID 1 (테마12, 5/10, 시간1) - Alice, Bob, Carol이 대기
    (1, 3, 'WAITING', '2025-05-09 10:00:00'),
    (1, 4, 'WAITING', '2025-05-09 10:30:00'),
    (1, 5, 'WAITING', '2025-05-09 11:00:00'),

    -- Reservation ID 2 (테마12, 5/10, 시간2) - Dave, Eve가 대기
    (2, 6, 'WAITING', '2025-05-09 11:30:00'),
    (2, 7, 'WAITING', '2025-05-09 12:00:00'),

    -- Reservation ID 3 (테마12, 5/10, 시간3) - Frank 혼자 대기
    (3, 8, 'WAITING', '2025-05-09 12:30:00'),

    -- Reservation ID 4 (테마12, 5/10, 시간4) - Grace, Heidi가 대기
    (4, 9, 'WAITING', '2025-05-09 13:00:00'),
    (4, 10, 'WAITING', '2025-05-09 13:30:00'),

    -- Reservation ID 5 (테마12, 5/10, 시간5) - Ivan, Judy, Mallory가 대기
    (5, 11, 'WAITING', '2025-05-09 14:00:00'),
    (5, 12, 'WAITING', '2025-05-09 14:30:00'),
    (5, 13, 'WAITING', '2025-05-09 15:00:00'),

    -- Reservation ID 6 (테마12, 5/10, 시간1) - Niaj, Olivia가 대기
    (6, 14, 'WAITING', '2025-05-09 15:30:00'),
    (6, 15, 'WAITING', '2025-05-09 16:00:00'),

    -- Reservation ID 7 (테마11, 5/10, 시간2) - Peggy가 혼자 대기
    (7, 16, 'WAITING', '2025-05-09 16:30:00'),

    -- Reservation ID 8 (테마11, 5/10, 시간3) - Rupert, Sybil이 대기
    (8, 17, 'WAITING', '2025-05-09 17:00:00'),
    (8, 18, 'WAITING', '2025-05-09 17:30:00'),

    -- Reservation ID 9 (테마11, 5/10, 시간4) - Trent, Uma가 대기
    (9, 19, 'WAITING', '2025-05-09 18:00:00'),
    (9, 20, 'WAITING', '2025-05-09 18:30:00'),

    -- Reservation ID 10 (테마11, 5/10, 시간5) - Victor가 혼자 대기
    (10, 21, 'WAITING', '2025-05-09 19:00:00'),

    -- Reservation ID 11 (테마11, 5/10, 시간1) - Wendy, Xander가 대기
    (11, 22, 'WAITING', '2025-05-09 19:30:00'),
    (11, 23, 'WAITING', '2025-05-09 20:00:00'),

    -- Reservation ID 12 (테마3, 5/10, 시간2) - Yvonne, Zack이 대기
    (12, 24, 'WAITING', '2025-05-09 20:30:00'),
    (12, 25, 'WAITING', '2025-05-09 21:00:00'),

    -- Reservation ID 13 (테마3, 5/10, 시간3) - Amy가 혼자 대기
    (13, 26, 'WAITING', '2025-05-09 21:30:00'),

    -- Reservation ID 14 (테마3, 5/10, 시간4) - Brian, Chloe가 대기
    (14, 27, 'WAITING', '2025-05-09 22:00:00'),
    (14, 28, 'WAITING', '2025-05-09 22:30:00'),

    -- Reservation ID 15 (테마3, 5/10, 시간5) - Daniel이 혼자 대기
    (15, 29, 'WAITING', '2025-05-09 23:00:00'),

    -- 5월 11일 예약들의 대기자들
    -- Reservation ID 16 (테마4, 5/11, 시간1) - Alice가 또 다른 예약에 대기
    (16, 3, 'WAITING', '2025-05-10 10:00:00'),

    -- Reservation ID 17 (테마4, 5/11, 시간2) - Bob, Carol이 대기
    (17, 4, 'WAITING', '2025-05-10 10:30:00'),
    (17, 5, 'WAITING', '2025-05-10 11:00:00'),

    -- Reservation ID 18 (테마4, 5/11, 시간3) - Dave, Eve, Frank가 대기
    (18, 6, 'WAITING', '2025-05-10 11:30:00'),
    (18, 7, 'WAITING', '2025-05-10 12:00:00'),
    (18, 8, 'WAITING', '2025-05-10 12:30:00'),

    -- Reservation ID 19 (테마5, 5/11, 시간4) - Grace가 혼자 대기
    (19, 9, 'WAITING', '2025-05-10 13:00:00'),

    -- Reservation ID 20 (테마5, 5/11, 시간5) - Heidi, Ivan이 대기
    (20, 10, 'WAITING', '2025-05-10 13:30:00'),
    (20, 11, 'WAITING', '2025-05-10 14:00:00');

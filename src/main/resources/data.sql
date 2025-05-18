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
       ('User', 'user@gmail.com', '$2a$10$lsczSamG1eaxq1KE2ivIpek7hOx.uNkDILI5nQPqaWyiUQtay6Msa', 'USER'),
       ('Alice', 'alice@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Bob', 'bob@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Carol', 'carol@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Dave', 'dave@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Eve', 'eve@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Frank', 'frank@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Grace', 'grace@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Heidi', 'heidi@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Ivan', 'ivan@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Judy', 'judy@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Mallory', 'mallory@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Niaj', 'niaj@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Olivia', 'olivia@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Peggy', 'peggy@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Rupert', 'rupert@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Sybil', 'sybil@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Trent', 'trent@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Uma', 'uma@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Victor', 'victor@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Wendy', 'wendy@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Xander', 'xander@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Yvonne', 'yvonne@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Zack', 'zack@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Amy', 'amy@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Brian', 'brian@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Chloe', 'chloe@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER'),
       ('Daniel', 'daniel@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqF.W', 'USER');

INSERT INTO reservation(date, time_id, theme_id, member_id, reservation_status)
VALUES
    -- theme_id = 12 (6회)
    ('2025-05-10', 1, 12, 3, 'RESERVED'),  -- Alice
    ('2025-05-10', 2, 12, 4, 'RESERVED'),  -- Bob
    ('2025-05-10', 3, 12, 5, 'RESERVED'),  -- Carol
    ('2025-05-10', 4, 12, 6, 'RESERVED'),  -- Dave
    ('2025-05-10', 5, 12, 7, 'RESERVED'),  -- Eve
    ('2025-05-10', 1, 12, 8, 'RESERVED'),  -- Frank

    -- theme_id = 11 (5회)
    ('2025-05-10', 2, 11, 9, 'RESERVED'),  -- Grace
    ('2025-05-10', 3, 11, 10, 'RESERVED'), -- Heidi
    ('2025-05-10', 4, 11, 11, 'RESERVED'), -- Ivan
    ('2025-05-10', 5, 11, 12, 'RESERVED'), -- Judy
    ('2025-05-10', 1, 11, 13, 'RESERVED'), -- Mallory

    -- theme_id = 3 (4회)
    ('2025-05-10', 2, 3, 14, 'RESERVED'),  -- Niaj
    ('2025-05-10', 3, 3, 15, 'RESERVED'),  -- Olivia
    ('2025-05-10', 4, 3, 16, 'RESERVED'),  -- Peggy
    ('2025-05-10', 5, 3, 17, 'RESERVED'),  -- Rupert

    -- theme_id = 4 (3회)
    ('2025-05-11', 1, 4, 18, 'RESERVED'),  -- Sybil
    ('2025-05-11', 2, 4, 19, 'RESERVED'),  -- Trent
    ('2025-05-11', 3, 4, 20, 'RESERVED'),  -- Uma

    -- theme_id = 5 (2회)
    ('2025-05-11', 4, 5, 21, 'RESERVED'),  -- Victor
    ('2025-05-11', 5, 5, 22, 'RESERVED'),  -- Wendy

    -- theme_id = 6 (1회)
    ('2025-05-11', 1, 6, 23, 'RESERVED'),  -- Xander

    -- theme_id = 7 (1회)
    ('2025-05-11', 2, 7, 24, 'RESERVED'),  -- Yvonne

    -- theme_id = 8 (1회)
    ('2025-05-11', 3, 8, 25, 'RESERVED'),  -- Zack

    -- theme_id = 9 (1회)
    ('2025-05-11', 4, 9, 26, 'RESERVED'),  -- Amy

    -- theme_id = 10 (1회)
    ('2025-05-11', 5, 10, 27, 'RESERVED'), -- Brian

    -- theme_id = 11 (1회)
    ('2025-05-11', 1, 11, 28, 'RESERVED'), -- Chloe

    -- theme_id = 12 (1회)
    ('2025-05-11', 2, 12, 29, 'RESERVED'); -- Daniel


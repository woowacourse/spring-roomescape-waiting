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
VALUES
    ('Admin', 'admin@gmail.com', '$2a$10$lsczSamG1eaxq1KE2ivIpek7hOx.uNkDILI5nQPqaWyiUQtay6Msa', 'ADMIN'),
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

INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES
    -- theme_id = 12 (6회)
    ('2025-05-10', 1, 12, 3), -- Alice
    ('2025-05-10', 2, 12, 4), -- Bob
    ('2025-05-10', 3, 12, 5), -- Carol
    ('2025-05-10', 4, 12, 6), -- Dave
    ('2025-05-10', 5, 12, 7), -- Eve
    ('2025-05-10', 1, 12, 8), -- Frank

    -- theme_id = 11 (5회)
    ('2025-05-10', 2, 11, 9), -- Grace
    ('2025-05-10', 3, 11, 10), -- Heidi
    ('2025-05-10', 4, 11, 11), -- Ivan
    ('2025-05-10', 5, 11, 12), -- Judy
    ('2025-05-10', 1, 11, 13), -- Mallory

    -- theme_id = 3 (4회)
    ('2025-05-10', 2, 3, 14), -- Niaj
    ('2025-05-10', 3, 3, 15), -- Olivia
    ('2025-05-10', 4, 3, 16), -- Peggy
    ('2025-05-10', 5, 3, 17), -- Rupert

    -- theme_id = 4 (3회)
    ('2025-05-11', 1, 4, 18), -- Sybil
    ('2025-05-11', 2, 4, 19), -- Trent
    ('2025-05-11', 3, 4, 20), -- Uma

    -- theme_id = 5 (2회)
    ('2025-05-11', 4, 5, 21), -- Victor
    ('2025-05-11', 5, 5, 22), -- Wendy

    -- theme_id = 6 (1회)
    ('2025-05-11', 1, 6, 23), -- Xander

    -- theme_id = 7 (1회)
    ('2025-05-11', 2, 7, 24), -- Yvonne

    -- theme_id = 8 (1회)
    ('2025-05-11', 3, 8, 25), -- Zack

    -- theme_id = 9 (1회)
    ('2025-05-11', 4, 9, 26), -- Amy

    -- theme_id = 10 (1회)
    ('2025-05-11', 5, 10, 27), -- Brian

    -- theme_id = 11 (1회)
    ('2025-05-11', 1, 11, 28), -- Chloe

    -- theme_id = 12 (1회)
    ('2025-05-11', 2, 12, 29); -- Daniel


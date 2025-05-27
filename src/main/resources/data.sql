-- member
INSERT INTO member (name, role, email, password)
VALUES ('관리자', 'ADMIN', 'admin@email.com', 'password'),
       ('회원1', 'USER', 'member1@email.com', '1234'),
       ('회원2', 'USER', 'member2@email.com', '1234'),
       ('회원3', 'USER', 'member3@email.com', '1234'),
       ('회원4', 'USER', 'member4@email.com', '1234'),
       ('회원5', 'USER', 'member5@email.com', '1234'),
       ('회원6', 'USER', 'member6@email.com', '1234'),
       ('회원7', 'USER', 'member7@email.com', '1234'),
       ('회원8', 'USER', 'member8@email.com', '1234'),
       ('회원9', 'USER', 'member9@email.com', '1234'),
       ( '회원10', 'USER', 'member10@email.com', '1234');

-- reservation_time
INSERT INTO reservation_time (start_at)
VALUES ('09:00'),
       ('10:00'),
       ('11:00'),
       ('12:00'),
       ('13:00'),
       ('14:00'),
       ('15:00'),
       ('16:00'),
       ('17:00'),
       ( '18:00'),
       ( '19:00'),
       ( '20:00'),
       ( '21:00'),
       ( '22:00'),
       ( '23:00');

-- theme
INSERT INTO theme (name, description, thumbnail)
VALUES ('우테코 레벨1 탈출', '우테코 레벨1 탈출 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('우테코 레벨2 탈출', '우테코 레벨2 탈출 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('우테코 레벨3 탈출', '우테코 레벨3 탈출 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('우테코 레벨4 탈출', '우테코 레벨4 탈출 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('우테코 레벨5 탈출', '우테코 레벨5 탈출 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('우테코 레벨6 탈출', '우테코 레벨6 탈출 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('우테코 레벨7 탈출', '우테코 레벨7 탈출 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('우테코 레벨8 탈출', '우테코 레벨8 탈출 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('우테코 레벨9 탈출', '우테코 레벨9 탈출 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ( '우테코 레벨10 탈출', '우테코 레벨10 탈출 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ( '우테코 레벨11 탈출', '우테코 레벨11 탈출 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

-- reservation
INSERT INTO reservation (date, time_id, theme_id, member_id, status, created_at)
VALUES
-- theme_id 1: 10건
(CURRENT_DATE - 3, 13, 1, 2, 'RESERVED', NOW()),
(CURRENT_DATE - 3, 12, 1, 3, 'RESERVED', NOW()),
(CURRENT_DATE - 3, 11, 1, 4, 'RESERVED', NOW()),
(CURRENT_DATE - 3, 10, 1, 5, 'RESERVED', NOW()),
(CURRENT_DATE - 3, 9, 1, 6, 'RESERVED', NOW()),
(CURRENT_DATE - 3, 8, 1, 7, 'RESERVED', NOW()),
(CURRENT_DATE - 3, 7, 1, 8, 'RESERVED', NOW()),
(CURRENT_DATE - 3, 6, 1, 9, 'RESERVED', NOW()),
(CURRENT_DATE - 3, 5, 1, 1, 'RESERVED', NOW()),
(CURRENT_DATE - 3, 4, 1, 1, 'RESERVED', NOW()),

-- theme_id 2: 9건
(CURRENT_DATE - 2, 11, 2, 2, 'RESERVED', NOW()),
(CURRENT_DATE - 2, 10, 2, 3, 'RESERVED', NOW()),
(CURRENT_DATE - 2, 9, 2, 4, 'RESERVED', NOW()),
(CURRENT_DATE - 2, 8, 2, 5, 'RESERVED', NOW()),
(CURRENT_DATE - 2, 7, 2, 6, 'RESERVED', NOW()),
(CURRENT_DATE - 2, 6, 2, 7, 'RESERVED', NOW()),
(CURRENT_DATE - 2, 5, 2, 8, 'RESERVED', NOW()),
(CURRENT_DATE - 2, 4, 2, 9, 'RESERVED', NOW()),
(CURRENT_DATE - 2, 3, 2, 1, 'RESERVED', NOW()),

-- theme_id 3: 8건
(CURRENT_DATE - 1, 9, 3, 2, 'RESERVED', NOW()),
(CURRENT_DATE - 1, 8, 3, 3, 'RESERVED', NOW()),
(CURRENT_DATE - 1, 7, 3, 4, 'RESERVED', NOW()),
(CURRENT_DATE - 1, 6, 3, 5, 'RESERVED', NOW()),
(CURRENT_DATE - 1, 5, 3, 6, 'RESERVED', NOW()),
(CURRENT_DATE - 1, 4, 3, 7, 'RESERVED', NOW()),
(CURRENT_DATE - 1, 3, 3, 8, 'RESERVED', NOW()),
(CURRENT_DATE - 1, 2, 3, 9, 'RESERVED', NOW()),

-- theme_id 4: 7건
(CURRENT_DATE - 7, 7, 4, 2, 'RESERVED', NOW()),
(CURRENT_DATE - 7, 6, 4, 3, 'RESERVED', NOW()),
(CURRENT_DATE - 7, 5, 4, 4, 'RESERVED', NOW()),
(CURRENT_DATE - 7, 4, 4, 5, 'RESERVED', NOW()),
(CURRENT_DATE - 7, 3, 4, 6, 'RESERVED', NOW()),
(CURRENT_DATE - 7, 2, 4, 7, 'RESERVED', NOW()),
(CURRENT_DATE - 7, 1, 4, 8, 'RESERVED', NOW()),

-- theme_id 5: 6건
(CURRENT_DATE - 6, 6, 5, 2, 'RESERVED', NOW()),
(CURRENT_DATE - 6, 5, 5, 3, 'RESERVED', NOW()),
(CURRENT_DATE - 6, 4, 5, 4, 'RESERVED', NOW()),
(CURRENT_DATE - 6, 3, 5, 5, 'RESERVED', NOW()),
(CURRENT_DATE - 6, 2, 5, 6, 'RESERVED', NOW()),
(CURRENT_DATE - 6, 1, 5, 7, 'RESERVED', NOW()),

-- theme_id 6: 5건
(CURRENT_DATE - 5, 5, 6, 2, 'RESERVED', NOW()),
(CURRENT_DATE - 5, 4, 6, 3, 'RESERVED', NOW()),
(CURRENT_DATE - 5, 3, 6, 4, 'RESERVED', NOW()),
(CURRENT_DATE - 5, 2, 6, 5, 'RESERVED', NOW()),
(CURRENT_DATE - 5, 1, 6, 6, 'RESERVED', NOW()),

-- theme_id 7: 4건
(CURRENT_DATE - 4, 4, 7, 2, 'RESERVED', NOW()),
(CURRENT_DATE - 4, 3, 7, 3, 'RESERVED', NOW()),
(CURRENT_DATE - 4, 2, 7, 4, 'RESERVED', NOW()),
(CURRENT_DATE - 4, 1, 7, 5, 'RESERVED', NOW()),

-- theme_id 8: 3건
(CURRENT_DATE - 3, 3, 8, 2, 'RESERVED', NOW()),
(CURRENT_DATE - 3, 2, 8, 3, 'RESERVED', NOW()),
(CURRENT_DATE - 3, 1, 8, 4, 'RESERVED', NOW()),

-- theme_id 9: 2건
(CURRENT_DATE - 2, 2, 9, 2, 'RESERVED', NOW()),
(CURRENT_DATE - 2, 1, 9, 3, 'RESERVED', NOW()),

-- theme_id 10: 1건
(CURRENT_DATE - 1, 1, 10, 2, 'RESERVED', NOW());


-- theme_id 1: 예약 10건, waiting 2명씩
INSERT INTO waiting (date, member_id, theme_id, time_id, created_at)
VALUES
-- (date, member_id, theme_id, time_id, created_at)
(CURRENT_DATE - 3, 5, 1, 13, NOW()),
(CURRENT_DATE - 3, 6, 1, 13, NOW()),

(CURRENT_DATE - 3, 7, 1, 12, NOW()),
(CURRENT_DATE - 3, 8, 1, 12, NOW()),

(CURRENT_DATE - 3, 9, 1, 11, NOW()),
(CURRENT_DATE - 3, 10, 1, 11, NOW()),

(CURRENT_DATE - 3, 11, 1, 10, NOW()),
(CURRENT_DATE - 3, 5, 1, 10, NOW()),

(CURRENT_DATE - 3, 6, 1, 9, NOW()),
(CURRENT_DATE - 3, 7, 1, 9, NOW()),

(CURRENT_DATE - 3, 8, 1, 8, NOW()),
(CURRENT_DATE - 3, 9, 1, 8, NOW()),

(CURRENT_DATE - 3, 10, 1, 7, NOW()),
(CURRENT_DATE - 3, 11, 1, 7, NOW()),

(CURRENT_DATE - 3, 5, 1, 6, NOW()),
(CURRENT_DATE - 3, 6, 1, 6, NOW()),

(CURRENT_DATE - 3, 7, 1, 5, NOW()),
(CURRENT_DATE - 3, 8, 1, 5, NOW()),

(CURRENT_DATE - 3, 9, 1, 4, NOW()),
(CURRENT_DATE - 3, 10, 1, 4, NOW());


INSERT INTO waiting (date, member_id, theme_id, time_id, created_at)
VALUES
    (CURRENT_DATE - 3, 1, 1, 13, NOW()),
    (CURRENT_DATE - 3, 2, 1, 13, NOW()),

    (CURRENT_DATE - 3, 3, 1, 12, NOW()),
    (CURRENT_DATE - 3, 4, 1, 12, NOW()),

    (CURRENT_DATE - 3, 5, 1, 11, NOW()),
    (CURRENT_DATE - 3, 6, 1, 11, NOW()),

    (CURRENT_DATE - 3, 7, 1, 10, NOW()),
    (CURRENT_DATE - 3, 8, 1, 10, NOW()),

    (CURRENT_DATE - 3, 9, 1, 9, NOW()),
    (CURRENT_DATE - 3, 10, 1, 9, NOW()),

    (CURRENT_DATE - 3, 11, 1, 8, NOW()),
    (CURRENT_DATE - 3, 1, 1, 8, NOW()),

    (CURRENT_DATE - 3, 2, 1, 7, NOW()),
    (CURRENT_DATE - 3, 3, 1, 7, NOW()),

    (CURRENT_DATE - 3, 4, 1, 6, NOW()),
    (CURRENT_DATE - 3, 5, 1, 6, NOW()),

    (CURRENT_DATE - 3, 6, 1, 5, NOW()),
    (CURRENT_DATE - 3, 7, 1, 5, NOW()),

    (CURRENT_DATE - 3, 8, 1, 4, NOW()),
    (CURRENT_DATE - 3, 9, 1, 4, NOW());


INSERT INTO waiting (date, member_id, theme_id, time_id, created_at)
VALUES
    (CURRENT_DATE - 2, 10, 2, 11, NOW()),
    (CURRENT_DATE - 2, 11, 2, 11, NOW()),

    (CURRENT_DATE - 2, 1, 2, 10, NOW()),
    (CURRENT_DATE - 2, 2, 2, 10, NOW()),

    (CURRENT_DATE - 2, 3, 2, 9, NOW()),
    (CURRENT_DATE - 2, 4, 2, 9, NOW()),

    (CURRENT_DATE - 2, 5, 2, 8, NOW()),
    (CURRENT_DATE - 2, 6, 2, 8, NOW()),

    (CURRENT_DATE - 2, 7, 2, 7, NOW()),
    (CURRENT_DATE - 2, 8, 2, 7, NOW()),

    (CURRENT_DATE - 2, 9, 2, 6, NOW()),
    (CURRENT_DATE - 2, 10, 2, 6, NOW()),

    (CURRENT_DATE - 2, 11, 2, 5, NOW()),
    (CURRENT_DATE - 2, 1, 2, 5, NOW()),

    (CURRENT_DATE - 2, 2, 2, 4, NOW()),
    (CURRENT_DATE - 2, 3, 2, 4, NOW()),

    (CURRENT_DATE - 2, 4, 2, 3, NOW()),
    (CURRENT_DATE - 2, 5, 2, 3, NOW());


INSERT INTO waiting (date, member_id, theme_id, time_id, created_at)
VALUES
    (CURRENT_DATE - 1, 6, 3, 9, NOW()),
    (CURRENT_DATE - 1, 7, 3, 9, NOW()),

    (CURRENT_DATE - 1, 8, 3, 8, NOW()),
    (CURRENT_DATE - 1, 9, 3, 8, NOW()),

    (CURRENT_DATE - 1, 10, 3, 7, NOW()),
    (CURRENT_DATE - 1, 11, 3, 7, NOW()),

    (CURRENT_DATE - 1, 1, 3, 6, NOW()),
    (CURRENT_DATE - 1, 2, 3, 6, NOW()),

    (CURRENT_DATE - 1, 3, 3, 5, NOW()),
    (CURRENT_DATE - 1, 4, 3, 5, NOW()),

    (CURRENT_DATE - 1, 5, 3, 4, NOW()),
    (CURRENT_DATE - 1, 6, 3, 4, NOW()),

    (CURRENT_DATE - 1, 7, 3, 3, NOW()),
    (CURRENT_DATE - 1, 8, 3, 3, NOW()),

    (CURRENT_DATE - 1, 9, 3, 2, NOW()),
    (CURRENT_DATE - 1, 10, 3, 2, NOW());




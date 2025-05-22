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
INSERT INTO reservation (date, time_id, theme_id, member_id, status)
VALUES
-- theme_id 1: 10건
(CURRENT_DATE - 3, 13, 1, 2, 'RESERVED'),
(CURRENT_DATE - 3, 12, 1, 3, 'RESERVED'),
(CURRENT_DATE - 3, 11, 1, 4, 'RESERVED'),
(CURRENT_DATE - 3, 10, 1, 5, 'RESERVED'),
(CURRENT_DATE - 3, 9, 1, 6, 'RESERVED'),
(CURRENT_DATE - 3, 8, 1, 7, 'RESERVED'),
(CURRENT_DATE - 3, 7, 1, 8, 'RESERVED'),
(CURRENT_DATE - 3, 6, 1, 9, 'RESERVED'),
(CURRENT_DATE - 3, 5, 1, 1, 'RESERVED'),
(CURRENT_DATE - 3, 4, 1, 1, 'RESERVED'),

-- theme_id 2: 9건
(CURRENT_DATE - 2, 11, 2, 2, 'RESERVED'),
(CURRENT_DATE - 2, 10, 2, 3, 'RESERVED'),
(CURRENT_DATE - 2, 9, 2, 4, 'RESERVED'),
(CURRENT_DATE - 2, 8, 2, 5, 'RESERVED'),
(CURRENT_DATE - 2, 7, 2, 6, 'RESERVED'),
(CURRENT_DATE - 2, 6, 2, 7, 'RESERVED'),
(CURRENT_DATE - 2, 5, 2, 8, 'RESERVED'),
(CURRENT_DATE - 2, 4, 2, 9, 'RESERVED'),
(CURRENT_DATE - 2, 3, 2, 1, 'RESERVED'),

-- theme_id 3: 8건
(CURRENT_DATE - 1, 9, 3, 2, 'RESERVED'),
(CURRENT_DATE - 1, 8, 3, 3, 'RESERVED'),
(CURRENT_DATE - 1, 7, 3, 4, 'RESERVED'),
(CURRENT_DATE - 1, 6, 3, 5, 'RESERVED'),
(CURRENT_DATE - 1, 5, 3, 6, 'RESERVED'),
(CURRENT_DATE - 1, 4, 3, 7, 'RESERVED'),
(CURRENT_DATE - 1, 3, 3, 8, 'RESERVED'),
(CURRENT_DATE - 1, 2, 3, 9, 'RESERVED'),

-- theme_id 4: 7건
(CURRENT_DATE - 7, 7, 4, 2, 'RESERVED'),
(CURRENT_DATE - 7, 6, 4, 3, 'RESERVED'),
(CURRENT_DATE - 7, 5, 4, 4, 'RESERVED'),
(CURRENT_DATE - 7, 4, 4, 5, 'RESERVED'),
(CURRENT_DATE - 7, 3, 4, 6, 'RESERVED'),
(CURRENT_DATE - 7, 2, 4, 7, 'RESERVED'),
(CURRENT_DATE - 7, 1, 4, 8, 'RESERVED'),

-- theme_id 5: 6건
(CURRENT_DATE - 6, 6, 5, 2, 'RESERVED'),
(CURRENT_DATE - 6, 5, 5, 3, 'RESERVED'),
(CURRENT_DATE - 6, 4, 5, 4, 'RESERVED'),
(CURRENT_DATE - 6, 3, 5, 5, 'RESERVED'),
(CURRENT_DATE - 6, 2, 5, 6, 'RESERVED'),
(CURRENT_DATE - 6, 1, 5, 7, 'RESERVED'),

-- theme_id 6: 5건
(CURRENT_DATE - 5, 5, 6, 2, 'RESERVED'),
(CURRENT_DATE - 5, 4, 6, 3, 'RESERVED'),
(CURRENT_DATE - 5, 3, 6, 4, 'RESERVED'),
(CURRENT_DATE - 5, 2, 6, 5, 'RESERVED'),
(CURRENT_DATE - 5, 1, 6, 6, 'RESERVED'),

-- theme_id 7: 4건
(CURRENT_DATE - 4, 4, 7, 2, 'RESERVED'),
(CURRENT_DATE - 4, 3, 7, 3, 'RESERVED'),
(CURRENT_DATE - 4, 2, 7, 4, 'RESERVED'),
(CURRENT_DATE - 4, 1, 7, 5, 'RESERVED'),

-- theme_id 8: 3건
(CURRENT_DATE - 3, 3, 8, 2, 'RESERVED'),
(CURRENT_DATE - 3, 2, 8, 3, 'RESERVED'),
(CURRENT_DATE - 3, 1, 8, 4, 'RESERVED'),

-- theme_id 9: 2건
(CURRENT_DATE - 2, 2, 9, 2, 'RESERVED'),
(CURRENT_DATE - 2, 1, 9, 3, 'RESERVED'),
(CURRENT_DATE + 1, 2, 9, 4, 'RESERVED'),

-- theme_id 10: 1건
(CURRENT_DATE - 1, 1, 10, 2, 'RESERVED'),
(CURRENT_DATE + 2, 1, 10, 2, 'RESERVED');

-- waiting
INSERT INTO waiting (reservation_id, member_id, rank)
VALUES
(55, 5, 1),
(55, 6, 2),
(55, 7, 3),

(57, 3, 1),
(57, 4, 2);
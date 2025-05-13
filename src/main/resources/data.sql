DELETE
FROM reservation;
DELETE
FROM reservation_time;
DELETE
FROM theme;
DELETE
FROM member;

-- member 데이터
INSERT INTO member(name, email, password, role)
VALUES ('훌라', 'test@test.com', 'test', 'USER'),
       ('어드민', 'admin@admin.com', 'admin', 'ADMIN');

-- theme 데이터
INSERT INTO theme (name, description, thumbnail)
VALUES ('공포의 방', '소름 끼치는 공포 테마', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('추리의 방', '논리력으로 푸는 추리 테마', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('시간 여행자', '과거와 미래를 넘나드는 방 탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

-- reservation_time 데이터
INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('12:00'),
       ('14:00'),
       ('16:00');

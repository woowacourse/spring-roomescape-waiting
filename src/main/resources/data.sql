-- reservation_time 테이블 초기 데이터
INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('12:00'),
       ('14:00'),
       ('16:00');

-- theme 테이블 초기 데이터
INSERT INTO theme (name, description, thumbnail)
VALUES ('공포의 우테코', '우테코에서 벌어지는 미스터리를 풀어라', '/image/horror.png'),
       ('시간 도둑', '스릴 넘치는 우테코 미션', '/image/time.png'),
       ('우테코 학교', '잃어버린 DDD를 찾아라', '/image/school.png');

-- 회원 테이블 초기 데이터
INSERT INTO member (name, email, password, role)
VALUES ('웨이드', 'email1@naver.com', '1234', 'USER');

INSERT INTO member (name, email, password, role)
VALUES ('모코', 'email2@naver.com', '1234', 'USER');

INSERT INTO member (name, email, password, role)
VALUES ('리사', 'email3@naver.com', '1234', 'USER');

INSERT INTO member (name, email, password, role)
VALUES ('검프', 'email4@naver.com', '1234', 'USER');

INSERT INTO member (name, email, password, role)
VALUES ('어드민', 'admin@naver.com', '1234', 'ADMIN');

-- reservation 테이블 초기 데이터
INSERT INTO reservation (date, time_id, theme_id, member_id, status)
VALUES ('2025-06-01', 1, 2, 1, 'CONFIRMED'),
       ('2025-06-01', 2, 1, 2, 'CONFIRMED'),
       ('2025-06-02', 3, 3, 3, 'CANCELED'),
       ('2025-06-02', 1, 3, 4, 'CONFIRMED');

INSERT INTO reservation_waiting (date, time_id, theme_id, member_id, created_at, status)
VALUES ('2025-06-01', 1, 2, 4, CURRENT_TIMESTAMP, PENDING),
       ('2025-06-01', 1, 2, 3, DATEADD('HOUR', 1, CURRENT_TIMESTAMP), PENDING),
       ('2025-06-01', 2, 1, 3, CURRENT_TIMESTAMP, PENDING),
       ('2025-06-02', 3, 3, 2, CURRENT_TIMESTAMP, DENIED),
       ('2025-06-02', 1, 3, 1, CURRENT_TIMESTAMP, PENDING);

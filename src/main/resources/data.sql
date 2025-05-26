-- 사용자
INSERT INTO member (id, name, email, password, role)
VALUES (1, '매트', 'matt@kakao.com', '1234', 'ADMIN');
INSERT INTO member (id, name, email, password, role)
VALUES (2, 'asd', 'asd@asd.com', 'asd', 'MEMBER');

-- 시간
INSERT INTO reservation_time (id, start_at)
VALUES (1, '10:00');
INSERT INTO reservation_time (id, start_at)
VALUES (2, '11:00');
INSERT INTO reservation_time (id, start_at)
VALUES (3, '12:00');

-- 테마
INSERT INTO theme (id, name, description, thumbnail)
VALUES (1, '레벨2 탈출', '우테코 레벨2를 탈출하는 내용입니다.',
        'https://techblog.woowahan.com/wp-content/uploads/img/2020-04-10/pobi.png');
INSERT INTO theme (id, name, description, thumbnail)
VALUES (2, '지하 감옥', '깊은 감옥에서 탈출하라!', 'https://truthfoundation.or.kr/media/images/68.width-1200.jpg');

-- RESERVED: 오래된 데이터 (2025-04-11 ~ 2025-04-20)
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (1, '2025-04-20', 2, 1, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (2, '2025-04-19', 3, 2, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (3, '2025-04-18', 1, 1, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (4, '2025-04-17', 2, 2, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (5, '2025-04-16', 3, 1, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (6, '2025-04-15', 1, 2, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (7, '2025-04-14', 2, 1, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (8, '2025-04-13', 3, 2, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (9, '2025-04-12', 1, 1, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (10, '2025-04-11', 2, 2, 1);

ALTER TABLE reservation_time
    ALTER COLUMN id RESTART WITH 4;

ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 3;

ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 11;

ALTER TABLE member
    ALTER COLUMN id RESTART WITH 3;

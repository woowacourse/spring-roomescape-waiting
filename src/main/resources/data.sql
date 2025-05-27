-- 사용자
INSERT INTO member (id, name, email, password, role)
VALUES (1, '찰리', '찰리@kakao.com', '1234', 'ADMIN');

INSERT INTO member (id, name, email, password, role)
VALUES (2, '포비', '포비@kakao.com', '1234', 'ADMIN');

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

INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (1, CURRENT_DATE, 2, 1, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (2, CURRENT_DATE - 1, 3, 2, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (3, CURRENT_DATE - 2, 1, 1, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (4, CURRENT_DATE - 3, 2, 2, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (5, CURRENT_DATE - 4, 3, 1, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (6, CURRENT_DATE - 5, 1, 2, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (7, CURRENT_DATE - 6, 2, 1, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (8, CURRENT_DATE - 7, 3, 2, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (9, CURRENT_DATE - 8, 1, 1, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (10, CURRENT_DATE - 9, 2, 2, 1);

INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (11, CURRENT_DATE - 10, 2, 2, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (12, CURRENT_DATE - 11, 3, 1, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (13, CURRENT_DATE - 12, 1, 2, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (14, CURRENT_DATE - 13, 2, 1, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (15, CURRENT_DATE - 14, 3, 2, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (16, CURRENT_DATE - 15, 1, 1, 1);
INSERT INTO reservation (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (17, CURRENT_DATE - 16, 2, 2, 1);

INSERT INTO waiting (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (1, CURRENT_DATE, 2, 1, 2);
INSERT INTO waiting (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (2, CURRENT_DATE - 1, 3, 2, 2);
INSERT INTO waiting (id, reservation_date, reservation_time_id, theme_id, member_id)
VALUES (3, CURRENT_DATE - 2, 1, 1, 2);

ALTER TABLE reservation_time
    ALTER COLUMN id RESTART WITH 4;

ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 3;

ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 18;

ALTER TABLE member
    ALTER COLUMN id RESTART WITH 2;

ALTER TABLE waiting
    ALTER COLUMN id RESTART WITH 4;

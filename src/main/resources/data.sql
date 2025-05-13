-- member
INSERT INTO member (name, role, email, password)
VALUES ('사용자1', 'ADMIN', 'test@email.com', 'pass1');
INSERT INTO member (name, role, email, password)
VALUES ('사용자2', 'USER', 'test2@email.com', 'pass2');
INSERT INTO member (name, role, email, password)
VALUES ('사용자3', 'USER', 'test3@email.com', 'pass3');

-- reservation_time
INSERT INTO reservation_time (start_at)
VALUES ('10:00');
INSERT INTO reservation_time (start_at)
VALUES ('11:00');
INSERT INTO reservation_time (start_at)
VALUES ('12:00');

-- theme
INSERT INTO theme (name, description, thumbnail)
VALUES ('레벨2 탈출',
        '우테코 레벨2를 탈출하는 내용입니다.',
        'image/wooteco.png');
INSERT INTO theme (name, description, thumbnail)
VALUES ('지하 감옥',
        '깊은 감옥에서 탈출하라!',
        'image/prison.png');

-- reservation: 오래된 데이터
-- 2025-04-11 ~ 2025-04-20
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-11', 2, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-12', 3, 2, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-13', 1, 1, 3);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-14', 2, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-15', 3, 1, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-16', 1, 2, 3);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-17', 2, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-18', 3, 2, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-19', 1, 1, 3);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-20', 2, 2, 1);

-- reservation: 오래된 데이터
-- 2025-04-23 ~ 2025-04-29
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-23', 2, 2, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-24', 3, 1, 3);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-25', 1, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-26', 2, 1, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-27', 3, 2, 3);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-28', 1, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-29', 2, 2, 2);

-- reservation: 최근 데이터
-- 2025-04-30 ~ 2025-05-06
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-30', 2, 2, 3);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-05-01', 3, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-05-02', 1, 2, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-05-03', 2, 1, 3);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-05-04', 3, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-05-05', 1, 1, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-05-06', 2, 2, 3);
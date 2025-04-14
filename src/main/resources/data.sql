-- 사용자
INSERT INTO users (name, email, password, role) VALUES ('매트', 'matt.kakao', '1234', 'ADMIN');

-- 시간
INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('11:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00');

-- 테마
INSERT INTO theme (name, description, thumbnail)
VALUES ('레벨2 탈출', '우테코 레벨2를 탈출하는 내용입니다.', 'https://techblog.woowahan.com/wp-content/uploads/img/2020-04-10/pobi.png');
INSERT INTO theme (name, description, thumbnail)
VALUES ('지하 감옥', '깊은 감옥에서 탈출하라!', 'https://truthfoundation.or.kr/media/images/68.width-1200.jpg');

-- 예약: 오래된 데이터 (2025-04-11 ~ 2025-04-20)
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-04-20', 2, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-04-19', 3, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-04-18', 1, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-04-17', 2, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-04-16', 3, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-04-15', 1, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-04-14', 2, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-04-13', 3, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-04-12', 1, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-04-11', 2, 2, 1);

-- 예약: 최근 데이터 (2025-04-30 ~ 2025-05-06)
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-05-06', 2, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-05-05', 3, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-05-04', 1, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-05-03', 2, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-05-02', 3, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-05-01', 1, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-04-30', 2, 2, 1);

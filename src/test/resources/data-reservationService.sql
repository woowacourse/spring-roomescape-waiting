-- 방탈출 예약 시간 삽입
INSERT INTO reservation_time (id, start_at) VALUES (100, '14:00');
INSERT INTO reservation_time (id, start_at) VALUES (101, '16:00');
INSERT INTO reservation_time (id, start_at) VALUES (102, '18:00');


-- 테마 삽입
INSERT INTO theme (id, name, description, thumbnail) VALUES (100, '평범', '평범한 테마입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');


-- 사용자 계정 삽입
INSERT INTO member (id, name, role, email, password) VALUES (100, 'kim', 'USER', 'email@test.com', 'pass');
INSERT INTO member (id, name, role, email, password) VALUES (101, 'lee', 'USER', 'lee@test.com', 'pass');


-- 방탈출 예약 삽입
INSERT INTO reservation (id, date, member_id, reservation_time_id, theme_id) VALUES (100, '2025-05-10', 100, 100, 100);
INSERT INTO reservation (id, date, member_id, reservation_time_id, theme_id) VALUES (101, '2025-05-09', 100, 100, 100);

-- 방탈출 예약 필터링에서 조회되지 않는 데이터
INSERT INTO reservation (id, date, member_id, reservation_time_id, theme_id) VALUES (102, '2025-05-08', 100, 100, 100);

INSERT INTO reservation (id, date, member_id, reservation_time_id, theme_id) VALUES (103, '2025-05-10', 101, 100, 100);

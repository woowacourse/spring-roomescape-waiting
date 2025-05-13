-- 방탈출 예약 시간 삽입
INSERT INTO reservation_time (id, start_at) VALUES (100, '10:00');


-- 테마 3종류 삽입
INSERT INTO theme (id, name, description, thumbnail) VALUES (100, '미예약', '미예약 테마입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (id, name, description, thumbnail) VALUES (101, '평범', '평범한 테마입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (id, name, description, thumbnail) VALUES (102, '추천', '추천하는 테마입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (id, name, description, thumbnail) VALUES (103, '강추', '강추하는 테마입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');


-- 사용자 계정 삽입
INSERT INTO member (id, name, role, email, password) VALUES (100, 'kim', 'USER', 'email@test.com', 'pass');


-- 방탈출 예약 삽입
INSERT INTO reservation (id, date, member_id, time_id, theme_id) VALUES (100, '2025-05-10', 100, 100, 101);
INSERT INTO reservation (id, date, member_id, time_id, theme_id) VALUES (101, '2025-05-02', 100, 100, 101);
INSERT INTO reservation (id, date, member_id, time_id, theme_id) VALUES (102, '2025-05-01', 100, 100, 101);

INSERT INTO reservation (id, date, member_id, time_id, theme_id) VALUES (103, '2025-05-10', 100, 100, 102);
INSERT INTO reservation (id, date, member_id, time_id, theme_id) VALUES (104, '2025-05-09', 100, 100, 102);

INSERT INTO reservation (id, date, member_id, time_id, theme_id) VALUES (105, '2025-05-10', 100, 100, 103);
INSERT INTO reservation (id, date, member_id, time_id, theme_id) VALUES (106, '2025-05-09', 100, 100, 103);
INSERT INTO reservation (id, date, member_id, time_id, theme_id) VALUES (107, '2025-05-08', 100, 100, 103);

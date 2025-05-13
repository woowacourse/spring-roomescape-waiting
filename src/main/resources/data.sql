-- 방탈출 예약 시간 삽입
INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00');
INSERT INTO reservation_time (start_at) VALUES ('14:00');
INSERT INTO reservation_time (start_at) VALUES ('16:00');
INSERT INTO reservation_time (start_at) VALUES ('18:00');
INSERT INTO reservation_time (start_at) VALUES ('20:00');


-- 테마 삽입
INSERT INTO theme (name, description, thumbnail) VALUES ('레벨1 탈출', '우테코 레벨1를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('레벨2 탈출', '우테코 레벨2를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('레벨3 탈출', '우테코 레벨3를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('레벨4 탈출', '우테코 레벨4를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('수료0 축하', '우테코 수료0를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');


-- 사용자 계정 삽입
INSERT INTO member (name, role, email, password) VALUES ('후유', 'ADMIN', 'email@test.com', '1212');
INSERT INTO member (name, role, email, password) VALUES ('네오', 'USER', 'neo@test.com', '1212');
INSERT INTO member (name, role, email, password) VALUES ('브라운', 'USER', 'braun@test.com', '1212');
INSERT INTO member (name, role, email, password) VALUES ('솔라', 'USER', 'sola@test.com', '1212');


-- 방탈출 예약 삽입
INSERT INTO reservation (date, member_id, time_id, theme_id) VALUES ('2025-05-10', 1, 1, 5);
INSERT INTO reservation (date, member_id, time_id, theme_id) VALUES ('2025-05-09', 1, 1, 5);

INSERT INTO reservation (date, member_id, time_id, theme_id) VALUES ('2025-05-08', 1, 1, 4);


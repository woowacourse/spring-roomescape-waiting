INSERT INTO theme(name, description, thumbnail) VALUES ('테마1', '설명1', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('테마2', '설명2', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('테마3', '설명3', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time(start_at) VALUES ('10:00');

INSERT INTO member(name, email, password, role) VALUES('어드민', 'admin@email.com', 'admin123', 'ADMIN');
INSERT INTO member(name, email, password, role) VALUES('리니', 'lini@email.com', 'lini123', 'GUEST');
INSERT INTO member(name, email, password, role) VALUES('페드로', 'pedro@email.com', 'pedro123', 'GUEST');
INSERT INTO member(name, email, password, role) VALUES('제이', 'junho@email.com', 'junho123', 'GUEST');
INSERT INTO member(name, email, password, role) VALUES('미르', 'duho@email.com', 'duho123', 'GUEST');

INSERT INTO reservation(date, time_id, member_id, theme_id, status) VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 1, 1, 'RESERVED');
INSERT INTO reservation(date, time_id, member_id, theme_id, status) VALUES (DATEADD('DAY', -7, CURRENT_DATE), 1, 2, 1, 'RESERVED');
INSERT INTO reservation(date, time_id, member_id, theme_id, status) VALUES (DATEADD('DAY', -6, CURRENT_DATE), 1, 3, 1, 'RESERVED');
INSERT INTO reservation(date, time_id, member_id, theme_id, status) VALUES (DATEADD('DAY', -5, CURRENT_DATE), 1, 3, 1, 'RESERVED');
INSERT INTO reservation(date, time_id, member_id, theme_id, status) VALUES (DATEADD('DAY', -5, CURRENT_DATE), 1, 3, 1, 'RESERVED');
INSERT INTO reservation(date, time_id, member_id, theme_id, status) VALUES (DATEADD('DAY', -7, CURRENT_DATE), 1, 3, 3, 'RESERVED');
INSERT INTO reservation(date, time_id, member_id, theme_id, status) VALUES (DATEADD('DAY', -7, CURRENT_DATE), 1, 3, 3, 'RESERVED');
INSERT INTO reservation(date, time_id, member_id, theme_id, status) VALUES (DATEADD('DAY', -1, CURRENT_DATE), 1, 3, 2, 'RESERVED');
-- 내일 날짜 2번 테마 1번 시간은 리니가 이미 예약한 상태임
INSERT INTO reservation(date, time_id, member_id, theme_id, status) VALUES (DATEADD('DAY', 1, CURRENT_DATE), 1, 1, 1, 'RESERVED');
INSERT INTO reservation(date, time_id, member_id, theme_id, status) VALUES (DATEADD('DAY', 1, CURRENT_DATE), 1, 2, 2, 'RESERVED');
INSERT INTO reservation(date, time_id, member_id, theme_id, status) VALUES (DATEADD('DAY', 1, CURRENT_DATE), 1, 3, 3, 'RESERVED');

-- 내일 날짜 2번 테마 1번 시간에 예약 대기 순서대로 생성(리니 -> 페드로 -> 제이 -> 미르)
INSERT INTO waiting(member_id, theme_id, time_id, date, created_at) VALUES (2, 2, 1, DATEADD('DAY', 1, CURRENT_DATE), DATEADD('HOUR', -4, CURRENT_TIMESTAMP));
INSERT INTO waiting(member_id, theme_id, time_id, date, created_at) VALUES (3, 2, 1, DATEADD('DAY', 1, CURRENT_DATE), DATEADD('HOUR', -3, CURRENT_TIMESTAMP));
INSERT INTO waiting(member_id, theme_id, time_id, date, created_at) VALUES (4, 2, 1, DATEADD('DAY', 1, CURRENT_DATE), DATEADD('HOUR', -2, CURRENT_TIMESTAMP));
INSERT INTO waiting(member_id, theme_id, time_id, date, created_at) VALUES (5, 2, 1, DATEADD('DAY', 1, CURRENT_DATE), DATEADD('HOUR', -1, CURRENT_TIMESTAMP));

INSERT INTO reservation_time (start_at)
VALUES ('10:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 A', '테마 A입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO member (name, email, password, role)
VALUES ('가이온', 'hello@woowa.com', 'password', 'ADMIN');
INSERT INTO member (name, email, password, role)
VALUES ('플린트', 'a', 'a', 'USER');
INSERT INTO member (name, email, password, role)
VALUES ('돔푸', 'b', 'b', 'USER');

INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-06-23', 1, 1, 2);
INSERT INTO waiting (date, time_id, theme_id, member_id)
VALUES ('2025-06-23', 1, 1, 1);
INSERT INTO waiting (date, time_id, theme_id, member_id)
VALUES ('2025-06-23', 1, 1, 3);

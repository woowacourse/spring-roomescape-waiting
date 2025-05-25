insert into member (name, email, role, password)
values ('가이온', 'hello@woowa.com', 'ADMIN', 'password');

INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 A', '테마 A입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time (start_at)
VALUES ('10:00');

INSERT INTO reservation (date, time_id, theme_id, member_id, status)
VALUES ('2025-05-22', 1, 1, 1, 'RESERVED');
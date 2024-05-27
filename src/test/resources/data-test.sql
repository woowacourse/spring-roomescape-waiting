SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE reservation_content RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
TRUNCATE TABLE time RESTART IDENTITY;
TRUNCATE TABLE member RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

SET @base_date = CURRENT_DATE;

INSERT INTO theme (id, name, description, thumbnail) VALUES (1, 'Harry Potter', '해리포터와 도비', 'thumbnail.jpg');
INSERT INTO time (id, start_at) VALUES (1, '22:59');
INSERT INTO member(id, name, email, password, role) VALUES (1, '켬미', 'aaa@naver.com', '1111', 'ADMIN');
INSERT INTO reservation_content (date, time_id, theme_id) VALUES (DATEADD('DAY', FLOOR(0) DAY, @base_date), 1, 1);
INSERT INTO reservation (id, member_id, reservation_content_id, created_at) VALUES (999, 1, 1, '2024-05-24 14:32:45');


INSERT INTO reservation_time (id, start_at)
VALUES (1, '10:00'),
       (2, '11:00');

INSERT INTO theme (id, name, description, thumbnail_url)
VALUES (1, '잃어버린 왕국', '사라진 고대 왕국의 비밀을 추적하는 모험 테마', 'https://example.com/images/lost-kingdom.jpg');

INSERT INTO member (id, name)
VALUES (1, 'fizz');

INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
VALUES (1, '2026-05-01', 1, 1);

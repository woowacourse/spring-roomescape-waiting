INSERT INTO reservation_time (time_id, start_at)
VALUES (100, '12:00');

INSERT INTO theme (theme_id, name, description, thumbnail) VALUES
  (100, '테마 1', '설명', '썸네일'),
  (200, '테마 2', '설명', '썸네일'),
  (300, '테마 3', '설명', '썸네일');

INSERT INTO member (member_id, name, email, password, role) VALUES
  (1000, '테스트', 'test_user@test.com', 'test', 'USER');

INSERT INTO reservation (member_id, date, time_id, theme_id, status) VALUES
  (1000, DATEADD('DAY', -1, CURRENT_DATE), 100, 100, 'RESERVED'),
  (1000, DATEADD('DAY', -2, CURRENT_DATE), 100, 200, 'RESERVED'),
  (1000, DATEADD('DAY', -3, CURRENT_DATE), 100, 200, 'RESERVED'),
  (1000, DATEADD('DAY', -4, CURRENT_DATE), 100, 300, 'RESERVED'),
  (1000, DATEADD('DAY', -5, CURRENT_DATE), 100, 300, 'RESERVED'),
  (1000, DATEADD('DAY', -6, CURRENT_DATE), 100, 300, 'RESERVED');

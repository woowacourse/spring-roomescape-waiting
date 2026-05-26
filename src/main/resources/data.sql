INSERT INTO reservation_time (start_time, end_time)
VALUES
    (DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', -1, TIMESTAMPADD(HOUR, 1, CURRENT_TIMESTAMP))),
    (DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -2, TIMESTAMPADD(HOUR, 1, CURRENT_TIMESTAMP))),
    (DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -3, TIMESTAMPADD(HOUR, 1, CURRENT_TIMESTAMP)));

INSERT INTO theme (name, description, image_url)
VALUES
    ('미궁의 유산', '고대 미궁에서 탈출하세요.', 'https://example.com/themes/1.png'),
    ('시간의 균열', '시간이 무너지는 방을 구하세요.', 'https://example.com/themes/2.png'),
    ('심해 기지', '심해 기지의 비밀을 밝혀라.', 'https://example.com/themes/3.png');

INSERT INTO reservation (name, time_id, theme_id)
VALUES
    ('tester1', 1, 1),
    ('tester2', 2, 1),
    ('tester3', 3, 1),
    ('tester4', 1, 2),
    ('tester5', 2, 2),
    ('tester6', 1, 3);

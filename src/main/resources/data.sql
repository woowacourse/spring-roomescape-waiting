INSERT INTO reservation_time (start_at, end_at)
VALUES
    (DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', -1, TIMESTAMPADD(HOUR, 1, CURRENT_TIMESTAMP))),
    (DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -2, TIMESTAMPADD(HOUR, 1, CURRENT_TIMESTAMP))),
    (DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -3, TIMESTAMPADD(HOUR, 1, CURRENT_TIMESTAMP))),
    (DATEADD('DAY', +3, CURRENT_TIMESTAMP), DATEADD('DAY', +3, TIMESTAMPADD(HOUR, 2, CURRENT_TIMESTAMP))),
    (DATEADD('DAY', +2, CURRENT_TIMESTAMP), DATEADD('DAY', +2, TIMESTAMPADD(HOUR, 2, CURRENT_TIMESTAMP))),
    (DATEADD('DAY', +1, CURRENT_TIMESTAMP), DATEADD('DAY', +1, TIMESTAMPADD(HOUR, 2, CURRENT_TIMESTAMP)));

INSERT INTO theme (name, description, image_url)
VALUES
    ('미궁의 유산', '고대 미궁에서 탈출하세요.', 'https://example.com/themes/1.png'),
    ('시간의 균열', '시간이 무너지는 방을 구하세요.', 'https://example.com/themes/2.png'),
    ('심해 기지3', '심해 기지의 비밀을 밝혀라.', 'https://example.com/themes/3.png'),
    ('심해 기지4', '심해 기지의 비밀을 밝혀라.', 'https://example.com/themes/4.png'),
    ('심해 기지5', '심해 기지의 비밀을 밝혀라.', 'https://example.com/themes/5.png');

INSERT INTO reservation (name, time_id, theme_id, status, created_at)
VALUES
    ('tester4', 1, 1, 'RESERVED', DATEADD('DAY', +1, CURRENT_TIMESTAMP)),
    ('tester1', 4, 2, 'RESERVED', DATEADD('DAY', +1, CURRENT_TIMESTAMP)),
    ('tester2', 4, 2, 'WAITING', DATEADD('DAY', +2, CURRENT_TIMESTAMP)),
    ('tester3', 4, 2, 'WAITING', DATEADD('DAY', +3, CURRENT_TIMESTAMP)),
    ('tester4', 4, 2, 'WAITING', DATEADD('DAY', +4, CURRENT_TIMESTAMP)),
    ('tester5', 4, 2, 'WAITING', DATEADD('DAY', +5, CURRENT_TIMESTAMP)),
    ('tester6', 4, 2, 'WAITING', DATEADD('DAY', +6, CURRENT_TIMESTAMP)),


    ('tester1', 1, 3, 'RESERVED', DATEADD('DAY', +1, CURRENT_TIMESTAMP)),
    ('tester2', 1, 3, 'WAITING', DATEADD('DAY', +4, CURRENT_TIMESTAMP)),
    ('tester3', 1, 3, 'WAITING', DATEADD('DAY', +3, CURRENT_TIMESTAMP)),
    ('tester4', 1, 3, 'WAITING', DATEADD('DAY', +2, CURRENT_TIMESTAMP)),

    ('tester4', 2, 2, 'RESERVED', CURRENT_TIMESTAMP),
    ('tester4', 2, 3, 'RESERVED', CURRENT_TIMESTAMP),
    ('tester4', 2, 4, 'RESERVED', CURRENT_TIMESTAMP);

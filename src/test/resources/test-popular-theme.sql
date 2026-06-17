INSERT INTO reservation_time (start_at, is_active)
VALUES ('09:00:00', TRUE),
       ('10:00:00', TRUE),
       ('11:00:00', TRUE);

INSERT INTO reservation_date (date, is_active)
VALUES (DATEADD('DAY', -9, CURRENT_DATE), TRUE),
       (DATEADD('DAY', -8, CURRENT_DATE), TRUE),
       (DATEADD('DAY', -7, CURRENT_DATE), TRUE),
       (DATEADD('DAY', -6, CURRENT_DATE), TRUE),
       (DATEADD('DAY', -5, CURRENT_DATE), TRUE),
       (DATEADD('DAY', -4, CURRENT_DATE), TRUE),
       (DATEADD('DAY', -3, CURRENT_DATE), TRUE),
       (DATEADD('DAY', -2, CURRENT_DATE), TRUE),
       (DATEADD('DAY', -1, CURRENT_DATE), TRUE),
       (CURRENT_DATE, TRUE);

INSERT INTO theme (name, description, thumbnail_url, is_active)
VALUES ('인기 테마 1', '예약 수가 가장 많은 테마', 'https://example.com/popular-theme-1.png', TRUE),
       ('인기 테마 2', '예약 수가 두 번째로 많은 테마', 'https://example.com/popular-theme-2.png', TRUE),
       ('인기 테마 3', '예약 수가 세 번째로 많은 테마', 'https://example.com/popular-theme-3.png', TRUE),
       ('비활성 인기 테마', '예약 수가 많아도 비활성이면 제외된다', 'https://example.com/inactive-theme.png', FALSE);

INSERT INTO member (name, password, role)
SELECT v.name, 'password', 'MEMBER'
FROM (VALUES ('member1'),
             ('member2'),
             ('member3'),
             ('member4'),
             ('member5'),
             ('member6'),
             ('member7'),
             ('canceled-member'),
             ('waiting-member'),
             ('waiting-member2'),
             ('today-member'),
             ('old-member'),
             ('inactive-member1'),
             ('inactive-member2'),
             ('inactive-member3')) AS v(name);

INSERT INTO reservation (member_id, date_id, time_id, theme_id, waiting_order, status)
SELECT m.id,
       rd.id,
       rt.id,
       t.id,
       v.waiting_order,
       v.status
FROM (VALUES ('member1', DATEADD('DAY', -8, CURRENT_DATE), '09:00:00', '인기 테마 1',
              0, 'RESERVED'),
             ('member2', DATEADD('DAY', -7, CURRENT_DATE), '09:00:00', '인기 테마 1',
              0, 'RESERVED'),
             ('member3', DATEADD('DAY', -6, CURRENT_DATE), '09:00:00', '인기 테마 1',
              0, 'RESERVED'),
             ('member4', DATEADD('DAY', -5, CURRENT_DATE), '09:00:00', '인기 테마 1',
              0, 'RESERVED'),
             ('member5', DATEADD('DAY', -4, CURRENT_DATE), '10:00:00', '인기 테마 2',
              0, 'RESERVED'),
             ('member6', DATEADD('DAY', -3, CURRENT_DATE), '10:00:00', '인기 테마 2',
              0, 'RESERVED'),
             ('member7', DATEADD('DAY', -2, CURRENT_DATE), '11:00:00', '인기 테마 3',
              0, 'RESERVED'),
             ('canceled-member', DATEADD('DAY', -1, CURRENT_DATE), '11:00:00', '인기 테마 2',
              0, 'CANCELED'),
             ('waiting-member', DATEADD('DAY', -1, CURRENT_DATE), '10:00:00', '인기 테마 3',
              1, 'WAITING'),
             ('waiting-member2', DATEADD('DAY', -1, CURRENT_DATE), '10:00:00', '인기 테마 3',
              2, 'WAITING'),
             ('today-member', CURRENT_DATE, '11:00:00', '인기 테마 1',
              0, 'RESERVED'),
             ('old-member', DATEADD('DAY', -9, CURRENT_DATE), '09:00:00', '인기 테마 2',
              0, 'RESERVED'),
             ('inactive-member1', DATEADD('DAY', -8, CURRENT_DATE), '10:00:00', '비활성 인기 테마',
              0, 'RESERVED'),
             ('inactive-member2', DATEADD('DAY', -7, CURRENT_DATE), '10:00:00', '비활성 인기 테마',
              0, 'RESERVED'),
             ('inactive-member3', DATEADD('DAY', -6, CURRENT_DATE), '10:00:00', '비활성 인기 테마',
              0, 'RESERVED')) AS v(name, reservation_date, start_at, theme_name, waiting_order,
                                    status)
         JOIN reservation_date rd ON rd.date = v.reservation_date
         JOIN reservation_time rt ON rt.start_at = v.start_at
         JOIN theme t ON t.name = v.theme_name
         JOIN member m ON m.name = v.name;

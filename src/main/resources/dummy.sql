-- Reservation Time
MERGE INTO reservation_time (start_at, is_active) KEY (start_at)
    VALUES ('11:00:00', true),
           ('12:00:00', true),
           ('13:00:00', true),
           ('14:00:00', true),
           ('15:00:00', true),
           ('16:00:00', true),
           ('17:00:00', true),
           ('18:00:00', true),
           ('19:00:00', true),
           ('20:00:00', true),
           ('21:00:00', true),
           ('22:00:00', true);

-- Reservation Date
MERGE INTO reservation_date (date, is_active) KEY (date)
    VALUES (DATEADD('DAY', -7, CURRENT_DATE), true),
           (DATEADD('DAY', -6, CURRENT_DATE), true),
           (DATEADD('DAY', -5, CURRENT_DATE), true),
           (DATEADD('DAY', -4, CURRENT_DATE), true),
           (DATEADD('DAY', -3, CURRENT_DATE), true),
           (DATEADD('DAY', -2, CURRENT_DATE), true),
           (DATEADD('DAY', -1, CURRENT_DATE), true),
           (CURRENT_DATE, true),
           (DATEADD('DAY', 1, CURRENT_DATE), true),
           (DATEADD('DAY', 2, CURRENT_DATE), true),
           (DATEADD('DAY', 3, CURRENT_DATE), false),
           (DATEADD('DAY', 4, CURRENT_DATE), true),
           (DATEADD('DAY', 5, CURRENT_DATE), true),
           (DATEADD('DAY', 6, CURRENT_DATE), true),
           (DATEADD('DAY', 7, CURRENT_DATE), true);

-- Theme
INSERT INTO theme (name, description, thumbnail_url, is_active)
SELECT v.name, v.description, v.thumbnail_url, v.is_active
FROM (VALUES ('잠겨버린 연구실', '제한 시간 안에 단서를 찾아 연구실을 탈출해야 합니다.',
              'https://images.unsplash.com/photo-1518005020951-eccb494ad742', TRUE),
             ('사라진 탐정', '실종된 탐정의 흔적을 따라 사건의 진실을 밝혀내세요.',
              'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee', TRUE),
             ('고대 유적의 비밀', '고대 유적에 숨겨진 암호를 풀고 보물을 찾아야 합니다.',
              'https://images.unsplash.com/photo-1506744038136-46273834b3fb', TRUE),
             ('유령 호텔', '폐쇄된 호텔에서 벌어진 미스터리한 사건을 해결하세요.',
              'https://images.unsplash.com/photo-1566073771259-6a8506099945',
              TRUE)) AS v(name, description, thumbnail_url, is_active)
WHERE NOT EXISTS (SELECT 1
                  FROM theme t
                  WHERE t.name = v.name);

INSERT INTO member (name, password, role)
SELECT v.name, v.password, v.role
FROM (VALUES ('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'MANAGER'),
             ('member', 'e606e38b0d8c19b24cf0ee3802e858abc6f393291503e30128a158bda25d1109', 'MEMBER'),
             ('다른사람', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'MEMBER'),
             ('송송', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'MEMBER'),
             ('pine', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'MEMBER'),
             ('김민준', 'password', 'MEMBER'),
             ('이서연', 'password', 'MEMBER'),
             ('박지후', 'password', 'MEMBER'),
             ('최하은', 'password', 'MEMBER'),
             ('정도윤', 'password', 'MEMBER'),
             ('한지민', 'password', 'MEMBER'),
             ('윤서준', 'password', 'MEMBER'),
             ('오지아', 'password', 'MEMBER'),
             ('강민재', 'password', 'MEMBER'),
             ('신예린', 'password', 'MEMBER'),
             ('송우석', 'password', 'MEMBER'),
             ('장하준', 'password', 'MEMBER'),
             ('임수아', 'password', 'MEMBER'),
             ('문지호', 'password', 'MEMBER'),
             ('백서윤', 'password', 'MEMBER')) AS v(name, password, role)
WHERE NOT EXISTS (SELECT 1
                  FROM member m
                  WHERE m.name = v.name);

-- Reservation Dummy Data
INSERT INTO reservation (member_id, date_id, time_id, theme_id, waiting_order, status)
SELECT m.id AS member_id,
       rd.id AS date_id,
       rt.id AS time_id,
       t.id  AS theme_id,
       v.waiting_order,
       v.status
FROM (VALUES ('김민준', DATEADD('DAY', 0, CURRENT_DATE), '11:00:00', '잠겨버린 연구실',
              0, 'RESERVED'),
             ('이서연', DATEADD('DAY', 0, CURRENT_DATE), '12:00:00', '잠겨버린 연구실',
              0, 'RESERVED'),
             ('박지후', DATEADD('DAY', 0, CURRENT_DATE), '13:00:00', '사라진 탐정',
              0, 'RESERVED'),
             ('최하은', DATEADD('DAY', -1, CURRENT_DATE), '11:00:00', '잠겨버린 연구실',
              0, 'RESERVED'),
             ('정도윤', DATEADD('DAY', -1, CURRENT_DATE), '14:00:00', '고대 유적의 비밀',
              0, 'RESERVED'),
             ('한지민', DATEADD('DAY', -1, CURRENT_DATE), '15:00:00', '사라진 탐정',
              0, 'RESERVED'),
             ('윤서준', DATEADD('DAY', -1, CURRENT_DATE), '16:00:00', '잠겨버린 연구실',
              0, 'RESERVED'),
             ('오지아', DATEADD('DAY', -2, CURRENT_DATE), '17:00:00', '잠겨버린 연구실',
              0, 'RESERVED'),
             ('강민재', DATEADD('DAY', -2, CURRENT_DATE), '18:00:00', '고대 유적의 비밀',
              0, 'RESERVED'),
             ('신예린', DATEADD('DAY', -3, CURRENT_DATE), '11:00:00', '사라진 탐정',
              0, 'RESERVED'),
             ('송우석', DATEADD('DAY', -3, CURRENT_DATE), '19:00:00', '잠겨버린 연구실',
              0, 'RESERVED'),
             ('장하준', DATEADD('DAY', -3, CURRENT_DATE), '20:00:00', '유령 호텔',
              0, 'RESERVED'),
             ('임수아', DATEADD('DAY', -4, CURRENT_DATE), '12:00:00', '잠겨버린 연구실',
              0, 'RESERVED'),
             ('문지호', DATEADD('DAY', -4, CURRENT_DATE), '13:00:00', '고대 유적의 비밀',
              0, 'RESERVED'),
             ('백서윤', DATEADD('DAY', -4, CURRENT_DATE), '21:00:00', '유령 호텔',
              0, 'CANCELED')) AS v(name, reservation_date, start_at, theme_name, waiting_order,
                                   status)
         JOIN reservation_date rd ON rd.date = v.reservation_date
         JOIN reservation_time rt ON rt.start_at = v.start_at
         JOIN theme t ON t.name = v.theme_name
         JOIN member m ON m.name = v.name
WHERE NOT EXISTS (SELECT 1
                  FROM reservation r
                  WHERE r.member_id = m.id
                    AND r.date_id = rd.id
                    AND r.time_id = rt.id
                    AND r.theme_id = t.id);

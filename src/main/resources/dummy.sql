-- Reservation Time
MERGE INTO reservation_time (start_at, is_active) KEY (start_at)
    VALUES
    ('11:00:00', true),
    ('12:00:00', true),
    ('13:00:00', true),
    ('14:00:00', true),
    ('15:00:00', true),
    ('16:00:00', true),
    ('17:00:00', true),
    ('18:00:00', true),
    ('19:00:00', true),
    ('20:00:00', true);

-- Reservation Date
MERGE INTO reservation_date (date, is_active) KEY (date)
    VALUES
    (DATEADD('DAY', -7, CURRENT_DATE), true),
    (DATEADD('DAY', -6, CURRENT_DATE), true),
    (DATEADD('DAY', -5, CURRENT_DATE), true),
    (DATEADD('DAY', -4, CURRENT_DATE), true),
    (DATEADD('DAY', -3, CURRENT_DATE), true),
    (DATEADD('DAY', -2, CURRENT_DATE), true),
    (DATEADD('DAY', -1, CURRENT_DATE), true),
    (CURRENT_DATE, true),
    (DATEADD('DAY', 1, CURRENT_DATE), true),
    (DATEADD('DAY', 2, CURRENT_DATE), true),
    (DATEADD('DAY', 3, CURRENT_DATE), false);

-- Theme
INSERT INTO theme (name, description, thumbnail_url, is_active, amount)
SELECT v.name, v.description, v.thumbnail_url, v.is_active, v.amount
FROM (
         VALUES
         ('잠겨버린 연구실', '제한 시간 안에 단서를 찾아 연구실을 탈출해야 합니다.', 'https://images.unsplash.com/photo-1518005020951-eccb494ad742', TRUE, 25000),
             ('사라진 탐정', '실종된 탐정의 흔적을 따라 사건의 진실을 밝혀내세요.', 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee', TRUE, 27000),
             ('고대 유적의 비밀', '고대 유적에 숨겨진 암호를 풀고 보물을 찾아야 합니다.', 'https://images.unsplash.com/photo-1506744038136-46273834b3fb', TRUE, 29000)
     ) AS v(name, description, thumbnail_url, is_active, amount)
WHERE NOT EXISTS (
    SELECT 1 FROM theme t WHERE t.name = v.name
);

-- Reservation Slot (11 dates × 10 times × 3 themes = 330개)
INSERT INTO reservation_slot (date_id, time_id, theme_id)
SELECT rd.id, rt.id, t.id
FROM reservation_date rd
         CROSS JOIN reservation_time rt
         CROSS JOIN theme t
WHERE NOT EXISTS (
    SELECT 1
    FROM reservation_slot rs
    WHERE rs.date_id  = rd.id
      AND rs.time_id  = rt.id
      AND rs.theme_id = t.id
);

-- Reservation Dummy Data
INSERT INTO reservation (name, slot_id, reserved_at, status)
SELECT
    v.name,
    rs.id AS slot_id,
    v.reserved_at,
    v.status
FROM (
         VALUES
             ('김민준', DATEADD('DAY',  0, CURRENT_DATE), '11:00:00', '잠겨버린 연구실',  DATEADD('DAY', -7,  CURRENT_TIMESTAMP), 'RESERVED'),
             ('이서연', DATEADD('DAY',  0, CURRENT_DATE), '12:00:00', '잠겨버린 연구실',  DATEADD('DAY', -7,  CURRENT_TIMESTAMP), 'RESERVED'),
             ('박지후', DATEADD('DAY',  0, CURRENT_DATE), '13:00:00', '사라진 탐정',      DATEADD('DAY', -7,  CURRENT_TIMESTAMP), 'RESERVED'),
             ('최하은', DATEADD('DAY', -1, CURRENT_DATE), '11:00:00', '잠겨버린 연구실',  DATEADD('DAY', -8,  CURRENT_TIMESTAMP), 'RESERVED'),
             ('정도윤', DATEADD('DAY', -1, CURRENT_DATE), '14:00:00', '고대 유적의 비밀', DATEADD('DAY', -8,  CURRENT_TIMESTAMP), 'RESERVED'),
             ('한지민', DATEADD('DAY', -1, CURRENT_DATE), '15:00:00', '사라진 탐정',      DATEADD('DAY', -8,  CURRENT_TIMESTAMP), 'RESERVED'),
             ('윤서준', DATEADD('DAY', -1, CURRENT_DATE), '16:00:00', '잠겨버린 연구실',  DATEADD('DAY', -8,  CURRENT_TIMESTAMP), 'RESERVED'),
             ('오지아', DATEADD('DAY', -2, CURRENT_DATE), '17:00:00', '잠겨버린 연구실',  DATEADD('DAY', -9,  CURRENT_TIMESTAMP), 'RESERVED'),
             ('강민재', DATEADD('DAY', -2, CURRENT_DATE), '18:00:00', '고대 유적의 비밀', DATEADD('DAY', -9,  CURRENT_TIMESTAMP), 'RESERVED'),
             ('신예린', DATEADD('DAY', -3, CURRENT_DATE), '11:00:00', '사라진 탐정',      DATEADD('DAY', -10, CURRENT_TIMESTAMP), 'RESERVED'),
             ('송우석', DATEADD('DAY', -3, CURRENT_DATE), '19:00:00', '잠겨버린 연구실',  DATEADD('DAY', -10, CURRENT_TIMESTAMP), 'RESERVED'),
             ('장하준', DATEADD('DAY', -3, CURRENT_DATE), '20:00:00', '고대 유적의 비밀', DATEADD('DAY', -10, CURRENT_TIMESTAMP), 'RESERVED')
     ) AS v(name, reservation_date, start_at, theme_name, reserved_at, status)
         JOIN reservation_date rd ON rd.date     = v.reservation_date
         JOIN reservation_time rt ON rt.start_at = v.start_at
         JOIN theme             t  ON t.name      = v.theme_name
         JOIN reservation_slot rs ON rs.date_id   = rd.id
                                 AND rs.time_id   = rt.id
                                 AND rs.theme_id  = t.id
WHERE NOT EXISTS (
    SELECT 1
    FROM reservation r
    WHERE r.name    = v.name
      AND r.slot_id = rs.id
);

-- Member
INSERT INTO member (name, password, role) VALUES ('admin',   '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'MANAGER');
INSERT INTO member (name, password, role) VALUES ('member',  'e606e38b0d8c19b24cf0ee3802e858abc6f393291503e30128a158bda25d1109', 'MEMBER');
INSERT INTO member (name, password, role) VALUES ('다른사람', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'MEMBER');
INSERT INTO member (name, password, role) VALUES ('송송',    '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'MEMBER');
INSERT INTO member (name, password, role) VALUES ('pine',    '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'MEMBER');

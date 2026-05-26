INSERT INTO reservation_time (start_at)
VALUES ('10:00');
INSERT INTO reservation_time (start_at)
VALUES ('11:00');
INSERT INTO reservation_time (start_at)
VALUES ('12:00');
INSERT INTO reservation_time (start_at)
VALUES ('13:00');

INSERT INTO theme (name, description, thumbnail_url)
VALUES ('세기의 도둑', '보안을 뚫고 보석을 훔쳐라', './images/thief.jpeg'),
       ('세기의 도둑2', '보안을 뚫고 보석을 훔쳐라2', './images/deepsea.jpeg'),
       ('세기의 도둑3', '보안을 뚫고 보석을 훔쳐라3', './images/time.jpeg'),
       ('세기의 도둑4', '보안을 뚫고 보석을 훔쳐라4', './images/ghosthotel.jpeg');

INSERT INTO store (name, spot)
VALUES ('이삭방탈출', '강남점');

INSERT INTO schedule (date, time_id, theme_id, store_id)
VALUES ('2026-05-05', 1, 1, 1), -- id=1
       ('2026-05-05', 2, 2, 1), -- id=2
       ('2026-05-05', 3, 3, 1), -- id=3
       ('2026-05-05', 4, 4, 1), -- id=4
       ('2026-05-06', 2, 2, 1); -- id=5

INSERT INTO member (name, password, role)
VALUES ('a', 'test1', 'USER'),
       ('testAdmin', 'test2', 'MANAGER');

INSERT INTO manager (member_id, store_id)
VALUES (2, 1);

INSERT
INTO reservation (member_id, schedule_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 5);

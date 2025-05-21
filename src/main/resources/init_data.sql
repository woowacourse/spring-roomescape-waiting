INSERT INTO member(role, name, email, password)
VALUES ('GENERAL', 'member', 'member@email.com', 'qwer1234!'),
       ('ADMIN', 'admin', 'admin@email.com', 'qwer1234!');

INSERT INTO reservation_time(start_at)
VALUES ('10:00'),
       ('11:00'),
       ('12:00'),
       ('13:00');

INSERT INTO theme(name, description, thumbnail)
VALUES ('Theme 1', '설명1',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 2', '설명2',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 3', '설명3',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 4', '설명4',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU');

INSERT INTO reservation(date, status, reservation_time_id, theme_id, member_id)
VALUES
-- theme_id 1에 1개 예약
('2025-05-10', 'BOOKED', 1, 1, 1),

-- theme_id 2에 2개 예약
('2025-05-10', 'BOOKED', 1, 2, 1),
('2025-05-10', 'BOOKED', 2, 2, 1),

-- theme_id 3에 3개 예약
('2025-05-10', 'BOOKED', 1, 3, 1),
('2025-05-10', 'BOOKED', 2, 3, 1),
('2025-05-10', 'BOOKED', 3, 3, 1),

-- theme_id 4에 4개 예약
('2025-05-10', 'BOOKED', 1, 4, 1),
('2025-05-10', 'BOOKED', 2, 4, 1),
('2025-05-10', 'BOOKED', 3, 4, 1),
('2025-05-10', 'BOOKED', 4, 4, 1);

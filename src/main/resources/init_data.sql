INSERT INTO member(role, name, email, password)
VALUES ('GENERAL', 'member1', 'member1@email.com', 'qwer1234!'),
       ('GENERAL', 'member2', 'member2@email.com', 'qwer1234!'),
       ('GENERAL', 'member3', 'member3@email.com', 'qwer1234!'),
       ('GENERAL', 'member4', 'member4@email.com', 'qwer1234!'),
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

INSERT INTO reservation(date, reservation_time_id, theme_id, member_id)
VALUES
('2025-05-10', 1, 1, 1),

('2025-05-10', 1, 2, 1),
('2025-05-10', 2, 2, 1),

('2025-05-10', 1, 3, 1),
('2025-05-10', 2, 3, 1),
('2025-05-10', 3, 3, 1),

('2025-05-10', 1, 4, 1),
('2025-05-10', 2, 4, 1),
('2025-05-10', 3, 4, 1),
('2025-05-10', 4, 4, 1);

INSERT INTO waiting(date, theme_id, time_id, member_id)
VALUES
('2025-05-10', 1, 1, 2),
('2025-05-10', 1, 1, 3),
('2025-05-10', 1, 1, 4);

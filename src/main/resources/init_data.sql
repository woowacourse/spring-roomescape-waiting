INSERT INTO member(role, name, email, password, created_at, updated_at)
VALUES ('GENERAL', 'member1', 'member1@email.com', 'qwer1234!', '2025-05-23 17:37:43.488281', '2025-05-23 17:37:43.488281'),
       ('GENERAL', 'member2', 'member2@email.com', 'qwer1234!', '2025-05-23 17:37:43.488281', '2025-05-23 17:37:43.488281'),
       ('GENERAL', 'member3', 'member3@email.com', 'qwer1234!', '2025-05-23 17:37:43.488281', '2025-05-23 17:37:43.488281'),
       ('GENERAL', 'member4', 'member4@email.com', 'qwer1234!', '2025-05-23 17:37:43.488281', '2025-05-23 17:37:43.488281'),
       ('ADMIN', 'admin', 'admin@email.com', 'qwer1234!', '2025-05-23 17:37:43.488281', '2025-05-23 17:37:43.488281');

INSERT INTO reservation_time(start_at, created_at, updated_at)
VALUES ('10:00', '2025-05-23 18:37:43.488281', '2025-05-23 18:37:43.488281'),
       ('11:00', '2025-05-23 18:37:43.488281', '2025-05-23 18:37:43.488281'),
       ('12:00', '2025-05-23 18:37:43.488281', '2025-05-23 18:37:43.488281'),
       ('13:00', '2025-05-23 18:37:43.488281', '2025-05-23 18:37:43.488281');

INSERT INTO theme(name, description, thumbnail, created_at, updated_at)
VALUES ('Theme 1', '설명1',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU',
        '2025-05-23 18:37:43.488281', '2025-05-23 18:37:43.488281'),
       ('Theme 2', '설명2',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU',
        '2025-05-23 18:37:43.488281', '2025-05-23 18:37:43.488281'),
       ('Theme 3', '설명3',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU',
        '2025-05-23 18:37:43.488281', '2025-05-23 18:37:43.488281'),
       ('Theme 4', '설명4',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU',
        '2025-05-23 18:37:43.488281', '2025-05-23 18:37:43.488281');

INSERT INTO reservation(date, reservation_time_id, theme_id, member_id, created_at, updated_at)
VALUES
('2025-05-10', 1, 1, 1, '2025-05-23 19:37:43.488281', '2025-05-23 19:37:43.488281'),

('2025-05-10', 1, 2, 1, '2025-05-23 19:37:43.488281', '2025-05-23 19:37:43.488281'),
('2025-05-10', 2, 2, 1, '2025-05-23 19:37:43.488281', '2025-05-23 19:37:43.488281'),

('2025-05-10', 1, 3, 1, '2025-05-23 19:37:43.488281', '2025-05-23 19:37:43.488281'),
('2025-05-10', 2, 3, 1, '2025-05-23 19:37:43.488281', '2025-05-23 19:37:43.488281'),
('2025-05-10', 3, 3, 1, '2025-05-23 19:37:43.488281', '2025-05-23 19:37:43.488281'),

('2025-05-10', 1, 4, 1, '2025-05-23 19:37:43.488281', '2025-05-23 19:37:43.488281'),
('2025-05-10', 2, 4, 1, '2025-05-23 19:37:43.488281', '2025-05-23 19:37:43.488281'),
('2025-05-10', 3, 4, 1, '2025-05-23 19:37:43.488281', '2025-05-23 19:37:43.488281'),
('2025-05-10', 4, 4, 1, '2025-05-23 19:37:43.488281', '2025-05-23 19:37:43.488281');

INSERT INTO waiting(date, theme_id, time_id, member_id, created_at, updated_at)
VALUES
('2025-05-10', 1, 1, 2, '2025-05-23 20:37:43.488281', '2025-05-23 20:37:43.488281'),
('2025-05-10', 1, 1, 3, '2025-05-23 21:37:43.488281', '2025-05-23 20:37:43.488281'),
('2025-05-10', 1, 1, 4, '2025-05-23 22:37:43.488281', '2025-05-23 20:37:43.488281');

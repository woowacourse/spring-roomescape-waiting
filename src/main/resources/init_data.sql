INSERT INTO reservation_time(start_at)
VALUES ('10:00'),
       ('11:00'),
       ('12:00'),
       ('13:00'),
       ('14:00'),
       ('15:00'),
       ('16:00'),
       ('17:00'),
       ('18:00'),
       ('19:00'),
       ('20:00'),
       ('21:00'),
       ('22:00'),
       ('23:00'),
       ('23:06');

INSERT INTO theme(name, description, thumbnail)
VALUES ('Theme 1', '테마1 설명',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 2', '테마2 설명',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 3', '테마3 설명',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 4', '테마4 설명',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 5', '테마5 설명',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 6', '테마6 설명',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 7', '테마7 설명',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 8', '테마8 설명',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 9', '테마9 설명',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 10', '테마10 설명',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 11', '테마11 설명',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 12', '테마12 설명',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU'),
       ('Theme 13', '테마13 설명',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQqlkX2ISwyii-yHkQmp-Ad0hsfekERx2RNEa_RFNrr25BDWEAxHRgghcPid7ckxbLngE&usqp=CAU');

INSERT INTO reservation(date, time_id, theme_id)
VALUES
-- theme_id 1에 1개 예약
('2025-05-10', 1, 1),

-- theme_id 2에 2개 예약
('2025-05-10', 1, 2),
('2025-05-10', 2, 2),

-- theme_id 3에 3개 예약
('2025-05-10', 1, 3),
('2025-05-10', 2, 3),
('2025-05-10', 3, 3),

-- theme_id 4에 4개 예약
('2025-05-10', 1, 4),
('2025-05-10', 2, 4),
('2025-05-10', 3, 4),
('2025-05-10', 4, 4),

-- theme_id 5에 5개 예약
('2025-05-10', 1, 5),
('2025-05-10', 2, 5),
('2025-05-10', 3, 5),
('2025-05-10', 4, 5),
('2025-05-10', 5, 5),

-- theme_id 6에 6개 예약
('2025-05-10', 1, 6),
('2025-05-10', 2, 6),
('2025-05-10', 3, 6),
('2025-05-10', 4, 6),
('2025-05-10', 5, 6),
('2025-05-10', 6, 6),

-- theme_id 7에 7개 예약
('2025-05-10', 1, 7),
('2025-05-10', 2, 7),
('2025-05-10', 3, 7),
('2025-05-10', 4, 7),
('2025-05-10', 5, 7),
('2025-05-10', 6, 7),
('2025-05-10', 7, 7),

-- theme_id 8에 8개 예약
('2025-05-10', 1, 8),
('2025-05-10', 2, 8),
('2025-05-10', 3, 8),
('2025-05-10', 4, 8),
('2025-05-10', 5, 8),
('2025-05-10', 6, 8),
('2025-05-10', 7, 8),
('2025-05-10', 8, 8),

-- theme_id 9에 9개 예약
('2025-05-10', 1, 9),
('2025-05-10', 2, 9),
('2025-05-10', 3, 9),
('2025-05-10', 4, 9),
('2025-05-10', 5, 9),
('2025-05-10', 6, 9),
('2025-05-10', 7, 9),
('2025-05-10', 8, 9),
('2025-05-10', 9, 9),

-- theme_id 10에 10개 예약
('2025-05-10', 1, 10),
('2025-05-10', 2, 10),
('2025-05-10', 3, 10),
('2025-05-10', 4, 10),
('2025-05-10', 5, 10),
('2025-05-10', 6, 10),
('2025-05-10', 7, 10),
('2025-05-10', 8, 10),
('2025-05-10', 9, 10),
('2025-05-10', 10, 10),

-- theme_id 11에 11개 예약
('2025-05-10', 1, 11),
('2025-05-10', 2, 11),
('2025-05-10', 3, 11),
('2025-05-10', 4, 11),
('2025-05-10', 5, 11),
('2025-05-10', 6, 11),
('2025-05-10', 7, 11),
('2025-05-10', 8, 11),
('2025-05-10', 9, 11),
('2025-05-10', 10, 11),
('2025-05-10', 11, 11),

-- theme_id 12에 12개 예약
('2025-05-5', 1, 12),
('2025-05-5', 2, 12),
('2025-05-5', 3, 12),
('2025-05-5', 4, 12),
('2025-05-5', 5, 12),
('2025-05-5', 6, 12),
('2025-05-5', 7, 12),
('2025-05-5', 8, 12),
('2025-05-5', 9, 12),
('2025-05-5', 10, 12),
('2025-05-5', 11, 12),
('2025-05-5', 12, 12),

-- theme_id 13에 13개 예약
('2025-05-5', 1, 13),
('2025-05-5', 2, 13),
('2025-05-5', 3, 13),
('2025-05-5', 4, 13),
('2025-05-5', 5, 13),
('2025-05-5', 6, 13),
('2025-05-5', 7, 13),
('2025-05-5', 8, 13),
('2025-05-5', 9, 13),
('2025-05-5', 10, 13),
('2025-05-5', 11, 13),
('2025-05-5', 12, 13),
('2025-05-5', 13, 13);

INSERT INTO users(role, name, email, password)
VALUES ('ROLE_MEMBER', 'name', 'user@email.com', 'password'),
       ('ROLE_ADMIN', '어드민', 'admin@email.com', 'password2');

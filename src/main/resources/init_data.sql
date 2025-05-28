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

INSERT INTO users(role, name, email, password)
VALUES ('ROLE_MEMBER', 'name', 'user@email.com', 'password'),
       ('ROLE_ADMIN', '어드민', 'admin@email.com', 'password2');

INSERT INTO reservation(date, status, reservation_time_id, theme_id, user_id)
VALUES
-- theme_id 1에 1개 예약
('2025-05-21', 'BOOKED', 1, 1, 1),

-- theme_id 2에 2개 예약
('2025-05-24', 'BOOKED', 1, 2, 1),
('2025-05-24', 'BOOKED', 2, 2, 1),

-- theme_id 3에 3개 예약
('2025-05-21', 'BOOKED', 1, 3, 1),
('2025-05-21', 'BOOKED', 2, 3, 1),
('2025-05-21', 'BOOKED', 3, 3, 1),

-- theme_id 4에 4개 예약
('2025-05-21', 'BOOKED', 1, 4, 1),
('2025-05-21', 'BOOKED', 2, 4, 1),
('2025-05-21', 'BOOKED', 3, 4, 1),
('2025-05-21', 'BOOKED', 4, 4, 1),

-- theme_id 5에 5개 예약
('2025-05-21', 'BOOKED', 1, 5, 1),
('2025-05-21', 'BOOKED', 2, 5, 1),
('2025-05-21', 'BOOKED', 3, 5, 1),
('2025-05-21', 'BOOKED', 4, 5, 1),
('2025-05-21', 'BOOKED', 5, 5, 1),

-- theme_id 6에 6개 예약
('2025-05-21', 'BOOKED', 1, 6, 1),
('2025-05-21', 'BOOKED', 2, 6, 1),
('2025-05-21', 'BOOKED', 3, 6, 1),
('2025-05-21', 'BOOKED', 4, 6, 1),
('2025-05-21', 'BOOKED', 5, 6, 1),
('2025-05-21', 'BOOKED', 6, 6, 1),

-- theme_id 7에 7개 예약
('2025-05-21', 'BOOKED', 1, 7, 1),
('2025-05-21', 'BOOKED', 2, 7, 1),
('2025-05-21', 'BOOKED', 3, 7, 1),
('2025-05-21', 'BOOKED', 4, 7, 1),
('2025-05-21', 'BOOKED', 5, 7, 1),
('2025-05-21', 'BOOKED', 6, 7, 1),
('2025-05-21', 'BOOKED', 7, 7, 1),

-- theme_id 8에 8개 예약
('2025-05-21', 'BOOKED', 1, 8, 1),
('2025-05-21', 'BOOKED', 2, 8, 1),
('2025-05-21', 'BOOKED', 3, 8, 1),
('2025-05-21', 'BOOKED', 4, 8, 1),
('2025-05-21', 'BOOKED', 5, 8, 1),
('2025-05-21', 'BOOKED', 6, 8, 1),
('2025-05-21', 'BOOKED', 7, 8, 1),
('2025-05-21', 'BOOKED', 8, 8, 1),

-- theme_id 9에 9개 예약
('2025-05-21', 'BOOKED', 1, 9, 1),
('2025-05-21', 'BOOKED', 2, 9, 1),
('2025-05-21', 'BOOKED', 3, 9, 1),
('2025-05-21', 'BOOKED', 4, 9, 1),
('2025-05-21', 'BOOKED', 5, 9, 1),
('2025-05-21', 'BOOKED', 6, 9, 1),
('2025-05-21', 'BOOKED', 7, 9, 1),
('2025-05-21', 'BOOKED', 8, 9, 1),
('2025-05-21', 'BOOKED', 8, 9, 1),

-- theme_id 10에 10개 예약
('2025-05-21', 'BOOKED', 1, 10, 1),
('2025-05-21', 'BOOKED', 2, 10, 1),
('2025-05-21', 'BOOKED', 3, 10, 1),
('2025-05-21', 'BOOKED', 4, 10, 1),
('2025-05-21', 'BOOKED', 5, 10, 1),
('2025-05-21', 'BOOKED', 6, 10, 1),
('2025-05-21', 'BOOKED', 7, 10, 1),
('2025-05-21', 'BOOKED', 8, 10, 1),
('2025-05-21', 'BOOKED', 8, 10, 1),
('2025-05-21', 'BOOKED', 8, 10, 1),

-- theme_id 11에 11개 예약
('2025-05-21', 'BOOKED', 1, 11, 1),
('2025-05-21', 'BOOKED', 2, 11, 1),
('2025-05-21', 'BOOKED', 3, 11, 1),
('2025-05-21', 'BOOKED', 4, 11, 1),
('2025-05-21', 'BOOKED', 5, 11, 1),
('2025-05-21', 'BOOKED', 6, 11, 1),
('2025-05-21', 'BOOKED', 7, 11, 1),
('2025-05-21', 'BOOKED', 8, 11, 1),
('2025-05-21', 'BOOKED', 8, 11, 1),
('2025-05-21', 'BOOKED', 8, 11, 1),
('2025-05-21', 'BOOKED', 8, 11, 1),

-- theme_id 12에 12개 예약
('2025-05-21', 'BOOKED', 1, 12, 1),
('2025-05-21', 'BOOKED', 2, 12, 1),
('2025-05-21', 'BOOKED', 3, 12, 1),
('2025-05-21', 'BOOKED', 4, 12, 1),
('2025-05-21', 'BOOKED', 5, 12, 1),
('2025-05-21', 'BOOKED', 6, 12, 1),
('2025-05-21', 'BOOKED', 7, 12, 1),
('2025-05-21', 'BOOKED', 8, 12, 1),
('2025-05-21', 'BOOKED', 8, 12, 1),
('2025-05-21', 'BOOKED', 8, 12, 1),
('2025-05-21', 'BOOKED', 8, 12, 1),
('2025-05-21', 'BOOKED', 8, 12, 1),

-- theme_id 13에 13개 예약
('2025-05-21', 'BOOKED', 1, 13, 1),
('2025-05-21', 'BOOKED', 2, 13, 1),
('2025-05-21', 'BOOKED', 3, 13, 1),
('2025-05-21', 'BOOKED', 4, 13, 1),
('2025-05-21', 'BOOKED', 5, 13, 1),
('2025-05-21', 'BOOKED', 6, 13, 1),
('2025-05-21', 'BOOKED', 7, 13, 1),
('2025-05-21', 'BOOKED', 8, 13, 1),
('2025-05-21', 'BOOKED', 8, 13, 1),
('2025-05-21', 'BOOKED', 8, 13, 1),
('2025-05-21', 'BOOKED', 8, 13, 1),
('2025-05-21', 'BOOKED', 8, 13, 1),
('2025-05-21', 'BOOKED', 8, 13, 1);

INSERT INTO waiting(date, reservation_time_id, theme_id, member_id)
VALUES
('2025-05-24', 1, 2, 1),
('2025-05-24', 2, 2, 1);

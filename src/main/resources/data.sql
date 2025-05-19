INSERT INTO member (name, email, password, role)
VALUES ('이스트', 'east@email.com', '1234', 'ADMIN');

INSERT INTO member (name, email, password, role)
VALUES ('WooGa', 'wooga@gmail.com', '1234', 'USER');

INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('11:00'),
       ('12:00'),
       ('13:00'),
       ('14:00'),
       ('15:00'),
       ('16:00'),
       ('17:00'),
       ('18:00'),
       ('19:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('Theme1', 'Description1', 'thumbnail1.jpg'),
       ('Theme2', 'Description2', 'thumbnail2.jpg'),
       ('Theme3', 'Description3', 'thumbnail3.jpg'),
       ('Theme4', 'Description4', 'thumbnail4.jpg'),
       ('Theme5', 'Description5', 'thumbnail5.jpg'),
       ('Theme6', 'Description6', 'thumbnail6.jpg'),
       ('Theme7', 'Description7', 'thumbnail7.jpg'),
       ('Theme8', 'Description8', 'thumbnail8.jpg'),
       ('Theme9', 'Description9', 'thumbnail9.jpg'),
       ('Theme10', 'Description10', 'thumbnail10.jpg'),
       ('Theme11', 'Description11', 'thumbnail11.jpg');


-- 인기 테마 테스트를 위한 예약 데이터 추가
-- Theme1-9: 1개 예약, Theme10: 2개 예약, Theme11: 0개 예약
INSERT INTO reservation (member_id, theme_id, time_id, date)
VALUES
    (1, 1, 1, CURRENT_DATE - INTERVAL '2' DAY),
    (1, 2, 1, CURRENT_DATE - INTERVAL '2' DAY),
    (1, 3, 1, CURRENT_DATE - INTERVAL '2' DAY),
    (1, 4, 1, CURRENT_DATE - INTERVAL '2' DAY),
    (1, 5, 1, CURRENT_DATE - INTERVAL '2' DAY),
    (1, 6, 1, CURRENT_DATE - INTERVAL '2' DAY),
    (1, 7, 1, CURRENT_DATE - INTERVAL '2' DAY),
    (1, 8, 1, CURRENT_DATE - INTERVAL '2' DAY),
    (1, 9, 1, CURRENT_DATE - INTERVAL '2' DAY),
    (1, 10, 1, CURRENT_DATE - INTERVAL '2' DAY),
    (1, 10, 2, CURRENT_DATE - INTERVAL '2' DAY);

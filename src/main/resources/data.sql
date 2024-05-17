-- 테마 추가
INSERT INTO theme(name, description, thumbnail)
VALUES ('추리', '추리 테마입니다.', 'https://image.yes24.com/goods/73161943/L');
INSERT INTO theme(name, description, thumbnail)
VALUES ('아날로그식', '아날로그식 테마입니다.', 'https://image.yes24.com/goods/62087889/L');
INSERT INTO theme(name, description, thumbnail)
VALUES ('스테이지형', '스테이지형 테마입니다.', 'https://image.yes24.com/goods/125101417/L');
INSERT INTO theme(name, description, thumbnail)
VALUES ('협동형', '협동형 테마입니다.', 'https://image.yes24.com/momo/TopCate432/MidCate002/43115671.jpg');


-- 예약 시간 추가
INSERT INTO reservation_time(start_at)
VALUES ('10:00');
INSERT INTO reservation_time(start_at)
VALUES ('11:30');
INSERT INTO reservation_time(start_at)
VALUES ('13:00');
INSERT INTO reservation_time(start_at)
VALUES ('15:00');
INSERT INTO reservation_time(start_at)
VALUES ('17:00');
INSERT INTO reservation_time(start_at)
VALUES ('18:00');
INSERT INTO reservation_time(start_at)
VALUES ('19:00');
INSERT INTO reservation_time(start_at)
VALUES ('20:00');


-- 예약 추가
INSERT INTO reservation(date, time_id, theme_id)
VALUES (DATEADD('DAY', -2, CURRENT_DATE()), 1, 1);
INSERT INTO reservation(date, time_id, theme_id)
VALUES (DATEADD('DAY', 16, CURRENT_DATE()), 1, 2);
INSERT INTO reservation(date, time_id, theme_id)
VALUES (DATEADD('YEAR', 1, CURRENT_DATE()), 2, 1);
INSERT INTO reservation(date, time_id, theme_id)
VALUES (DATEADD('DAY', 1, CURRENT_DATE()), 2, 2);
INSERT INTO reservation(date, time_id, theme_id)
VALUES (DATEADD('DAY', -4, CURRENT_DATE()), 3, 3);
INSERT INTO reservation(date, time_id, theme_id)
VALUES (DATEADD('DAY', 4, CURRENT_DATE()), 3, 4);
INSERT INTO reservation(date, time_id, theme_id)
VALUES (DATEADD('MONTH', 1, CURRENT_DATE()), 3, 4);
INSERT INTO reservation(date, time_id, theme_id)
VALUES (DATEADD('DAY', 4, CURRENT_DATE()), 3, 4);


-- 회원 추가
INSERT INTO member(name, email, password, role)
VALUES ('초코칩', 'dev.chocochip@gmail.com', '$2a$10$hNWX2lluVCgwaorpX8TnZO2XadZKdzI6qCNGoSm/ptdBxvFYAGFw.', 'USER');
INSERT INTO member(name, email, password, role)
VALUES ('이든', 'dev.eden@gmail.com', '$2a$10$opT2WtzYtjCgcWrHAOxe/u7DcNQXPBgoEVjoM8ld8nc1DIaMOfmvm', 'USER');
INSERT INTO member(name, email, password, role)
VALUES ('클로버', 'dev.clover@gmail.com', '$2a$10$SpRsR566UrP/bK2pfKJhe.ghb5Y9/GLjXi/kifJ8x53y5opxHqkr6', 'USER');
INSERT INTO member(name, email, password, role)
VALUES ('관리자', 'admin@roomescape.com', '$2a$10$5xUHgA2/scLa/9YzqkCrXuAoIwLYiZTif8F8QrjuFfSFRgsUdJYhC', 'ADMIN');


-- 예약 목록 추가
INSERT INTO member_reservation(reservation_status, member_id, reservation_id)
VALUES ('BOOKED', 1, 1);
INSERT INTO member_reservation(reservation_status, member_id, reservation_id)
VALUES ('BOOKED', 1, 3);
INSERT INTO member_reservation(reservation_status, member_id, reservation_id)
VALUES ('BOOKED', 1, 7);
INSERT INTO member_reservation(reservation_status, member_id, reservation_id)
VALUES ('BOOKED', 2, 2);
INSERT INTO member_reservation(reservation_status, member_id, reservation_id)
VALUES ('BOOKED', 2, 4);
INSERT INTO member_reservation(reservation_status, member_id, reservation_id)
VALUES ('BOOKED', 2, 8);
INSERT INTO member_reservation(reservation_status, member_id, reservation_id)
VALUES ('BOOKED', 3, 3);
INSERT INTO member_reservation(reservation_status, member_id, reservation_id)
VALUES ('BOOKED', 3, 5);
INSERT INTO member_reservation(reservation_status, member_id, reservation_id)
VALUES ('BOOKED', 3, 6);
INSERT INTO member_reservation(reservation_status, member_id, reservation_id)
VALUES ('BOOKED', 3, 7);

-- Member 데이터
INSERT INTO member (name, email, role)
VALUES ('관리자', 'admin@email.com', 'ADMIN');
INSERT INTO member (name, email, role)
VALUES ('사용자1', 'user1@email.com', 'USER');
INSERT INTO member (name, email, role)
VALUES ('사용자2', 'user2@email.com', 'USER');

-- Account 데이터 (비밀번호: qwe123)
INSERT INTO account (member_id, password)
VALUES (1, '$2a$10$sTsPr34tBtFTg/kPncx0EecdPlqcvMu2kBRrUbCXf3KOW7rNMvC7a');
INSERT INTO account (member_id, password)
VALUES (2, '$2a$10$sTsPr34tBtFTg/kPncx0EecdPlqcvMu2kBRrUbCXf3KOW7rNMvC7a');
INSERT INTO account (member_id, password)
VALUES (3, '$2a$10$sTsPr34tBtFTg/kPncx0EecdPlqcvMu2kBRrUbCXf3KOW7rNMvC7a');

-- Theme 데이터
INSERT INTO theme (name, description, thumbnail)
VALUES ('미스터리 하우스', '미스터리한 저택에서 벌어지는 스릴러 테마', 'https://example.com/mystery.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('이집트 탈출', '고대 이집트 피라미드에서의 탈출', 'https://example.com/egypt.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('감옥 탈출', '감옥에서 탈출하는 스릴러 테마', 'https://example.com/prison.jpg');

-- ReservationTime 데이터
INSERT INTO reservation_time (start_at)
VALUES ('10:00:00');
INSERT INTO reservation_time (start_at)
VALUES ('12:00:00');
INSERT INTO reservation_time (start_at)
VALUES ('14:00:00');
INSERT INTO reservation_time (start_at)
VALUES ('16:00:00');
INSERT INTO reservation_time (start_at)
VALUES ('18:00:00');
INSERT INTO reservation_time (start_at)
VALUES ('20:00:00');

-- Reservation 데이터 (현재 날짜 기준 예약)
INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
VALUES (2, CURRENT_DATE + 1, 1, 1);
INSERT INTO reservation (member_id, reservation_date, time_id, theme_id)
VALUES (3, CURRENT_DATE + 1, 2, 2);

-- ReservationWait 데이터 (대기 예약)
INSERT INTO reservation_wait (member_id, reservation_date, time_id, theme_id)
VALUES (3, CURRENT_DATE + 1, 1, 1);
INSERT INTO reservation_wait (member_id, reservation_date, time_id, theme_id)
VALUES (2, CURRENT_DATE + 1, 2, 2);

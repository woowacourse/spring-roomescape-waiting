INSERT INTO reservation_time (start_at)
VALUES ('13:00:00');
INSERT INTO reservation_time (start_at)
VALUES ('14:00:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('호러', '매우 무섭습니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('추리', '매우 어렵습니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO member (name, email, password, role)
VALUES ('미아', 'mia@gmail.com', 'asdfe', 'USER');
INSERT INTO member (name, email, password, role)
VALUES ('냥인', 'nyangin@gmail.com', 'dfdfdf', 'USER');
INSERT INTO member (name, email, password, role)
VALUES ('토미', 'tommy@gmail.com', 'jyefr', 'USER');

INSERT INTO reservation (date, member_id, theme_id, time_id, status)
VALUES (CURRENT_DATE() - 7, 1, 1, 1, 'BOOKING');
INSERT INTO reservation (date, member_id, theme_id, time_id, status)
VALUES (CURRENT_DATE() - 2, 2, 2, 1, 'BOOKING');
INSERT INTO reservation (date, member_id, theme_id, time_id, status)
VALUES (CURRENT_DATE() - 1, 3, 2, 2, 'BOOKING');

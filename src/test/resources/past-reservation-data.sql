INSERT INTO reservation_time (id, start_at)
VALUES (1, '13:00:00');
INSERT INTO reservation_time (id, start_at)
VALUES (2, '14:00:00');

INSERT INTO theme (id, name, description, thumbnail)
VALUES (1, '호러', '매우 무섭습니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (id, name, description, thumbnail)
VALUES (2, '추리', '매우 어렵습니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO member (id, name, email, password, role)
VALUES (1, '미아', 'mia@gmail.com', 'asdfe', 'USER');
INSERT INTO member (id, name, email, password, role)
VALUES (2, '냥인', 'nyangin@gmail.com', 'dfdfdf', 'USER');
INSERT INTO member (id, name, email, password, role)
VALUES (3, '토미', 'tommy@gmail.com', 'jyefr', 'USER');

INSERT INTO reservation (id, date, member_id, theme_id, time_id)
VALUES (1, CURRENT_DATE() - 7, 1, 1, 1);
INSERT INTO reservation (id, date, member_id, theme_id, time_id)
VALUES (2, CURRENT_DATE() - 2, 2, 2, 1);
INSERT INTO reservation (id, date, member_id, theme_id, time_id)
VALUES (3, CURRENT_DATE() - 1, 3, 2, 2);

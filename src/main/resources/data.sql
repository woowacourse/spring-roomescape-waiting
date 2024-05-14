INSERT INTO reservation_time (id, start_at)
VALUES (1, '13:00:00');
INSERT INTO reservation_time (id, start_at)
VALUES (2, '14:00:00');

INSERT INTO theme (id, name, description, thumbnail)
VALUES (1, '호러', '매우 무섭습니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (id, name, description, thumbnail)
VALUES (2, '추리', '매우 어렵습니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO member (id, name, email, password, role)
VALUES (1, '어드민', 'admin@admin.com', '1234', 'ADMIN');
INSERT INTO member (id, name, email, password, role)
VALUES (2, '미아', 'mia@mia.com', '1234', 'USER');

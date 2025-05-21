INSERT INTO reservation_theme (name, description, thumbnail)
VALUES ('레벨 1탈출', '우테코 레벨1를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨 2탈출', '우테코 레벨2를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨 3탈출', '우테코 레벨3를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨 4탈출', '우테코 레벨4를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨 5탈출', '우테코 레벨5를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨 6탈출', '우테코 레벨6를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨 7탈출', '우테코 레벨7를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨 8탈출', '우테코 레벨8를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨 9탈출', '우테코 레벨9를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨 10탈출', '우테코 레벨10를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨 11탈출', '우테코 레벨11를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('레벨 12탈출', '우테코 레벨12를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO reservation_time (start_at)
VALUES ('15:40'),
       ('16:30'),
       ('17:50');
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('김민준', '2025-04-30', 1, 8),
       ('이서연', '2025-04-30', 2, 3),
       ('박지후', '2025-04-30', 3, 11),
       ('최예린', '2025-04-30', 1, 5),
       ('정우진', '2025-04-30', 2, 1),
       ('한수아', '2025-04-30', 3, 12),
       ('윤도현', '2025-04-30', 1, 6),
       ('장하윤', '2025-04-30', 2, 9),
       ('서지호', '2025-04-30', 3, 2),
       ('배예준', '2025-04-30', 1, 10),
       ('임하람', '2025-04-30', 2, 4),
       ('권지민', '2025-04-30', 3, 7),
       ('조유진', '2025-04-30', 1, 2),
       ('백승현', '2025-04-30', 2, 12),
       ('노하은', '2025-04-30', 3, 6),
       ('하준서', '2025-04-30', 1, 3),
       ('문다연', '2025-04-30', 2, 5),
       ('송지후', '2025-04-30', 3, 8),
       ('남시우', '2025-04-30', 1, 11),
       ('오예린', '2025-04-30', 2, 1);

INSERT INTO member (email, password, name, role, session_id)
VALUES ('admin@email.com', 'MTIzNA==', '어드민', 'ADMIN', NULL),
       ('phk1138@naver.com', 'MTIzNA==', '윌슨', 'USER', NULL),
       ('phk1148@naver.com', 'MTIzNA==', '호떡', 'USER', NULL),
       ('phk1158@naver.com', 'MTIzNA==', '한스', 'USER', NULL);

INSERT INTO reservation_v2 (member_id, date, time_id, theme_id)
VALUES (2, '2025-05-07', 1, 8),
    (3, '2025-05-06', 3, 6),
    (4, '2025-05-05', 1, 7),
       (1, '2025-05-01', 2, 7)

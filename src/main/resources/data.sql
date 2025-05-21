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

INSERT INTO member (email, password, name, role, session_id)
VALUES ('admin@email.com', 'MTIzNA==', '어드민', 'ADMIN', NULL),
       ('phk1138@naver.com', 'MTIzNA==', '윌슨', 'USER', NULL),
       ('phk1148@naver.com', 'MTIzNA==', '호떡', 'USER', NULL),
       ('phk1158@naver.com', 'MTIzNA==', '한스', 'USER', NULL);

INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (2, '2025-05-07', 1, 8),
    (3, '2025-05-06', 3, 6),
    (4, '2025-05-05', 1, 7),
       (1, '2025-05-01', 2, 7)

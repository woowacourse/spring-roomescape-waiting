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
       ('user1@email.com', 'MTIzNA==', '윌슨', 'USER', NULL),
       ('user2@email.com', 'MTIzNA==', '호떡', 'USER', NULL),
       ('user3@email.com', 'MTIzNA==', '한스', 'USER', NULL);

INSERT INTO reservation_item (date, time_id, theme_id)
VALUES
    -- 2주 전 (CURRENT_DATE - 14일 ~ CURRENT_DATE - 8일)
    (CURRENT_DATE - 14, 1, 1),   -- id: 1
    (CURRENT_DATE - 14, 2, 2),   -- id: 2
    (CURRENT_DATE - 13, 1, 3),   -- id: 3
    (CURRENT_DATE - 13, 3, 4),   -- id: 4
    (CURRENT_DATE - 12, 2, 5),   -- id: 5
    (CURRENT_DATE - 11, 1, 6),   -- id: 6
    (CURRENT_DATE - 10, 3, 7),   -- id: 7
    (CURRENT_DATE - 9, 2, 8),    -- id: 8
    (CURRENT_DATE - 8, 1, 9),    -- id: 9

    -- 1주 전 (CURRENT_DATE - 7일 ~ CURRENT_DATE - 1일)
    (CURRENT_DATE - 7, 2, 10),   -- id: 10
    (CURRENT_DATE - 6, 1, 11),   -- id: 11
    (CURRENT_DATE - 6, 3, 12),   -- id: 12
    (CURRENT_DATE - 5, 2, 1),    -- id: 13
    (CURRENT_DATE - 4, 1, 2),    -- id: 14
    (CURRENT_DATE - 3, 3, 3),    -- id: 15
    (CURRENT_DATE - 2, 2, 4),    -- id: 16
    (CURRENT_DATE - 1, 1, 5),    -- id: 17

    -- 오늘과 미래
    (CURRENT_DATE, 2, 6),        -- id: 18
    (CURRENT_DATE + 1, 1, 7),    -- id: 19
    (CURRENT_DATE + 1, 3, 8),    -- id: 20
    (CURRENT_DATE + 2, 2, 9),    -- id: 21
    (CURRENT_DATE + 3, 1, 10),   -- id: 22
    (CURRENT_DATE + 4, 3, 11),   -- id: 23
    (CURRENT_DATE + 5, 2, 12),   -- id: 24
    (CURRENT_DATE + 6, 1, 1);    -- id: 25

INSERT INTO reservation (member_id, reservation_item_id, reservation_status)
VALUES
    -- 과거 예약들 (모두 확정)
    (2, 1, 'ACCEPTED'),
    (3, 2, 'ACCEPTED'),
    (4, 3, 'ACCEPTED'),
    (1, 4, 'ACCEPTED'),
    (2, 5, 'ACCEPTED'),
    (3, 6, 'ACCEPTED'),
    (4, 7, 'ACCEPTED'),
    (1, 8, 'ACCEPTED'),
    (2, 9, 'ACCEPTED'),
    (3, 10, 'ACCEPTED'),
    (4, 11, 'ACCEPTED'),

    -- PENDING이 있을 예약 아이템들의 ACCEPTED 예약 (먼저 삽입)
    (3, 12, 'ACCEPTED'),
    (1, 13, 'ACCEPTED'),
    (2, 14, 'ACCEPTED'),
    (2, 15, 'ACCEPTED'),
    (1, 16, 'ACCEPTED'),
    (3, 17, 'ACCEPTED'),
    (4, 18, 'ACCEPTED'),
    (1, 19, 'ACCEPTED'),
    (1, 20, 'ACCEPTED'),
    (4, 21, 'ACCEPTED'),
    (2, 22, 'ACCEPTED'),
    (1, 23, 'ACCEPTED'),
    (3, 24, 'ACCEPTED'),
    (2, 25, 'ACCEPTED');

-- 2단계: PENDING 예약들 삽입 (각 아이템에 ACCEPTED가 먼저 있는 경우에만)
INSERT INTO reservation (member_id, reservation_item_id, reservation_status)
VALUES
    -- reservation_item_id: 12 (6일전, 호떡이 확정)에 대한 대기 목록
    (1, 12, 'PENDING'),   -- 어드민 - 1번째 대기
    (2, 12, 'PENDING'),   -- 윌슨 - 2번째 대기

    -- reservation_item_id: 14 (4일전, 윌슨이 확정)에 대한 대기 목록
    (3, 14, 'PENDING'),   -- 호떡 - 1번째 대기
    (4, 14, 'PENDING'),   -- 한스 - 2번째 대기
    (1, 14, 'PENDING'),   -- 어드민 - 3번째 대기

    -- reservation_item_id: 16 (2일전, 어드민이 확정)에 대한 대기 목록
    (2, 16, 'PENDING'),   -- 윌슨 - 1번째 대기
    (4, 16, 'PENDING'),   -- 한스 - 2번째 대기

    -- reservation_item_id: 19 (내일, 어드민이 확정)에 대한 대기 목록
    (3, 19, 'PENDING'),   -- 호떡 - 1번째 대기
    (4, 19, 'PENDING'),   -- 한스 - 2번째 대기
    (2, 19, 'PENDING'),   -- 윌슨 - 3번째 대기

    -- reservation_item_id: 21 (2일후, 한스가 확정)에 대한 대기 목록
    (1, 21, 'PENDING'),   -- 어드민 - 1번째 대기
    (3, 21, 'PENDING'),   -- 호떡 - 2번째 대기

    -- reservation_item_id: 23 (4일후, 어드민이 확정)에 대한 대기 목록
    (2, 23, 'PENDING'),   -- 윌슨 - 1번째 대기
    (4, 23, 'PENDING'),   -- 한스 - 2번째 대기
    (3, 23, 'PENDING'),   -- 호떡 - 3번째 대기

    -- reservation_item_id: 25 (6일후, 윌슨이 확정)에 대한 대기 목록
    (4, 25, 'PENDING'),   -- 한스 - 1번째 대기
    (3, 25, 'PENDING');   -- 호떡 - 2번째 대기

-- users 테이블
INSERT INTO users (name, email, password, role, created_at)
VALUES ('admin1', 'admin@email.com', 'a(단방향) 해시 처리 완료', 'ADMIN', '2024-05-25 12:00:00'),
       ('normal1', 'normal1@email.com', 'a(단방향) 해시 처리 완료', 'NORMAL', '2024-05-25 12:05:00'),
       ('normal2', 'normal2@email.com', 'a(단방향) 해시 처리 완료', 'NORMAL', '2024-05-25 12:10:00'),
       ('normal3', 'normal3@email.com', 'a(단방향) 해시 처리 완료', 'NORMAL', '2024-05-25 12:10:00');

-- time_slots 테이블
INSERT INTO time_slots (start_at, created_at)
VALUES ('10:00', '2024-05-25 09:00:00'),
       ('12:00', '2024-05-25 09:00:00'),
       ('14:00', '2024-05-25 09:00:00'),
       ('16:00', '2024-05-25 09:00:00'),
       ('18:00', '2024-05-25 09:00:00'),
       ('20:00', '2024-05-25 09:00:00');

-- themes 테이블
INSERT INTO themes (name, description, thumbnail, created_at)
VALUES ('레벨2 탈출', '우테코 레벨2를 탈출하는 내용입니다.', 'https://example.com/image.jpg', '2024-05-25 09:05:00'),
       ('지하 감옥', '깊은 감옥에서 탈출하라!', 'https://example.com/jail.jpg', '2024-05-25 09:10:00');

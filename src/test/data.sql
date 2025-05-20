INSERT INTO users (id, name, email, password, role)
VALUES (1, 'admin', 'admin@email.com', 'a(단방향) 해시 처리 완료', 'ADMIN'),
       (2, 'normal1', 'normal1@email.com', 'a(단방향) 해시 처리 완료', 'NORMAL'),
       (3, 'normal2', 'normal2@email.com', 'a(단방향) 해시 처리 완료', 'NORMAL');

INSERT INTO reservation_times (start_at)
VALUES ('10:00'),
       ('11:00'),
       ('12:00');

INSERT INTO themes (name, description, thumbnail)
VALUES ('레벨2 탈출',
        '우테코 레벨2를 탈출하는 내용입니다.',
        'https://example.com/image.jpg'),
       ('지하 감옥',
        '깊은 감옥에서 탈출하라!',
        'https://example.com/jail.jpg');



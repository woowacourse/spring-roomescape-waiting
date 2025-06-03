INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('11:00'),
       ('12:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('레벨2탈출', '우테코 레벨2를 탈출하는 내용입니다.', 'https://example.com/image.jpg'),
       ('지하감옥', '깊은 감옥에서 탈출하라!', 'https://example.com/jail.jpg');

INSERT INTO member (name, email, password, role)
VALUES ('한스', 'leehyeonsu4888@gmail.com', '$2a$10$JuoTZeZ9AxCZGB/M8U7Ft.y5UBAHrUg7eS3p8ISOcmMmx0Z6t9kfm', 'MEMBER'),
       ('머피', 'woowacourse@naver.com', '$2a$10$JuoTZeZ9AxCZGB/M8U7Ft.y5UBAHrUg7eS3p8ISOcmMmx0Z6t9kfm', 'MEMBER'),
       ('한스', 'leehyeonsu48888@gmail.com', '$2a$10$JuoTZeZ9AxCZGB/M8U7Ft.y5UBAHrUg7eS3p8ISOcmMmx0Z6t9kfm', 'ADMIN')

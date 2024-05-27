INSERT INTO reservation_time (start_at)
VALUES ('09:00');

INSERT INTO reservation_time (start_at)
VALUES ('10:00');

INSERT INTO reservation_time (start_at)
VALUES ('11:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('레벨1 탈출', '우테코 레벨1를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO theme (name, description, thumbnail)
VALUES ('레벨2 탈출', '우테코 레벨2를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO theme (name, description, thumbnail)
VALUES ('레벨3 탈출', '우테코 레벨3를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO theme (name, description, thumbnail)
VALUES ('레벨4 탈출', '우테코 레벨4를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

--모든 멤버의 비밀번호는 문자열 'password'를 BCryptPasswordEncoder 로 인코딩한 암호 입니다.
INSERT INTO member (name, email, password, role)
VALUES ('카고', 'kargo123@email.com', '$2a$10$I5luDmIIQbnvBpUcJZrICedhIJSOMQ0QvnhIGkLoOaEyjupvVjHLm', 'USER');

INSERT INTO member (name, email, password, role)
VALUES ('브라운', 'brown123@email.com', '$2a$10$I5luDmIIQbnvBpUcJZrICedhIJSOMQ0QvnhIGkLoOaEyjupvVjHLm', 'USER');

INSERT INTO member (name, email, password, role)
VALUES ('솔라', 'solar123@email.com', '$2a$10$I5luDmIIQbnvBpUcJZrICedhIJSOMQ0QvnhIGkLoOaEyjupvVjHLm', 'USER');

INSERT INTO member (name, email, password, role)
VALUES ('어드민', 'admin@email.com', '$2a$10$I5luDmIIQbnvBpUcJZrICedhIJSOMQ0QvnhIGkLoOaEyjupvVjHLm', 'ADMIN');

INSERT INTO reservation (member_id, date, reservation_time_id, theme_id) VALUES
                               (1L, DATEADD('DAY', -10, CURRENT_DATE), 1, 3),
                               (1L, DATEADD('DAY', -6, CURRENT_DATE), 2, 3),
                               (1L, DATEADD('DAY', -5, CURRENT_DATE), 3, 3),
                               (2L, DATEADD('DAY', -2, CURRENT_DATE), 2, 2),
                               (2L, DATEADD('DAY', -1, CURRENT_DATE), 1, 1),
                               (1L, DATEADD('DAY', +2, CURRENT_DATE), 1, 1),
                               (1L, DATEADD('DAY', +3, CURRENT_DATE), 1, 1),
                               (2L, DATEADD('DAY', +4, CURRENT_DATE), 1, 2),
                               (2L, DATEADD('DAY', +5, CURRENT_DATE), 1, 2);

INSERT INTO waiting (member_id, date, reservation_time_id, theme_id) VALUES
                               (2L, DATEADD('DAY', +2, CURRENT_DATE), 1, 1),
                               (3L, DATEADD('DAY', +2, CURRENT_DATE), 1, 1),
                               (2L, DATEADD('DAY', +3, CURRENT_DATE), 1, 1),
                               (3L, DATEADD('DAY', +4, CURRENT_DATE), 1, 2),
                               (1L, DATEADD('DAY', +4, CURRENT_DATE), 1, 2),
                               (1L, DATEADD('DAY', +5, CURRENT_DATE), 1, 2);

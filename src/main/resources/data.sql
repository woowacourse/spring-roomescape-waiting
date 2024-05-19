INSERT INTO reservation_time (start_at)
VALUES ('15:40'),
       ('13:40'),
       ('17:40');

INSERT INTO member (name, email, password, role)
VALUES ('어드민', 'polla@gmail.com', 'pollari99', 'ADMIN'),
       ('일반', 'polla@naver.com', 'pollari999', 'MEMBER');


INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 - polla', '폴라 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 - dobby', '도비 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마 - pobi', '포비 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2024-04-30', 1, 1, 1),
       ('2024-05-01', 2, 2, 2);

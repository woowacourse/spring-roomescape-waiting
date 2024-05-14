INSERT INTO member (name, email, password, role)
VALUES ('어드민', 'test@email.com', 'password', 'ADMIN'),
       ('유저', 'user@email.com', 'password', 'USER');

INSERT INTO reservation_time (start_at)
VALUES ('13:00'),
       ('14:00'),
       ('15:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('테마1', '테마1 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마2', '테마2 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마3', '테마3 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마4', '테마4 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg'),
       ('테마5', '테마5 설명', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation (date, member_id, time_id, theme_id)
VALUES ('2024-05-07', 1, 1, 1);
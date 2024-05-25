INSERT INTO theme (name, description, thumbnail)
VALUES ('Birthday Party', 'Celebration for birthdays', 'birthday_party_thumbnail.jpg'),
       ('Romantic Dinner', 'Intimate dinner setting', 'romantic_dinner_thumbnail.jpg'),
       ('Movie Night', 'Movie-themed event', 'movie_night_thumbnail.jpg');

-- reservation_time 테이블에 데이터 삽입
INSERT INTO reservation_time (start_at)
VALUES ('18:00:00'),
       ('19:30:00'),
       ('21:00:00');

-- member 테이블에 데이터 삽입
INSERT INTO member (name, email, password, role)
VALUES ('John Doe', 'john@example.com', 'password123', 'ADMIN'),
       ('Jane Smith', 'jane@example.com', 'abc12345', 'USER'),
       ('Alice Lee', 'alice@example.com', 'qwerty1234', 'USER');

INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2024-05-30', 1, 1, 2),
       ('2024-05-31', 2, 2, 3),
       ('2024-06-01', 3, 3, 1);

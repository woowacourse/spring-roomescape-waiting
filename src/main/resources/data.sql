INSERT INTO member (name, email, password, role)
VALUES ('admin', 'admin@email.com', 'password', 'ADMIN'),
       ('qwe', 'qwe@a.com', 'qwe', 'USER'),
       ('asd', 'asd@a.com', 'asd', 'ADMIN');

INSERT INTO reservation_time (start_at)
VALUES ('11:11'),
       ('12:12'),
       ('13:13'),
       ('14:14'),
       ('15:15'),
       ('16:16'),
       ('17:17');

INSERT INTO theme (name, description, thumbnail)
VALUES ('theme1', 'description1', 'https://img.taplb.com/md5/86/00/86004586c417ea624782b4eeb9672ab2/-_-/fit-in/720x0/filters:quality(80):background_color(white):format(jpeg)'),
       ('theme2', 'description2', 'https://play-lh.googleusercontent.com/Lhnq7HqTciwIiEeEqnCJzA8wYQIQw5M0qqWWZBS3GfgrM1nNKyQmKUZBqvluwYztQg');

INSERT INTO reservation(reservation_date, member_id, time_id, theme_id)
VALUES ('2024-05-01', 1, 1, 1),
       ('2024-05-11', 2, 1, 1),
       ('2024-06-02', 2, 2, 1);
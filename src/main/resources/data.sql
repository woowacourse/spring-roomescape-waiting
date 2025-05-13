INSERT INTO members (name, email, password, role)
VALUES ('이스트', 'east@email.com', '1234', 'ADMIN');

INSERT INTO members (name, email, password, role)
VALUES ('WooGa', 'wooga@gmail.com', '1234', 'USER');

INSERT INTO reservation_times (start_at)
VALUES ('10:00:00'),
       ('11:00:00'),
       ('12:00:00'),
       ('13:00:00'),
       ('14:00:00'),
       ('15:00:00'),
       ('16:00:00'),
       ('17:00:00'),
       ('18:00:00'),
       ('19:00:00');

INSERT INTO themes (name, description, thumbnail)
VALUES ('Theme1', 'Description1', 'thumbnail1.jpg'),
       ('Theme2', 'Description2', 'thumbnail2.jpg'),
       ('Theme3', 'Description3', 'thumbnail3.jpg'),
       ('Theme4', 'Description4', 'thumbnail4.jpg'),
       ('Theme5', 'Description5', 'thumbnail5.jpg');

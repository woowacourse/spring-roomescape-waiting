INSERT INTO theme(name, description, thumbnail)
VALUES ('theme1', 'desc1',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme2', 'desc2',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme3', 'desc3',
        'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation_time(start_at)
VALUES ('10:00');

INSERT INTO member(name, email, password, role)
VALUES ('Stitch', 'admin@gmail.com', 'asd', 'ADMIN');

INSERT INTO member(name, email, password, role)
VALUES ('Wiib', 'user@gmail.com', 'asd', 'USER');

INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 1, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 1, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 1, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES (TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 3, 1);

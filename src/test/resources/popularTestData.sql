ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;
ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1;
ALTER TABLE theme ALTER COLUMN id RESTART WITH 1;

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

INSERT INTO reservation(name, date, time_id, theme_id)
VALUES ('brown1', TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation(name, date, time_id, theme_id)
VALUES ('brown2', TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation(name, date, time_id, theme_id)
VALUES ('brown3', TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation(name, date, time_id, theme_id)
VALUES ('brown4', TIMESTAMPADD(WEEK, -1, CURRENT_DATE), 1, 3);

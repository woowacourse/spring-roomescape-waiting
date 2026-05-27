INSERT INTO reservation_time (start_at, finish_at)
VALUES ('10:00', '11:00');
INSERT INTO reservation_time (start_at, finish_at)
VALUES ('14:00', '15:00');
INSERT INTO reservation_time (start_at, finish_at)
VALUES ('18:00', '19:00');

INSERT INTO theme (name, description, image_url)
VALUES ('테마A', '설명A', 'https://a.com');
INSERT INTO theme (name, description, image_url)
VALUES ('테마B', '설명B', 'https://b.com');
INSERT INTO theme (name, description, image_url)
VALUES ('테마C', '설명C', 'https://c.com');
INSERT INTO theme (name, description, image_url)
VALUES ('테마D', '설명D', 'https://d.com');

INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user2', DATEADD('DAY', -2, CURRENT_DATE), 1, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user3', DATEADD('DAY', -3, CURRENT_DATE), 1, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user4', DATEADD('DAY', -4, CURRENT_DATE), 1, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user5', DATEADD('DAY', -5, CURRENT_DATE), 1, 1);

INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', -1, CURRENT_DATE), 2, 2);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user2', DATEADD('DAY', -2, CURRENT_DATE), 2, 2);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user3', DATEADD('DAY', -3, CURRENT_DATE), 2, 2);

INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', -1, CURRENT_DATE), 3, 3);

INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user1', DATEADD('DAY', -10, CURRENT_DATE), 1, 4);

INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user1', '2099-12-01', 1, 1);
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('user2', '2099-12-01', 2, 1);

INSERT INTO reservation_waiting (name, reservation_id)
VALUES ('user2', 1);

INSERT INTO reservation_waiting (name, reservation_id)
VALUES ('user1', 12);

INSERT INTO reservation_waiting (name, reservation_id)
VALUES ('user3', 1);


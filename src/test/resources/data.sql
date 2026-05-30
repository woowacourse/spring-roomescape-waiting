INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00');
INSERT INTO reservation_time (start_at, finish_at) VALUES ('14:00', '15:00');
INSERT INTO reservation_time (start_at, finish_at) VALUES ('18:00', '19:00');

INSERT INTO theme (name, description, image_url) VALUES ('테마A', '설명A', 'https://a.com');
INSERT INTO theme (name, description, image_url) VALUES ('테마B', '설명B', 'https://b.com');
INSERT INTO theme (name, description, image_url) VALUES ('테마C', '설명C', 'https://c.com');
INSERT INTO theme (name, description, image_url) VALUES ('테마D', '설명D', 'https://d.com');

INSERT INTO member (name, email, password) VALUES ('user1', 'user1@test.com', '1234');
INSERT INTO member (name, email, password) VALUES ('user2', 'user2@test.com', '1234');
INSERT INTO member (name, email, password) VALUES ('user3', 'user3@test.com', '1234');

INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY', -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, DATEADD('DAY', -2, CURRENT_DATE), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (3, DATEADD('DAY', -3, CURRENT_DATE), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY', -4, CURRENT_DATE), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, DATEADD('DAY', -5, CURRENT_DATE), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY', -1, CURRENT_DATE), 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, DATEADD('DAY', -2, CURRENT_DATE), 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (3, DATEADD('DAY', -3, CURRENT_DATE), 2, 2);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY', -1, CURRENT_DATE), 3, 3);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY', -10, CURRENT_DATE), 1, 4);

INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY', 11, CURRENT_DATE), 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, DATEADD('DAY', 12, CURRENT_DATE), 2, 1);

INSERT INTO reservation_waiting (member_id, date, time_id, theme_id) VALUES (2, DATEADD('DAY', -1, CURRENT_DATE), 1, 1);
INSERT INTO reservation_waiting (member_id, date, time_id, theme_id) VALUES (1, DATEADD('DAY', 12, CURRENT_DATE), 2, 1);
INSERT INTO reservation_waiting (member_id, date, time_id, theme_id) VALUES (3, DATEADD('DAY', -1, CURRENT_DATE), 1, 1);
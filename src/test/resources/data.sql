INSERT INTO theme (name, description, thumbnail) VALUES ('테마1', '재밌음', '/image/default.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마2', '무서움', '/image/default.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('테마3', '놀라움', '/image/default.jpg');

INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('11:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00');

INSERT INTO member (name, email, password) VALUES ('유저1', 'member1@email.com', 'password');
INSERT INTO member (name, email, password) VALUES ('유저2', 'member2@email.com', 'password');
INSERT INTO member (name, email, password) VALUES ('유저3', 'member3@email.com', 'password');

INSERT INTO admin (name, email, password) VALUES ('어드민', 'admin@email.com', 'password');

INSERT INTO reservation (date, time_id, theme_id, member_id, status) VALUES (CURRENT_TIMESTAMP - INTERVAL '1' DAY, 1, 1, 1, 'RESERVED');
INSERT INTO reservation (date, time_id, theme_id, member_id, status) VALUES (CURRENT_TIMESTAMP - INTERVAL '1' DAY, 2, 1, 1, 'RESERVED');
INSERT INTO reservation (date, time_id, theme_id, member_id, status) VALUES (CURRENT_TIMESTAMP - INTERVAL '3' DAY, 1, 3, 2, 'RESERVED');
INSERT INTO reservation (date, time_id, theme_id, member_id, status) VALUES (CURRENT_TIMESTAMP - INTERVAL '11' DAY, 1, 2, 3, 'RESERVED');

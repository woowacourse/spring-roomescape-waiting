INSERT INTO reservation_time(start_at)
VALUES ('10:00');
INSERT INTO reservation_time(start_at)
VALUES ('11:00');
INSERT INTO reservation_time(start_at)
VALUES ('12:00');
INSERT INTO reservation_time(start_at)
VALUES ('13:00');
INSERT INTO reservation_time(start_at)
VALUES ('14:00');
INSERT INTO reservation_time(start_at)
VALUES ('15:00');
INSERT INTO reservation_time(start_at)
VALUES ('16:00');
INSERT INTO reservation_time(start_at)
VALUES ('17:00');
INSERT INTO reservation_time(start_at)
VALUES ('18:00');
INSERT INTO reservation_time(start_at)
VALUES ('19:00');
INSERT INTO reservation_time(start_at)
VALUES ('20:00');
INSERT INTO reservation_time(start_at)
VALUES ('21:00');
INSERT INTO reservation_time(start_at)
VALUES ('22:00');

INSERT INTO theme(name, description, thumbnail)
VALUES ('theme1', 'description1', 'thumbnail1');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme2', 'description2', 'thumbnail2');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme3', 'description3', 'thumbnail3');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme4', 'description4', 'thumbnail4');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme5', 'description5', 'thumbnail5');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme6', 'description6', 'thumbnail6');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme7', 'description7', 'thumbnail7');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme8', 'description8', 'thumbnail8');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme9', 'description9', 'thumbnail9');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme10', 'description10', 'thumbnail10');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme11', 'description11', 'thumbnail11');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme12', 'description12', 'thumbnail12');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme13', 'description13', 'thumbnail13');
INSERT INTO theme(name, description, thumbnail)
VALUES ('theme14', 'description14', 'thumbnail14');

INSERT INTO member(name, email, password, role)
VALUES ('admin', 'wooteco@gmail.com', '$2a$10$HPuLMfygOsN.3UIEqvcBwOS/uaOS4cJ0EQb/eeqexol7BaiGMSXXi', 'admin');
INSERT INTO member(name, email, password, role)
VALUES ('riwon', 'riwon@gmail.com', '$2a$10$xulkTSZyjjPnfcvkoaQECusw/HPCnLx3p/AndyHEtimKZVljx0aVC', 'user');
INSERT INTO member(name, email, password, role)
VALUES ('mimi', 'mimi@gmail.com', '$2a$10$ZuuoYwqZXLsCBCdeUN0j1eifbsHzleUB0qjtw1UNd.xM1SwWb.MP2', 'user');

INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-10', 1, 1, 3);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-10', 2, 1, 3);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-10', 3, 2, 3);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-10', 4, 2, 3);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-10', 5, 2, 3);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-10', 6, 2, 3);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-10', 7, 3, 3);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-10', 8, 3, 3);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-10', 9, 3, 3);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-10', 10, 3, 3);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-10', 11, 3, 3);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-10', 12, 3, 3);

INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-15', 1, 4, 2);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-15', 2, 4, 2);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-15', 3, 4, 2);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-15', 4, 4, 2);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-15', 5, 4, 2);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-15', 6, 5, 2);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-15', 7, 5, 2);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-15', 8, 5, 2);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-15', 9, 5, 2);
INSERT INTO reservation(date, time_id, theme_id, member_id)
VALUES ('2025-05-15', 10, 5, 2);

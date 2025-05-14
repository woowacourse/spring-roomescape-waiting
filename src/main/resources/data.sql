INSERT INTO reservation_time (start_at)
VALUES ('10:00');
INSERT INTO reservation_time (start_at)
VALUES ('13:00');
INSERT INTO reservation_time (start_at)
VALUES ('15:00');
INSERT INTO reservation_time (start_at)
VALUES ('17:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 A', '테마 A입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 B', '테마 B입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 C', '테마 C입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 D', '테마 D입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('테마 E', '테마 E입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO member (name, email, password, auth_role)
VALUES ('가이온', 'jumdo12', 'jumdo12', 'ADMIN');

INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-22', 2, 4, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-23', 2, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-24', 2, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-25', 1, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-26', 2, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-26', 2, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-27', 1, 3, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-27', 2, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-28', 1, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-28', 2, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-04-30', 2, 5, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-05-09', 2, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-05-10', 2, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-05-13', 2, 2, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-05-14', 1, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-05-15', 2, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id)
VALUES ('2025-05-16', 2, 5, 1);

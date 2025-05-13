INSERT INTO theme(id, name, description, thumbnail)
VALUES (1, '테마1', '설명1', '사진1');
INSERT INTO theme(id, name, description, thumbnail)
VALUES (2, '테마2', '설명2', '사진2');
INSERT INTO theme(id, name, description, thumbnail)
VALUES (3, '테마3', '설명3', '사진3');
INSERT INTO theme(id, name, description, thumbnail)
VALUES (4, '테마4', '설명4', '사진4');
INSERT INTO theme(id, name, description, thumbnail)
VALUES (5, '테마5', '설명5', '사진5');
INSERT INTO theme(id, name, description, thumbnail)
VALUES (6, '테마6', '설명6', '사진6');
INSERT INTO theme(id, name, description, thumbnail)
VALUES (7, '테마7', '설명7', '사진7');
INSERT INTO theme(id, name, description, thumbnail)
VALUES (8, '테마8', '설명8', '사진8');
INSERT INTO theme(id, name, description, thumbnail)
VALUES (9, '테마9', '설명9', '사진9');
INSERT INTO theme(id, name, description, thumbnail)
VALUES (10, '테마10', '설명10', '사진10');
INSERT INTO theme(id, name, description, thumbnail)
VALUES (11, '테마11', '설명11', '사진11');

INSERT INTO reservation_time(id, start_at)
VALUES (1, '10:00');
INSERT INTO reservation_time(id, start_at)
VALUES (2, '11:00');
INSERT INTO reservation_time(id, start_at)
VALUES (3, '12:00');

INSERT INTO member(id, name, email, password, role)
VALUES (1, '어드민', 'admin@email.com', '1234', 'ADMIN');
INSERT INTO member(id, name, email, password, role)
VALUES (2, '유저', 'user@email.com', '1234', 'USER');

INSERT INTO reservation(id, date, time_id, theme_id, member_id)
VALUES (1, '2025-04-29', 1, 1, 1);
INSERT INTO reservation(id, date, time_id, theme_id, member_id)
VALUES (2, '2025-04-28', 1, 2, 1);
INSERT INTO reservation(id, date, time_id, theme_id, member_id)
VALUES (3, '2025-04-28', 3, 3, 1);
INSERT INTO reservation(id, date, time_id, theme_id, member_id)
VALUES (4, '2025-04-27', 1, 3, 1);


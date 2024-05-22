INSERT INTO member (name, email, password, role)
VALUES ('테니', 'tenny@email.com', '1234', 'ADMIN');
INSERT INTO member (name, email, password, role)
VALUES ('냥인', 'nyangin@email.com', '1234', 'MEMBER');

INSERT INTO reservation_time (start_at)
VALUES ('13:00:00');
INSERT INTO reservation_time (start_at)
VALUES ('14:00:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('커비', '매우 재밌습니다.', 'https://i.pinimg.com/474x/e2/55/4d/e2554dea5499f88c242178ce7ed568d9.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('포켓몬', '매우 귀엽습니다.', 'https://i.pinimg.com/474x/b3/aa/d7/b3aad752a5fbda932dd37015bca3047f.jpg');

INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (1, '2024-05-20', 1, 1);
INSERT INTO reservation (member_id, date, time_id, theme_id)
VALUES (2, '2024-05-20', 1, 2);
INSERT INTO waiting (member_id, date, time_id, theme_id)
VALUES (2, '2024-05-20', 1, 1);
INSERT INTO waiting (member_id, date, time_id, theme_id)
VALUES (1, '2024-05-20', 1, 2);
INSERT INTO waiting (member_id, date, time_id, theme_id)
VALUES (1, '2025-05-20', 1, 2);

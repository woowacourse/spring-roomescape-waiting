-- initialize data
DELETE
FROM reservation;
ALTER TABLE reservation
    ALTER COLUMN id RESTART;

DELETE
FROM reservation_time;
ALTER TABLE reservation_time
    ALTER COLUMN id RESTART;

DELETE
FROM theme;
ALTER TABLE theme
    ALTER COLUMN id RESTART;

DELETE
FROM member;
ALTER TABLE member
    ALTER COLUMN id RESTART;

-- reservation_time
INSERT INTO reservation_time(start_at)
VALUES ('10:00');
INSERT INTO reservation_time(start_at)
VALUES ('11:00');
INSERT INTO reservation_time(start_at)
VALUES ('12:00');
INSERT INTO reservation_time(start_at)
VALUES ('13:00');
-- theme
INSERT INTO theme(name, description, thumbnail)
VALUES ('레벨1 탈출', '우테코 레벨2를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('레벨2 탈출', '우테코 레벨3를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('레벨3 탈출', '우테코 레벨4를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('레벨4 탈출', '우테코 레벨5를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
-- member
INSERT INTO member(email, password, salt, name, role)
VALUES ('user@test.com', 'hashedpassword', 'salt', 'poke', 'USER');
INSERT INTO member(email, password, salt, name, role)
VALUES ('user2@test.com', 'hashedpassword', 'salt', 'duck', 'USER');
INSERT INTO member(email, password, salt, name, role)
VALUES ('admin@test.com', 'yAD5RFQKSwFkOImAX+hDr1RSlsR7MBBSFr/xni5sGOE=', 'SZj5iqlnmc4cLvpOAS0a1g==', 'wedge', 'ADMIN');
-- reservation
INSERT INTO reservation(date, time_id, theme_id, member_id, status)
VALUES ('2024-05-01', 3, 2, 1, 'RESERVED');
INSERT INTO reservation(date, time_id, theme_id, member_id, status)
VALUES ('2024-05-01', 2, 2, 1, 'RESERVED');
INSERT INTO reservation(date, time_id, theme_id, member_id, status)
VALUES ('2024-04-30', 2, 2, 1, 'RESERVED');
INSERT INTO reservation(date, time_id, theme_id, member_id, status)
VALUES ('2024-04-30', 1, 1, 1, 'RESERVED');
INSERT INTO reservation(date, time_id, theme_id, member_id, status)
VALUES ('2024-04-02', 3, 3, 1, 'RESERVED');
INSERT INTO reservation(date, time_id, theme_id, member_id, status)
VALUES ('2024-03-02', 3, 3, 1, 'RESERVED');
INSERT INTO reservation(date, time_id, theme_id, member_id, status)
VALUES ('2099-04-30', 1, 1, 1, 'RESERVED');
INSERT INTO reservation(date, time_id, theme_id, member_id, status)
VALUES ('2999-04-30', 1, 1, 2, 'RESERVED');
INSERT INTO reservation(date, time_id, theme_id, member_id, status)
VALUES ('2999-04-30', 1, 1, 3, 'WAITING');
INSERT INTO reservation(date, time_id, theme_id, member_id, status)
VALUES ('2999-04-30', 1, 1, 1, 'WAITING');

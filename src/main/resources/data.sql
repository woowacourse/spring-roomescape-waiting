INSERT INTO member (name, email, password, role)
VALUES ('사용자1', 'user1@wooteco.com', 'user1', 'USER');
INSERT INTO member (name, email, password, role)
VALUES ('사용자2', 'user2@wooteco.com', 'user2', 'USER');
INSERT INTO member (name, email, password, role)
VALUES ('사용자3', 'user3@wooteco.com', 'user3', 'USER');
INSERT INTO member (name, email, password, role)
VALUES ('사용자4', 'user4@wooteco.com', 'user4', 'USER');
INSERT INTO member (name, email, password, role)
VALUES ('사용자5', 'user5@wooteco.com', 'user5', 'USER');
INSERT INTO member (name, email, password, role)
VALUES ('관리자1', 'admin1@wooteco.com', 'admin1', 'ADMIN');

INSERT INTO reservation_time (start_at)
VALUES ('10:00');
INSERT INTO reservation_time (start_at)
VALUES ('11:00');
INSERT INTO reservation_time (start_at)
VALUES ('12:00');
INSERT INTO reservation_time (start_at)
VALUES ('13:00');
INSERT INTO reservation_time (start_at)
VALUES ('14:00');
INSERT INTO reservation_time (start_at)
VALUES ('15:00');

INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출1', '1번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출2', '2번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출3', '3번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출4', '4번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출5', '5번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail)
VALUES ('방탈출6', '6번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출7', '7번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출8', '8번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출9', '9번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출10', '10번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출11', '11번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출12', '12번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출13', '13번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출14', '14번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail)
VALUES ('방탈출15', '15번 방탈출', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO reservation (date, member_id, time_id, theme_id)
VALUES (CURRENT_DATE + INTERVAL '-1' DAY, 1, 1, 1),
       (CURRENT_DATE + INTERVAL '-1' DAY, 2, 2, 1),
       (CURRENT_DATE + INTERVAL '-1' DAY, 3, 3, 1),
       (CURRENT_DATE + INTERVAL '-1' DAY, 4, 4, 1),
       (CURRENT_DATE + INTERVAL '-1' DAY, 5, 5, 1),
       (CURRENT_DATE + INTERVAL '-2' DAY, 1, 1, 2),
       (CURRENT_DATE + INTERVAL '-2' DAY, 2, 2, 2),
       (CURRENT_DATE + INTERVAL '-2' DAY, 3, 3, 2),
       (CURRENT_DATE + INTERVAL '-2' DAY, 4, 4, 2),
       (CURRENT_DATE + INTERVAL '-3' DAY, 1, 1, 3),
       (CURRENT_DATE + INTERVAL '-3' DAY, 2, 2, 3),
       (CURRENT_DATE + INTERVAL '-3' DAY, 3, 3, 3),
       (CURRENT_DATE + INTERVAL '-4' DAY, 1, 1, 4),
       (CURRENT_DATE + INTERVAL '-4' DAY, 2, 2, 4),
       (CURRENT_DATE + INTERVAL '-6' DAY, 1, 1, 5),
       (CURRENT_DATE + INTERVAL '-6' DAY, 2, 2, 6),
       (CURRENT_DATE + INTERVAL '-7' DAY, 1, 1, 7),
       (CURRENT_DATE + INTERVAL '-8' DAY, 1, 1, 8),
       (CURRENT_DATE + INTERVAL '-1' DAY, 1, 1, 9),
       (CURRENT_DATE + INTERVAL '-2' DAY, 2, 2, 10),
       (CURRENT_DATE + INTERVAL '-3' DAY, 3, 3, 11),
       (CURRENT_DATE + INTERVAL '-4' DAY, 4, 4, 12);

INSERT INTO reservation (date, member_id, time_id, theme_id)
VALUES (CURRENT_DATE + INTERVAL '1' DAY, 1, 1, 1);
INSERT INTO reservation (date, member_id, time_id, theme_id)
VALUES (CURRENT_DATE + INTERVAL '1' DAY, 2, 2, 1);
INSERT INTO reservation (date, member_id, time_id, theme_id)
VALUES (CURRENT_DATE + INTERVAL '1' DAY, 2, 2, 2);

INSERT INTO reservation_wait (member_id, date, time_id, theme_id, status)
VALUES (2, CURRENT_DATE + INTERVAL '1' DAY, 1, 1, 'WAITING');
INSERT INTO reservation_wait (member_id, date, time_id, theme_id, status)
VALUES (3, CURRENT_DATE + INTERVAL '1' DAY, 1, 1, 'WAITING');
INSERT INTO reservation_wait (member_id, date, time_id, theme_id, status)
VALUES (1, CURRENT_DATE + INTERVAL '1' DAY, 2, 1, 'WAITING');
INSERT INTO reservation_wait (member_id, date, time_id, theme_id, status)
VALUES (1, CURRENT_DATE + INTERVAL '1' DAY, 2, 2, 'WAITING');

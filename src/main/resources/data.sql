INSERT INTO reservation_time(start_at)
VALUES ('10:00');
INSERT INTO reservation_time(start_at)
VALUES ('11:00');

INSERT INTO theme(name, description, thumbnail)
VALUES ('이름1', '설명1', '썸네일1');
INSERT INTO theme(name, description, thumbnail)
VALUES ('이름2', '설명2', '썸네일2');

INSERT INTO member(name, email, password, role)
VALUES ('운영진', 'admin@naver.com', '1234', 'ADMIN');
INSERT INTO member(name, email, password, role)
VALUES ('홍길동', 'member@naver.com', '1234', 'MEMBER');

INSERT INTO room_escape_information(date, time_id, theme_id)
VALUES ('2025-05-11', 1, 1);

INSERT INTO room_escape_information(date, time_id, theme_id)
VALUES ('2099-05-11', 1, 1);

INSERT INTO reservation(room_escape_information_id, member_id)
VALUES (1, 1);

INSERT INTO reservation(room_escape_information_id, member_id)
VALUES (2, 1);

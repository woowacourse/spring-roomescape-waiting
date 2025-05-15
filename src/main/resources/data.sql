INSERT INTO member(name, email, password, role) VALUES('normal1', 'normal1@normal.com', 'password', 'NORMAL');
INSERT INTO member(name, email, password, role) VALUES('normal2', 'normal2@normal.com', 'password', 'NORMAL');
INSERT INTO member(name, email, password, role) VALUES('normal3', 'normal3@normal.com', 'password', 'NORMAL');
INSERT INTO member(name, email, password, role) VALUES('normal4', 'normal4@normal.com', 'password', 'NORMAL');
INSERT INTO member(name, email, password, role) VALUES('admin', 'admin@admin.com', 'admin', 'ADMIN');

INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('14:00');
INSERT INTO reservation_time (start_at) VALUES ('16:00');
INSERT INTO reservation_time (start_at) VALUES ('18:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00');

INSERT INTO theme (name, description, thumbnail) VALUES ('레벨1 탈출', '자바 완전 정복', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('레벨2 탈출', '스프링 완전 정복', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('레벨3 탈출', '서비스 완전 정복', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
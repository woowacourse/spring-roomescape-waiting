INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('11:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00');
INSERT INTO reservation_time (start_at) VALUES ('13:00');
INSERT INTO reservation_time (start_at) VALUES ('14:00');
INSERT INTO reservation_time (start_at) VALUES ('15:00');
INSERT INTO reservation_time (start_at) VALUES ('16:00');
INSERT INTO reservation_time (start_at) VALUES ('17:00');
INSERT INTO reservation_time (start_at) VALUES ('18:00');
INSERT INTO reservation_time (start_at) VALUES ('19:00');
INSERT INTO reservation_time (start_at) VALUES ('20:00');

INSERT INTO theme (name, description, thumbnail) VALUES ('요정의 숲', '판타지', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('치유의 공간', '힐링', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('시간 여행자의 모험', '어드벤처', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('마법 학교', '판타지', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('단잠', '공포', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('보헤미아 왕국의 스캔들', '공포', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('퓨처리스트', '공포', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme (name, description, thumbnail) VALUES ('022', '아케이드', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

INSERT INTO member (name, email, password, role) VALUES ('에버', 'ever@gmail.com', 'ever123!', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('아서', 'hyunta@gmail.com', 'hyunta123!', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('왓에버', 'whatever@gmail.com', 'whatever123!', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('후에버', 'whoever@gmail.com', 'whoever123!', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('하우에버', 'however@gmail.com', 'however123!', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('웨어에버', 'wherever@gmail.com', 'wherever123!', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('웬에버', 'whenever@gmail.com', 'whenever123!', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('나무늘보', 'treeboss@gmail.com', 'treeboss123!', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('관리자', 'admin@gmail.com', 'admin123!', 'ADMIN');

INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2024-05-22', 1, 1, 1);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2024-05-22', 2, 2, 2);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2024-05-22', 3, 3, 3);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2024-05-22', 4, 4, 4);
INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES ('2024-05-22', 5, 5, 5);

INSERT INTO waiting (date, time_id, theme_id, member_id) VALUES ('2024-05-22', 1, 1, 2);
INSERT INTO waiting (date, time_id, theme_id, member_id) VALUES ('2024-05-22', 2, 2, 1);

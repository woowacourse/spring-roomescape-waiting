INSERT INTO stores(name) VALUES ('강남점');
INSERT INTO stores(name) VALUES ('홍대점');

-- password: password
INSERT INTO members(name, email, password, role) VALUES ('어드민', 'admin@test.com', '$2a$10$LzNRNMIeDFJdLCa.esEa0.RW6uxlRb3JruT7QtWUHF.xAIJeDzDrC', 'ADMIN');
INSERT INTO members(name, email, password, role) VALUES ('유저', 'user@test.com', '$2a$10$LzNRNMIeDFJdLCa.esEa0.RW6uxlRb3JruT7QtWUHF.xAIJeDzDrC', 'USER');
INSERT INTO members(name, email, password, role, store_id) VALUES ('강남매니저', 'manager1@test.com', '$2a$10$LzNRNMIeDFJdLCa.esEa0.RW6uxlRb3JruT7QtWUHF.xAIJeDzDrC', 'MANAGER', 1);
INSERT INTO members(name, email, password, role, store_id) VALUES ('홍대매니저', 'manager2@test.com', '$2a$10$LzNRNMIeDFJdLCa.esEa0.RW6uxlRb3JruT7QtWUHF.xAIJeDzDrC', 'MANAGER', 2);

INSERT INTO times(start_at) values ('10:00');
INSERT INTO times(start_at) values ('12:00');
INSERT INTO times(start_at) values ('14:00');
INSERT INTO times(start_at) values ('16:00');
INSERT INTO times(start_at) values ('18:00');
INSERT INTO times(start_at) values ('20:00');

INSERT INTO themes(name, thumbnail_url, description)
VALUES ('냥이 점집', 'https://i.postimg.cc/3JRp43dK/1553676990.jpg', '난이도 NORMAL 3/5');
INSERT INTO themes(name, thumbnail_url, description)
VALUES ('어느 구두쇠의 전시회장', 'https://i.postimg.cc/4yrMrRfQ/image.jpg', '난이도: NORMAL 3/5');
INSERT INTO themes(name, thumbnail_url, description)
VALUES ('준단화:몸을 잘라낸 꽃',
        'https://www.seoul-escape.com/storage/episode/2024_11/06/qnAHwzCVuvRU7x62epGGSUciARX22w08CsrMSBb9.png',
        '난이도: NORMAL 3/5');
INSERT INTO themes(name, thumbnail_url, description)
VALUES ('팩토리', 'https://www.seoul-escape.com/storage/episode/2024_11/06/qnAHwzCVuvRU7x62epGGSUciARX22w08CsrMSBb9.png',
        '난이도: NORMAL 3/5');
INSERT INTO themes(name, thumbnail_url, description)
VALUES ('고문실', 'https://www.seoul-escape.com/storage/episode/2022_11/09/hFa2HaQPrHERgVtstgwsVfdMGT69AGxRMRXpolRe.png',
        '난이도: NORMAL 3/5');
INSERT INTO themes(name, thumbnail_url, description)
VALUES ('엘리베이터', 'https://www.seoul-escape.com/storage/episode/2022_11/09/ZEiSp4KjRt6L47SroX8ikS0OoeR99nftI4ndeS2r.png',
        '난이도: NORMAL 3/5');
INSERT INTO themes(name, thumbnail_url, description)
VALUES ('접견', 'https://www.seoul-escape.com/storage/episode/2026_04/11/mbAc3GHBTmF9mWtXh6JJ1DW2lyioqzt5ih68Pnie.png',
        '난이도: NORMAL 3/5');
INSERT INTO themes(name, thumbnail_url, description)
VALUES ('오시리스', 'https://www.seoul-escape.com/storage/episode/2026_02/20/m9gnxCTeS22AbuXCNRJ4SKLzLtzk8NMEwuJzebb4.png',
        '난이도: NORMAL 3/5');
INSERT INTO themes(name, thumbnail_url, description)
VALUES ('만찬', 'https://www.seoul-escape.com/storage/episode/2026_02/20/m9gnxCTeS22AbuXCNRJ4SKLzLtzk8NMEwuJzebb4.png',
        '난이도: NORMAL 3/5');
INSERT INTO themes(name, thumbnail_url, description)
VALUES ('오모테나시', 'https://www.seoul-escape.com/storage/episode/2024_11/06/qnAHwzCVuvRU7x62epGGSUciARX22w08CsrMSBb9.png',
        '난이도: NORMAL 3/5');

-- 강남점 매니저(manager1@test.com) 조회용 예약
INSERT INTO reservations(member_id, date, theme_id, time_id, store_id, status)
VALUES (2, CURRENT_DATE, 1, 1, 1, 'BOOKED');
INSERT INTO reservations(member_id, date, theme_id, time_id, store_id, status)
VALUES (2, CURRENT_DATE, 2, 2, 1, 'BOOKED');
INSERT INTO reservations(member_id, date, theme_id, time_id, store_id, status)
VALUES (2, '2026-10-01', 3, 3, 1, 'BOOKED');

-- 홍대점 매니저(manager2@test.com) 조회용 예약
INSERT INTO reservations(member_id, date, theme_id, time_id, store_id, status)
VALUES (2, CURRENT_DATE, 4, 4, 2, 'BOOKED');

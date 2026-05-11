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
INSERT INTO reservation_time (start_at) VALUES ('21:00');

INSERT INTO theme (name, description, url)
VALUES ('인형의 집', '공포 테마의 클래식, 밤마다 살아 움직이는 인형들이 가득한 저택을 탈출하세요.', 'https://example.com/1');

INSERT INTO theme (name, description, url)
VALUES ('해적왕의 보물', '침몰한 해적선 속 숨겨진 황금 보물을 찾아 제한 시간 내에 탈출해야 합니다.', 'https://example.com/2');

INSERT INTO theme (name, description, url)
VALUES ('명탐정의 부재', '사라진 명탐정의 사무실에 남겨진 단서들을 조합해 진범을 밝혀내세요.', 'https://example.com/3');

INSERT INTO theme (name, description, url)
VALUES ('우주정거장: 블랙아웃', '산소가 고갈되기 직전의 우주정거장에서 탈출 포드를 가동하세요.', 'https://example.com/4');

INSERT INTO theme (name, description, url)
VALUES ('꿈속의 과자집', '꿈속에서 길을 잃은 당신, 달콤하지만 위험한 과자집의 비밀을 풀어야 합니다.', 'https://example.com/5');


INSERT INTO reservation (name, date, theme_id, time_id, created_at)
VALUES ('브라운', '2024-06-01', 1, 1, '2024-05-15 10:30:00');

INSERT INTO reservation (name, date, theme_id, time_id, created_at)
VALUES ('네오', '2024-06-02', 2, 3, '2024-05-16 14:20:00');

INSERT INTO reservation (name, date, theme_id, time_id, created_at)
VALUES ('제이슨', '2024-06-05', 3, 2, '2024-05-18 09:15:00');

INSERT INTO reservation (name, date, theme_id, time_id, created_at)
VALUES ('워니', '2024-06-05', 4, 4, '2024-05-20 18:45:00');

INSERT INTO reservation (name, date, theme_id, time_id, created_at)
VALUES ('포비', '2024-06-10', 5, 1, '2024-05-21 22:00:00');
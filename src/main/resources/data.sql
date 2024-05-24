INSERT INTO theme (name, description, thumbnail)
VALUES ('고풍 한옥 마을', '한국의 전통적인 아름다움이 당신을 맞이합니다.', 'https://via.placeholder.com/150/92c952'),
       ('우주 탐험', '끝없는 우주에 숨겨진 비밀을 파헤치세요.', 'https://via.placeholder.com/150/771796'),
       ('시간여행', '과거와 미래를 오가며 역사의 비밀을 밝혀보세요.', 'https://via.placeholder.com/150/24f355'),
       ('마법의 숲', '요정과 마법사들이 사는 신비로운 숲 속으로!', 'https://via.placeholder.com/150/30f9e7'),
       ('타임캡슐', '오래된 타임캡슐을 찾아내어 그 안의 비밀을 풀어보세요.', 'https://via.placeholder.com/150/56a8c2'),
       ('로맨틱 유럽 여행', '로맨틱한 분위기 속에서 유럽을 여행하세요.', 'https://via.placeholder.com/150/7472e7'),
       ('신화 속의 세계', '신화와 전설 속으로 당신을 초대합니다.', 'https://via.placeholder.com/150/24f355'),
       ('바다 속 신비', '깊은 바다에서의 모험을 경험하세요.', 'https://via.placeholder.com/150/56a8c2');

INSERT INTO reservation_time (start_at)
VALUES ('09:00'),
       ('12:00'),
       ('17:00'),
       ('21:00');

INSERT INTO member (email, password, name, role)
VALUES ('admin@gmail.com', '$2a$10$MbGFqyn/u4wfggRK7HAqDeC1y9s1mESgmXV3b7e7GZT5u1JkIT.gm', '어드민', 'ADMIN'),
       -- password: abc123
       ('user@gmail.com', '$2a$10$MbGFqyn/u4wfggRK7HAqDeC1y9s1mESgmXV3b7e7GZT5u1JkIT.gm', '유저', 'USER'),
       -- password: abc123
       ('example1@gmail.com', '1234', '구름a', 'USER'),
       ('example2@gmail.com', '1234', '구름b', 'USER'),
       ('example3@gmail.com', '1234', '구름c', 'USER'),
       ('example4@gmail.com', '1234', '구름d', 'USER');

INSERT INTO reservation (date, member_id, time_id, theme_id)
VALUES ('2024-04-28', 1, 1, 5),
       ('2024-04-28', 2, 2, 5),
       ('2024-04-28', 3, 3, 5),
       ('2024-04-29', 4, 1, 5),
       ('2024-04-29', 1, 2, 5),
       ('2024-04-29', 2, 1, 4),
       ('2024-04-29', 3, 2, 4),
       ('2024-04-29', 4, 3, 4),
       ('2024-04-29', 1, 4, 4),
       ('2024-05-01', 2, 1, 3),
       ('2024-05-01', 3, 2, 3),
       ('2024-05-01', 4, 3, 3),
       ('2024-05-02', 1, 1, 2),
       ('2024-05-02', 2, 2, 2),
       ('2024-05-02', 3, 1, 1),
       ('2024-05-03', 4, 2, 1),
       ('2024-05-03', 1, 3, 1),
       ('2024-05-03', 2, 4, 1),
       ('2024-05-04', 3, 1, 5),
       ('2024-05-04', 4, 2, 4),
       ('2024-05-04', 1, 3, 3),
       ('2024-05-04', 2, 4, 2);
